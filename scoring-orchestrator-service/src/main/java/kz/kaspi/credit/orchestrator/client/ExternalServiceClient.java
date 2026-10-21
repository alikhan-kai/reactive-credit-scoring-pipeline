package kz.kaspi.credit.orchestrator.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import kz.kaspi.credit.contracts.external.AisOipDebtorDto;
import kz.kaspi.credit.contracts.external.GcvpDeductionsDto;
import kz.kaspi.credit.contracts.external.PkbReportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalServiceClient {
    private static final Duration EXTERNAL_CALL_TIMEOUT = Duration.ofMillis(1200);
    private final WebClient externalMockWebClient;

    // Запрос кредитного отчета ПКБ (Первое Кредитное Бюро)
    public Mono<PkbReportDto> fetchPkbReport(String iin) {
        return externalMockWebClient.get()
                .uri("/mock/pkb/{iin}", iin)
                .retrieve()
                .bodyToMono(PkbReportDto.class)
                .timeout(EXTERNAL_CALL_TIMEOUT)
                .doOnError(e -> log.warn("Ошибка или таймаут вызова ПКБ для ИИН {}: {}", iin, e.getMessage()))
                .onErrorResume(e -> Mono.just(createPkbFallback(iin)));
    }

    // Запрос пенсионных отчислений ГЦВП
    public Mono<GcvpDeductionsDto> fetchGcvpDeductions(String iin) {
        return externalMockWebClient.get()
                .uri("/mock/gcvp/{iin}", iin)
                .retrieve()
                .bodyToMono(GcvpDeductionsDto.class)
                .timeout(EXTERNAL_CALL_TIMEOUT)
                .doOnError(e -> log.warn("\"Ошибка или таймаут вызова ГЦВП для ИИН {}: {}", iin, e.getMessage()))
                .onErrorResume(e -> Mono.just(createGcvpFallback(iin)));
    }

    // Запрос реестра должников АИС ОИП (Минюст РК - аресты и ограничения)
    public Mono<AisOipDebtorDto> fetchAisOipDebtor(String iin) {
        return externalMockWebClient.get()
                .uri("/mock/ais-oip/{iin}", iin)
                .retrieve()
                .bodyToMono(AisOipDebtorDto.class)
                .timeout(EXTERNAL_CALL_TIMEOUT)
                .doOnError(e -> log.warn("Ошибка или таймаут вызова АИС ОИП для ИИН {}: {}", iin, e.getMessage()))
                .onErrorResume(e -> Mono.just(createAisOipFallback(iin)));
    }

    // Fallback-сценарии при недоступности госсервисов
    private PkbReportDto createPkbFallback(String iin) {
        return PkbReportDto.builder()
                .iin(iin)
                .creditScore(500) // базовый нейтральный скор при сбое
                .activeCreditsCount(0)
                .totalMonthlyPayment(BigDecimal.ZERO)
                .totalOutstandingDebt(BigDecimal.ZERO)
                .maxDpdLast12Months(0)
                .firstPaymentDefault(false)
                .build();
    }

    private GcvpDeductionsDto createGcvpFallback(String iin) {
        // Минимальный расчетный резервный доход (например, 150 000 KZT при сбое ГЦВП)
        return GcvpDeductionsDto.builder()
                .iin(iin)
                .monthlyDeductions(Collections.emptyList())
                .calculatedAverageMonthlyIncome(new BigDecimal("150000.00"))
                .consecutiveMonthsCount(0)
                .hasIrregularGaps(true)
                .build();
    }

    private AisOipDebtorDto createAisOipFallback(String iin) {
        return AisOipDebtorDto.builder()
                .iin(iin)
                .isDebtor(false)
                .hasActiveAccountArrest(false)
                .hasTravelBan(false)
                .totalBailiffDebt(BigDecimal.ZERO)
                .activeProceedingsCount(0)
                .build();
    }
}
