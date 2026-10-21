package kz.kaspi.credit.orchestrator.service;

import kz.kaspi.credit.contracts.constants.ScoringConstants;
import kz.kaspi.credit.contracts.dto.CreditApplicationRequest;
import kz.kaspi.credit.contracts.dto.CreditApplicationResponse;
import kz.kaspi.credit.contracts.enums.ApplicationStatus;
import kz.kaspi.credit.contracts.enums.CreditProductType;
import kz.kaspi.credit.contracts.external.AisOipDebtorDto;
import kz.kaspi.credit.contracts.external.GcvpDeductionsDto;
import kz.kaspi.credit.contracts.external.PkbReportDto;
import kz.kaspi.credit.contracts.util.FinancialCalculator;
import kz.kaspi.credit.orchestrator.client.ExternalServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringOrchestrationService {

    // Базовая процентная ставка для экспресс-кредита (24% годовых)
    private static final BigDecimal EXPRESS_CREDIT_RATE = new BigDecimal("24.0");

    private final ExternalServiceClient externalClient;

    public Mono<CreditApplicationResponse> evaluateApplication(UUID applicationId, CreditApplicationRequest request) {
        log.info("Запуск реактивного скоринга для заявки {}, ИИН {}", applicationId, request.getIin());

        // Реактивный Scatter-Gather: параллельный опрос ПКБ, ГЦВП и АИС ОИП
        return Mono.zip(
                externalClient.fetchPkbReport(request.getIin()),
                externalClient.fetchGcvpDeductions(request.getIin()),
                externalClient.fetchAisOipDebtor(request.getIin())
        ).map(tuple -> {
            PkbReportDto pkb = tuple.getT1();
            GcvpDeductionsDto gcvp = tuple.getT2();
            AisOipDebtorDto aisOip = tuple.getT3();

            return makeDecision(applicationId, request, pkb, gcvp, aisOip);
        });
    }

    private CreditApplicationResponse makeDecision(
            UUID applicationId,
            CreditApplicationRequest request,
            PkbReportDto pkb,
            GcvpDeductionsDto gcvp,
            AisOipDebtorDto aisOip
    ) {
        // 1. Проверка СТОП-ФАКТОРА: Аресты счетов судебными исполнителями (Минюст / АИС ОИП)
        if (aisOip.isHasActiveAccountArrest()) {
            log.warn("Отказ по заявке {}: наличие арестов счетов в АИС ОИП", applicationId);
            return reject(applicationId, request, "Наличие действующих арестов банковских счетов");
        }

        // 2. Проверка СТОП-ФАКТОРА: Злостный дефолт в ПКБ (DPD 90+ или FPD)
        if (pkb.isFirstPaymentDefault() || pkb.getMaxDpdLast12Months() >= 90) {
            log.warn("Отказ по заявке {}: критические просрочки в ПКБ (DPD={}, FPD={})",
                    applicationId, pkb.getMaxDpdLast12Months(), pkb.isFirstPaymentDefault());
            return reject(applicationId, request, "Негативная кредитная история: просрочки свыше 90 дней");
        }

        // 3. Расчет процентной ставки в зависимости от продукта
        BigDecimal annualRate = (request.getProductType() == CreditProductType.EXPRESS_CREDIT)
                ? EXPRESS_CREDIT_RATE
                : ScoringConstants.INSTALLMENT_INTEREST_RATE; // 0% для рассрочки 0-0-12

        // 4. Расчет ежемесячного платежа по запрашиваемой сумме
        BigDecimal requestedMonthlyPayment = FinancialCalculator.calculateMonthlyPayment(
                request.getRequestedAmount(),
                annualRate,
                request.getTermMonths()
        );

        // 5. Расчет КДН (DTI)
        BigDecimal monthlyIncome = gcvp.getCalculatedAverageMonthlyIncome();
        BigDecimal dti = FinancialCalculator.calculateDti(
                pkb.getTotalMonthlyPayment(),
                requestedMonthlyPayment,
                monthlyIncome
        );

        // 6. Проверка порога долговой нагрузки КДН <= 50% (норматив АРРФР)
        if (dti.compareTo(ScoringConstants.MAX_ALLOWED_DTI) > 0) {
            // Если КДН превышен, рассчитываем максимальную доступную сумму (Counter-Offer)
            BigDecimal maxAllowedMonthlyPayment = monthlyIncome
                    .multiply(ScoringConstants.MAX_ALLOWED_DTI)
                    .subtract(pkb.getTotalMonthlyPayment());

            if (maxAllowedMonthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Отказ по заявке {}: долговая нагрузка КДН превышает 50%, лимит исчерпан", applicationId);
                return reject(applicationId, request, "Превышение максимального коэффициента долговой нагрузки (КДН > 50%)");
            }

            // Предлагаем уменьшенную сумму рассрочки под допустимый платеж
            BigDecimal counterOfferAmount = maxAllowedMonthlyPayment
                    .multiply(BigDecimal.valueOf(request.getTermMonths()))
                    .setScale(0, RoundingMode.DOWN);

            if (counterOfferAmount.compareTo(ScoringConstants.MIN_LOAN_AMOUNT) < 0) {
                return reject(applicationId, request, "Превышение лимита долговой нагрузки (КДН > 50%)");
            }

            BigDecimal counterOfferMonthlyPayment = FinancialCalculator.calculateMonthlyPayment(
                    counterOfferAmount, annualRate, request.getTermMonths()
            );

            log.info("Встречное предложение (Counter-Offer) для заявки {}: запрошено {}, предложено {}",
                    applicationId, request.getRequestedAmount(), counterOfferAmount);

            return CreditApplicationResponse.builder()
                    .applicationId(applicationId)
                    .iin(request.getIin())
                    .status(ApplicationStatus.COUNTER_OFFER)
                    .productType(request.getProductType())
                    .requestedAmount(request.getRequestedAmount())
                    .approvedAmount(counterOfferAmount)
                    .approvedTermMonths(request.getTermMonths())
                    .monthlyPayment(counterOfferMonthlyPayment)
                    .interestRate(annualRate)
                    .calculatedDti(dti)
                    .creditScore(pkb.getCreditScore())
                    .decisionTimestamp(Instant.now())
                    .build();
        }

        // 7. УСПЕШНОЕ ОДОБРЕНИЕ (APPROVED)
        log.info("Заявка {} успешно одобрена: сумма {}, платеж {}, DTI {}",
                applicationId, request.getRequestedAmount(), requestedMonthlyPayment, dti);

        return CreditApplicationResponse.builder()
                .applicationId(applicationId)
                .iin(request.getIin())
                .status(ApplicationStatus.APPROVED)
                .productType(request.getProductType())
                .requestedAmount(request.getRequestedAmount())
                .approvedAmount(request.getRequestedAmount())
                .approvedTermMonths(request.getTermMonths())
                .monthlyPayment(requestedMonthlyPayment)
                .interestRate(annualRate)
                .calculatedDti(dti)
                .creditScore(pkb.getCreditScore())
                .decisionTimestamp(Instant.now())
                .build();
    }

    private CreditApplicationResponse reject(UUID applicationId, CreditApplicationRequest request, String reason) {
        return CreditApplicationResponse.builder()
                .applicationId(applicationId)
                .iin(request.getIin())
                .status(ApplicationStatus.REJECTED)
                .productType(request.getProductType())
                .requestedAmount(request.getRequestedAmount())
                .approvedAmount(BigDecimal.ZERO)
                .approvedTermMonths(0)
                .monthlyPayment(BigDecimal.ZERO)
                .interestRate(BigDecimal.ZERO)
                .rejectionReason(reason)
                .decisionTimestamp(Instant.now())
                .build();
    }
}
