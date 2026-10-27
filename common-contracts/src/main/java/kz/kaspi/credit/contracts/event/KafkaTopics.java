package kz.kaspi.credit.contracts.event;

public final class KafkaTopics {

    private KafkaTopics() {
        // Константы топиков
    }

    public static final String CREDIT_DECISIONS_TOPIC = "credit.decisions";
    public static final String CREDIT_DECISIONS_DLT_TOPIC = "credit.decisions.DLT";
    public static final String CREDIT_COMPENSATIONS_TOPIC = "credit.compensations";
}