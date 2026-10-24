package kz.kaspi.credit.agreement.consumer;

import kz.kaspi.credit.agreement.service.AgreementService;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditDecisionConsumer {

    private final AgreementService agreementService;

    @KafkaListener(topics = KafkaTopics.CREDIT_DECISIONS_TOPIC, groupId = "credit-agreement-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleCreditDecision(Object event, Acknowledgment ack) {
        if (event instanceof CreditApprovedEvent approvedEvent) {
            log.info("Получено событие одобрения заявки {} для ИИН {}",
                    approvedEvent.getApplicationId(), approvedEvent.getIin());

            agreementService.createAgreementWithSchedule(approvedEvent)
                    .doOnSuccess(agreement -> {
                        log.info("Договор {} успешно оформлен из события Kafka", agreement.getId());
                        ack.acknowledge(); // Фиксируем смещение в Kafka только после успешной транзакции
                    })
                    .doOnError(e -> log.error("Ошибка при обработке одобренной заявки {}: {}",
                            approvedEvent.getApplicationId(), e.getMessage()))
                    .subscribe();
        } else {
            // Отклоненные заявки договорной сервис пропускает, сразу подтверждая смещение
            ack.acknowledge();
        }
    }
}