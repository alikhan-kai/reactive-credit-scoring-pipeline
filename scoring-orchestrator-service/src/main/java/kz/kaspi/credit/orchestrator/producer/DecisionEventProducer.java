package kz.kaspi.credit.orchestrator.producer;

import kz.kaspi.credit.contracts.dto.CreditApplicationResponse;
import kz.kaspi.credit.contracts.enums.ApplicationStatus;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.CreditRejectedEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DecisionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<CreditApplicationResponse> publishDecision(CreditApplicationResponse response) {
        return Mono.fromFuture(() -> {
            String key = response.getApplicationId().toString();

            if (response.getStatus() == ApplicationStatus.APPROVED
                    || response.getStatus() == ApplicationStatus.COUNTER_OFFER) {
                CreditApprovedEvent approvedEvent = CreditApprovedEvent.builder()
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

                log.info("Публикация события одобрения кредита в Kafka [топик {}]: {}",
                        KafkaTopics.CREDIT_DECISIONS_TOPIC, response.getApplicationId());
                return kafkaTemplate.send(KafkaTopics.CREDIT_DECISIONS_TOPIC, key, approvedEvent);

            } else {
                CreditRejectedEvent rejectedEvent = CreditRejectedEvent.builder()
                        .applicationId(response.getApplicationId())
                        .iin(response.getIin())
                        .productType(response.getProductType())
                        .requestedAmount(response.getRequestedAmount())
                        .rejectionCode("SCORING_REJECTED")
                        .rejectionReason(response.getRejectionReason())
                        .rejectedAt(Instant.now())
                        .build();

                log.info("Публикация события отказа в кредите в Kafka [топик {}]: {}",
                        KafkaTopics.CREDIT_DECISIONS_TOPIC, response.getApplicationId());
                return kafkaTemplate.send(KafkaTopics.CREDIT_DECISIONS_TOPIC, key, rejectedEvent);
            }
        }).thenReturn(response);
    }
}