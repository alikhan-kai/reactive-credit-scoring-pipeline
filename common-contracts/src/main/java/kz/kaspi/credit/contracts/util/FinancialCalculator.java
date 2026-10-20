package kz.kaspi.credit.contracts.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialCalculator {

    private static final int DEFAULT_SCALE = 2;

    private FinancialCalculator() {
        // Утилитный класс
    }

    /**
     * Расчет ежемесячного аннуитетного платежа.
     * Для рассрочки (ставка 0%) платеж равен: сумма / срок.
     * Для кредита с процентной ставкой: A = S * (r * (1 + r)^n) / ((1 + r)^n - 1)
     */
    public static BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRatePercent, int termMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || termMonths <= 0) {
            return BigDecimal.ZERO;
        }

        if (annualRatePercent == null || annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), DEFAULT_SCALE, RoundingMode.HALF_UP);
        }

        // Месячная процентная ставка: annualRatePercent / 100 / 12
        double monthlyRate = annualRatePercent.doubleValue() / 100.0 / 12.0;
        double factor = Math.pow(1.0 + monthlyRate, termMonths);
        double annuityRatio = (monthlyRate * factor) / (factor - 1.0);

        return principal.multiply(BigDecimal.valueOf(annuityRatio))
                .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Расчет коэффициента долговой нагрузки (КДН / DTI):
     * DTI = (текущие_платежи + новый_платеж) / среднемесячный_доход
     */
    public static BigDecimal calculateDti(BigDecimal currentMonthlyDebts, BigDecimal newMonthlyPayment, BigDecimal verifiedMonthlyIncome) {
        if (verifiedMonthlyIncome == null || verifiedMonthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE; // 100% нагрузка при отсутствии дохода
        }

        BigDecimal totalDebt = (currentMonthlyDebts != null ? currentMonthlyDebts : BigDecimal.ZERO)
                .add(newMonthlyPayment != null ? newMonthlyPayment : BigDecimal.ZERO);

        return totalDebt.divide(verifiedMonthlyIncome, 4, RoundingMode.HALF_UP);
    }
}
