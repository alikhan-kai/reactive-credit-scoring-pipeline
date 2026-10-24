package kz.kaspi.credit.audit.consumer;

import kz.kaspi.credit.audit.service.AuditService;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.CreditRejectedEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditDecisionConsumer {

    private final AuditService auditService;

    @KafkaListener(topics = KafkaTopics.CREDIT_DECISIONS_TOPIC, groupId = "audit-notification-group", containerFactory = "auditKafkaListenerContainerFactory")
    public void handleDecision(Object event) {
        if (event instanceof CreditApprovedEvent approved) {
            auditService.processApproval(approved);
        } else if (event instanceof CreditRejectedEvent rejected) {
            auditService.processRejection(rejected);
        } else {
            log.warn("Неизвестный тип события в топике решений: {}",
                    event != null ? event.getClass().getName() : "null");
        }
    }
}