package kz.kaspi.credit.audit.service;

import kz.kaspi.credit.contracts.event.CreditApprovedEvent;
import kz.kaspi.credit.contracts.event.CreditRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditService {

    /**
     * Аудит и нотификация при одобрении заявки
     */
    public void processApproval(CreditApprovedEvent event) {
        // 1. Логирование регуляторного аудита для АРРФР
        log.info("[РЕГУЛЯТОРНЫЙ АУДИТ | ОДОБРЕНО] Заявка {}, ИИН {}, Продукт {}, Одобрено {} KZT на {} мес., " +
                "Платеж {} KZT/мес, Ставка {}%, КДН {}, Скор ПКБ {}",
                event.getApplicationId(),
                maskIin(event.getIin()),
                event.getProductType(),
                event.getApprovedAmount(),
                event.getTermMonths(),
                event.getMonthlyPayment(),
                event.getInterestRate(),
                event.getDti(),
                event.getCreditScore());

        // 2. Симуляция отправки Push-уведомления клиенту в приложение Kaspi
        sendPushNotification(event.getIin(), String.format(
                "Вам одобрен(а) %s на сумму %s KZT! Ежемесячный платеж: %s KZT.",
                event.getProductType().getDescription(),
                event.getApprovedAmount(),
                event.getMonthlyPayment()));
    }

    /**
     * Аудит и нотификация при отказе по заявке
     */
    public void processRejection(CreditRejectedEvent event) {
        // 1. Логирование регуляторного аудита отказа
        log.warn("[РЕГУЛЯТОРНЫЙ АУДИТ | ОТКАЗ] Заявка {}, ИИН {}, Код отказа: {}, Причина: '{}'",
                event.getApplicationId(),
                maskIin(event.getIin()),
                event.getRejectionCode(),
                event.getRejectionReason());

        // 2. Симуляция отправки Push-уведомления об отказе
        sendPushNotification(event.getIin(),
                "К сожалению, банк не может одобрить заявку в данный момент. " + event.getRejectionReason());
    }

    private void sendPushNotification(String iin, String message) {
        log.info("[Kaspi Push Service] Отправка Push клиенту {}: \"{}\"", maskIin(iin), message);
    }

    private String maskIin(String iin) {
        if (iin == null || iin.length() != 12)
            return "******";
        return iin.substring(0, 4) + "****" + iin.substring(8);
    }
}