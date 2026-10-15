package kz.kaspi.credit.externalmock.domain;

public enum BorrowerArchetype {
    PRIME("Идеальный заемщик: высокий скор (800+), стабильный высокий доход, без просрочек"),
    AVERAGE("Стандартный клиент: средний доход, скор 650-750, мелкие закрытые просрочки"),
    OVERLEVERAGED("Закредитованный клиент: высокая кредитная нагрузка, КДН превышает 50%"),
    DEFAULTER("Злостный неплательщик: критические просрочки DPD 90+, низкий кредитный скор"),
    LEGAL_BLOCK("Ограничения Минюста: действующие аресты счетов судебными исполнителями"),
    CHAOS_TIMEOUT("Симуляция сбоя: искусственная задержка и таймаут для проверки Circuit Breaker");

    private final String description;

    BorrowerArchetype(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
