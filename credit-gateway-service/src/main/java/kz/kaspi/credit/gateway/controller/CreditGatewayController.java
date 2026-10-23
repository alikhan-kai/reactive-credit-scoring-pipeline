package kz.kaspi.credit.gateway.controller;

import jakarta.validation.Valid;
import kz.kaspi.credit.contracts.dto.CreditApplicationRequest;
import kz.kaspi.credit.contracts.dto.CreditApplicationResponse;
import kz.kaspi.credit.contracts.exception.IdempotencyConflictException;
import kz.kaspi.credit.gateway.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/credit")
@RequiredArgsConstructor
public class CreditGatewayController {

    private final IdempotencyService idempotencyService;
    private final WebClient orchestratorWebClient;

    @PostMapping("/apply")
    public Mono<ResponseEntity<CreditApplicationResponse>> applyForCredit(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreditApplicationRequest request) {
        // Если клиент не передал ключ, генерируем временный
        String effectiveKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        log.info("Входной запрос на кредит/рассрочку: ИИН {}, сумма {}, ключ идемпотентности {}",
                request.getIin(), request.getRequestedAmount(), effectiveKey);

        // 1. Атомарная проверка идемпотентности через Redis SETNX
        return idempotencyService.acquireLock(effectiveKey)
                .flatMap(acquired -> {
                    if (Boolean.FALSE.equals(acquired)) {
                        log.warn("Конфликт идемпотентности по ключу {}", effectiveKey);
                        return Mono.error(new IdempotencyConflictException(effectiveKey));
                    }

                    // 2. Неблокирующий вызов оркестратора скоринга
                    return orchestratorWebClient.post()
                            .uri("/internal/v1/scoring/evaluate")
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(CreditApplicationResponse.class)
                            .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
                });
    }
}