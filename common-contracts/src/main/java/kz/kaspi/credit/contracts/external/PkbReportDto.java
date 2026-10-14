package kz.kaspi.credit.contracts.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PkbReportDto {

    private String iin;
    private int creditScore; // 0 - 1000
    private int activeCreditsCount;
    private BigDecimal totalMonthlyPayment;
    private BigDecimal totalOutstandingDebt;
    private int maxDpdLast12Months; // Days Past Due (0, 30, 60, 90+)
    private boolean firstPaymentDefault; // FPD (дефолт по первому платежу)
    private Instant reportGeneratedAt;
}
