package kz.kaspi.credit.agreement.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kaspi.credit.agreement.service.AgreementService;
import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditDecisionConsumer {

    private final AgreementService agreementService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.CREDIT_DECISIONS_TOPIC, groupId = "credit-agreement-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleCreditDecision(ConsumerRecord<String, Object> record) {
        Object event = record.value();

        try {
            CreditApprovedEvent approvedEvent = null;
            if (event instanceof CreditApprovedEvent directEvent) {
                approvedEvent = directEvent;
            } else if (event instanceof String jsonString) {
                // Если событие пришло в виде JSON строки из Outbox
                if (jsonString.contains("approvedAmount")) {
                    approvedEvent = objectMapper.readValue(jsonString, CreditApprovedEvent.class);
                }
            }

            if (approvedEvent != null) {
                log.info("Обработка одобренной заявки {} для ИИН {}",
                        approvedEvent.getApplicationId(), approvedEvent.getIin());

                agreementService.createAgreementWithSchedule(approvedEvent)
                        .doOnSuccess(agreement -> log.info("Договор {} успешно оформлен из события Kafka",
                                agreement.getId()))
                        .doOnError(e -> log.error("Ошибка при сохранении договора для заявки {}: {}",
                                record.key(), e.getMessage()))
                        .block(); // Синхронизируем реактивный поток с контейнером Kafka для гарантированного
                                  // триггера Retry/DLQ
            }
        } catch (Exception e) {
            log.error("Фатальный сбой при обработке события договора {}: {}", record.key(), e.getMessage());
            throw new RuntimeException("Agreement processing failed for key: " + record.key(), e);
        }
    }
}