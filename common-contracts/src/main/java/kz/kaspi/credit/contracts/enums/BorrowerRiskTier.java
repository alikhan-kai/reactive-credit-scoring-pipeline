package kz.kaspi.credit.contracts.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum BorrowerRiskTier {

    PRIME("Премиальный заемщик с высоким скорингом (ПКБ 720+)", new BigDecimal("3000000")),
    STANDARD("Стандартный заемщик с умеренным риском (ПКБ 600-719)", new BigDecimal("1500000")),
    SUBPRIME("Повышенный риск с ограниченным лимитом (ПКБ 500-599)", new BigDecimal("500000")),
    HIGH_RISK("Высокий уровень риска (ПКБ < 500)", BigDecimal.ZERO);

    private final String description;
    private final BigDecimal maxCreditLimit;

    public static BorrowerRiskTier fromScore(int creditScore) {
        if (creditScore >= 720) {
            return PRIME;
        } else if (creditScore >= 600) {
            return STANDARD;
        } else if (creditScore >= 500) {
            return SUBPRIME;
        } else {
            return HIGH_RISK;
        }
    }
}
