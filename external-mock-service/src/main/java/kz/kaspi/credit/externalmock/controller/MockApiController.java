package kz.kaspi.credit.externalmock.controller;

import kz.kaspi.credit.contracts.external.AisOipDebtorDto;
import kz.kaspi.credit.contracts.external.GcvpDeductionsDto;
import kz.kaspi.credit.contracts.external.PkbReportDto;
import kz.kaspi.credit.externalmock.config.MockProperties;
import kz.kaspi.credit.externalmock.domain.BorrowerArchetype;
import kz.kaspi.credit.externalmock.service.MockDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
public class MockApiController {

    private final MockDataProvider dataProvider;
    private final MockProperties properties;

    @GetMapping("/pkb/{iin}")
    public Mono<PkbReportDto> getPkbReport(@PathVariable String iin) {
        log.info("Запрос отчета ПКБ для ИИН: {}", iin);
        BorrowerArchetype archetype = dataProvider.resolveArchetype(iin);
        long delay = (archetype == BorrowerArchetype.CHAOS_TIMEOUT)
                ? properties.getTimeoutDelayMs()
                : properties.getPkbDelayMs();

        return Mono.just(dataProvider.getPkbReport(iin))
                .delayElement(Duration.ofMillis(delay));
    }

    @GetMapping("/gcvp/{iin}")
    public Mono<GcvpDeductionsDto> getGcvpDeductions(@PathVariable String iin) {
        log.info("Запрос отчислений ГЦВП для ИИН: {}", iin);
        BorrowerArchetype archetype = dataProvider.resolveArchetype(iin);
        long delay = (archetype == BorrowerArchetype.CHAOS_TIMEOUT)
                ? properties.getTimeoutDelayMs()
                : properties.getGcvpDelayMs();

        return Mono.defer(() -> {
            // Симуляция 5% сбоя внешнего шлюза ГЦВП для проверки Fallback
            if (Math.random() < properties.getGcvpFailureRate() && archetype != BorrowerArchetype.PRIME) {
                log.warn("Симуляция сбоя 504 Gateway Timeout от ГЦВП для ИИН: {}", iin);
                return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "ГЦВП временно недоступен"));
            }
            return Mono.just(dataProvider.getGcvpDeductions(iin));
        }).delayElement(Duration.ofMillis(delay));
    }

    @GetMapping("/ais-oip/{iin}")
    public Mono<AisOipDebtorDto> getAisOipDebtor(@PathVariable String iin) {
        log.info("Запрос реестра должников АИС ОИП для ИИН: {}", iin);
        return Mono.just(dataProvider.getAisOipDebtor(iin))
                .delayElement(Duration.ofMillis(properties.getAisOipDelayMs()));
    }
}
