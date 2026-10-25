package kz.kaspi.credit.agreement.consumer;

import kz.kaspi.credit.agreement.service.AgreementService;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditDecisionConsumer {

    private final AgreementService agreementService;

    @KafkaListener(topics = KafkaTopics.CREDIT_DECISIONS_TOPIC, groupId = "credit-agreement-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleCreditDecision(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        Object event = record.value();
        if (event instanceof CreditApprovedEvent approvedEvent) {
            log.info("Получено событие одобрения заявки {} для ИИН {}",
                    approvedEvent.getApplicationId(), approvedEvent.getIin());

            agreementService.createAgreementWithSchedule(approvedEvent)
                    .doOnSuccess(agreement -> {
                        log.info("Договор {} успешно оформлен из события Kafka", agreement.getId());
                        ack.acknowledge();
                    })
                    .doOnError(e -> {
                        log.error("Ошибка при обработке одобренной заявки {}: {}",
                                approvedEvent.getApplicationId(), e.getMessage());
                        ack.acknowledge();
                    })
                    .subscribe();
        } else {
            // Отклоненные заявки договорной сервис пропускает, сразу подтверждая смещение
            ack.acknowledge();
        }
    }
}
