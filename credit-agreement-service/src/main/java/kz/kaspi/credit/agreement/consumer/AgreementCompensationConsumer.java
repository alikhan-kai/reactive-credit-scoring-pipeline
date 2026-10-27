package kz.kaspi.credit.agreement.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kaspi.credit.agreement.service.AgreementService;
import kz.kaspi.credit.contracts.event.CreditCompensationEvent;
import kz.kaspi.credit.contracts.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgreementCompensationConsumer {

    private final AgreementService agreementService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.CREDIT_COMPENSATIONS_TOPIC, groupId = "credit-agreement-compensation-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleCompensation(ConsumerRecord<String, Object> record) {
        Object value = record.value();
        try {
            CreditCompensationEvent compensationEvent = null;
            if (value instanceof CreditCompensationEvent directEvent) {
                compensationEvent = directEvent;
            } else if (value instanceof String json) {
                compensationEvent = objectMapper.readValue(json, CreditCompensationEvent.class);
            }

            if (compensationEvent != null) {
                log.warn("Получен сигнал Saga компенсации: отмена договора для заявки {}, причина: {}",
                        compensationEvent.getApplicationId(), compensationEvent.getReason());

                agreementService.cancelAgreement(compensationEvent.getApplicationId(), compensationEvent.getReason())
                        .subscribe();
            }
        } catch (Exception e) {
            log.error("Ошибка при разборе события компенсации {}: {}", record.key(), e.getMessage());
        }
    }
}