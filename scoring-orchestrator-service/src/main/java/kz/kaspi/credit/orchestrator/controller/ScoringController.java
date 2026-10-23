package kz.kaspi.credit.orchestrator.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.kaspi.credit.contracts.dto.CreditApplicationRequest;
import kz.kaspi.credit.contracts.dto.CreditApplicationResponse;
import kz.kaspi.credit.orchestrator.producer.DecisionEventProducer;
import kz.kaspi.credit.orchestrator.service.ScoringOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/internal/v1/scoring")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringOrchestrationService orchestrationService;
    private final DecisionEventProducer decisionProducer;

    @PostMapping("/evaluate")
    public Mono<ResponseEntity<CreditApplicationResponse>> evaluate(
            @Valid @RequestBody CreditApplicationRequest request) {
        UUID applicationId = UUID.randomUUID();
        log.info("Получен запрос на скоринг заявки {} для ИИН {}", applicationId, request.getIin());
        return orchestrationService.evaluateApplication(applicationId, request)
                .flatMap(decisionProducer::publishDecision)
                .map(ResponseEntity::ok)
                .doOnSuccess(res -> log.info("Скоринг заявки {} успешно завершен со статусом: {}",
                        applicationId, res.getBody() != null ? res.getBody().getStatus() : "UNKNOWN"));
    }
}
