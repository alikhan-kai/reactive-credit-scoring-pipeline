package kz.kaspi.credit.contracts.event;

public final class KafkaTopics {

    private KafkaTopics() {
        // Константный класс
    }

    /** Топик принятых скоринговых решений (одобрено / отклонено) */
    public static final String CREDIT_DECISIONS_TOPIC = "credit.decisions";

    /** Топик сформированных графиков платежей */
    public static final String PAYMENT_SCHEDULES_TOPIC = "payment.schedules";

    /** Dead Letter Topic (DLT) для сбойных и невалидных сообщений */
    public static final String CREDIT_DECISIONS_DLT_TOPIC = "credit.decisions.dlt";
}
