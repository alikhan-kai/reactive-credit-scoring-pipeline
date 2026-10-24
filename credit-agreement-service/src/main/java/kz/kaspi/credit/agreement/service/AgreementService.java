package kz.kaspi.credit.agreement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import kz.kaspi.credit.agreement.domain.CreditAgreement;
import kz.kaspi.credit.agreement.domain.PaymentScheduleItem;
import kz.kaspi.credit.agreement.repository.CreditAgreementRepository;
import kz.kaspi.credit.agreement.repository.PaymentScheduleRepository;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgreementService {

    private final CreditAgreementRepository agreementRepository;
    private final PaymentScheduleRepository scheduleRepository;
    private final TransactionalOperator TransactionalOperator;

    /**
     * Создает кредитный договор и потоково генерирует график платежей
     * в рамках единой неблокирующей транзакции R2DBC.
     */
    public Mono<CreditAgreement> createAgreementWithSchedule(CreditApprovedEvent event) {
        UUID agreementId = UUID.randomUUID();
        Instant now = Instant.now();

        CreditAgreement agreement = CreditAgreement.builder()
                .id(agreementId)
                .applicationId(event.getApplicationId())
                .iin(event.getIin())
                .productType(event.getProductType().name())
                .approvedAmount(event.getApprovedAmount())
                .termMonths(event.getTermMonths())
                .interestRate(event.getInterestRate())
                .monthlyPayment(event.getMonthlyPayment())
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 1. Сохраняем договор
        Mono<CreditAgreement> saveAgreementMono = agreementRepository.save(agreement)
                .doOnSuccess(saved -> log.info("Договор {} успешно сохранен для заявки {}",
                        saved.getId(), event.getApplicationId()));

        // 2. Потоково генерируем и пакетно сохраняем график платежей
        Mono<Void> saveScheduleMono = generatePaymentSchedule(agreementId, event)
                .collectList()
                .flatMapMany(scheduleRepository::saveAll)
                .then();

        // 3. Объединяем в единую реактивную транзакцию через TransactionalOperator
        return saveAgreementMono
                .flatMap(savedAgreement -> saveScheduleMono.thenReturn(savedAgreement))
                .as(TransactionalOperator::transactional)
                .doOnError(e -> log.error("Ошибка при создании договора и графика для заявки {}: {}",
                        event.getApplicationId(), e.getMessage()));
    }

    /**
     * Реактивный расчет графика погашения через Flux.range(1, termMonths)
     */
    private Flux<PaymentScheduleItem> generatePaymentSchedule(UUID agreementId, CreditApprovedEvent event) {
        int term = event.getTermMonths();
        BigDecimal totalAmount = event.getApprovedAmount();
        BigDecimal monthlyPayment = event.getMonthlyPayment();
        LocalDate startDate = LocalDate.now();
        Instant now = Instant.now();

        // Для рассрочки 0-0-12 (ставка 0%) проценты нулевые, основной долг =
        // ежемесячный платеж
        boolean isZeroPercent = event.getInterestRate() == null
                || event.getInterestRate().compareTo(BigDecimal.ZERO) == 0;

        return Flux.range(1, term)
                .map(monthNumber -> {
                    LocalDate paymentDate = startDate.plusMonths(monthNumber);
                    BigDecimal principalPart;
                    BigDecimal interestPart;

                    if (isZeroPercent) {
                        principalPart = monthlyPayment;
                        interestPart = BigDecimal.ZERO;
                    } else {
                        // Расчет процентов по аннуитету
                        double monthlyRate = event.getInterestRate().doubleValue() / 100.0 / 12.0;
                        interestPart = totalAmount.multiply(BigDecimal.valueOf(monthlyRate)).setScale(2,
                                RoundingMode.HALF_UP);
                        principalPart = monthlyPayment.subtract(interestPart).setScale(2, RoundingMode.HALF_UP);
                    }

                    BigDecimal remainingDebt = totalAmount
                            .subtract(principalPart.multiply(BigDecimal.valueOf(monthNumber)))
                            .max(BigDecimal.ZERO);
                    return PaymentScheduleItem.builder()
                            .id(UUID.randomUUID())
                            .agreementId(agreementId)
                            .paymentNumber(monthNumber)
                            .paymentDate(paymentDate)
                            .monthlyPayment(monthlyPayment)
                            .principalPayment(principalPart)
                            .interestPayment(interestPart)
                            .remainingDebt(remainingDebt)
                            .createdAt(now)
                            .build();
                });
    }
}
