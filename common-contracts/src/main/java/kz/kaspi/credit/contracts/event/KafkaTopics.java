package kz.kaspi.credit.contracts.event;

public final class KafkaTopics {

    private KafkaTopics() {
        // Константный класс
    }

    /** Топик принятых скоринговых решений (одобрено / отклонено) */
    public static final String CREDIT_DECISIONS_TOPIC = "credit.decisions";
    public static final String CREDIT_DECISIONS_DLQ_TOPIC = "credit.decisions.DLQ";
    public static final String CREDIT_COMPENSATIONS_TOPIC = "credit.compensations";
}
