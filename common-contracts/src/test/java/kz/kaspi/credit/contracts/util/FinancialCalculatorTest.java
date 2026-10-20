package kz.kaspi.credit.contracts.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialCalculatorTest {

    @Test
    @DisplayName("Рассрочка 0-0-12 на 120 000 KZT должна делить сумму поровну (10 000 KZT)")
    void shouldCalculateInstallmentMonthlyPayment() {
        BigDecimal payment = FinancialCalculator.calculateMonthlyPayment(
                new BigDecimal("120000.00"),
                BigDecimal.ZERO,
                12
        );
        assertEquals(new BigDecimal("10000.00"), payment);
    }

    @Test
    @DisplayName("Аннуитет с процентной ставкой должен корректно рассчитываться")
    void shouldCalculateAnnuityPaymentWithInterest() {
        BigDecimal payment = FinancialCalculator.calculateMonthlyPayment(
                new BigDecimal("100000.00"),
                new BigDecimal("24.0"), // 24% годовых
                12
        );
        // При 100k, 24% годовых (2% в мес), n=12 платеж около 9455.96 KZT
        assertEquals(new BigDecimal("9455.96"), payment);
    }

    @Test
    @DisplayName("Расчет КДН (DTI) должен корректно отражать нагрузку")
    void shouldCalculateDtiCorrectly() {
        // Доход: 400 000 KZT, текущие кредиты: 50 000 KZT, новый платеж: 50 000 KZT -> КДН = 25% (0.2500)
        BigDecimal dti = FinancialCalculator.calculateDti(
                new BigDecimal("50000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("400000.00")
        );
        assertEquals(new BigDecimal("0.2500"), dti);
    }
}
