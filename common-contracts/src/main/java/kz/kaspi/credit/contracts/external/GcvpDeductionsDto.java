package kz.kaspi.credit.contracts.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcvpDeductionsDto {

    private String iin;
    private List<MonthlyDeduction> monthlyDeductions;
    private BigDecimal calculatedAverageMonthlyIncome; // Среднемесячный подтвержденный доход (ОПВ / 0.10)
    private int consecutiveMonthsCount; // Количество месяцев непрерывных отчислений
    private boolean hasIrregularGaps; // Наличие пропусков в пенсионных отчислениях

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyDeduction {
        private YearMonth yearMonth;
        private BigDecimal opvAmount; // Обязательные пенсионные взносы (10%)
        private String employerBin;   // БИН работодателя
        private String employerName;  // Наименование организации
    }
}
