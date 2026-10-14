package kz.kaspi.credit.contracts.enums;

public enum ApplicationStatus {
    PENDING("Заявка в обработке скорингового пайплайна"),
    APPROVED("Заявка одобрена на запрошенных условиях"),
    COUNTER_OFFER("Предложен встречный лимит с уменьшенной суммой"),
    REJECTED("В выдаче кредита/рассрочки отказано");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
