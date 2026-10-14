package kz.kaspi.credit.contracts.enums;

public enum CreditProductType {
    INSTALLMENT_0_0_12("Рассрочка Kaspi 0-0-12 (0% переплаты)"),
    INSTALLMENT_0_0_24("Рассрочка Kaspi 0-0-24 (Kaspi Жұма)"),
    EXPRESS_CREDIT("Кредит на покупки / наличными с аннуитетной ставкой");

    private final String description;

    CreditProductType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
