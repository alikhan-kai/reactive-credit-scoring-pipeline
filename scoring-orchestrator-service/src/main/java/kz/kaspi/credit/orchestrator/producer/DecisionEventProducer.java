package kz.kaspi.credit.orchestrator.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kaspi.credit.contracts.dto.CreditApplicationResponse;
import kz.kaspi.credit.contracts.enums.ApplicationStatus;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.CreditRejectedEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import kz.kaspi.credit.orchestrator.domain.OutboxEvent;
import kz.kaspi.credit.orchestrator.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DecisionEventProducer {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public Mono<CreditApplicationResponse> publishDecision(CreditApplicationResponse response) {
        return Mono.defer(() -> {
            String key = response.getApplicationId().toString();
            Object eventPayload;

            if (response.getStatus() == ApplicationStatus.APPROVED
                    || response.getStatus() == ApplicationStatus.COUNTER_OFFER) {
                eventPayload = CreditApprovedEvent.builder()
                        .applicationId(response.getApplicationId())
                        .iin(response.getIin())
                        .productType(response.getProductType())
                        .approvedAmount(response.getApprovedAmount())
                        .termMonths(response.getApprovedTermMonths())
                        .interestRate(response.getInterestRate())
                        .monthlyPayment(response.getMonthlyPayment())
                        .creditScore(response.getCreditScore())
                        .dti(response.getCalculatedDti())
                        .approvedAt(Instant.now())
                        .build();
            } else {
                eventPayload = CreditRejectedEvent.builder()
                        .applicationId(response.getApplicationId())
                        .iin(response.getIin())
                        .productType(response.getProductType())
                        .requestedAmount(response.getRequestedAmount())
                        .rejectionCode("SCORING_REJECTED")
                        .rejectionReason(response.getRejectionReason())
                        .rejectedAt(Instant.now())
                        .build();
            }

            try {
                String json = objectMapper.writeValueAsString(eventPayload);
                OutboxEvent outboxEvent = OutboxEvent.builder()
                        .id(UUID.randomUUID())
                        .aggregateType("CreditDecision")
                        .aggregateId(key)
                        .topic(KafkaTopics.CREDIT_DECISIONS_TOPIC)
                        .payload(json)
                        .status("PENDING")
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .isNewRecord(true)
                        .build();

                log.info("Сохранение события скоринга в Outbox [заявка {}]", key);
                return outboxEventRepository.save(outboxEvent)
                        .thenReturn(response);

            } catch (JsonProcessingException e) {
                log.error("Ошибка сериализации события скоринга для заявки {}: {}", key, e.getMessage());
                return Mono.error(e);
            }
        });
    }
}