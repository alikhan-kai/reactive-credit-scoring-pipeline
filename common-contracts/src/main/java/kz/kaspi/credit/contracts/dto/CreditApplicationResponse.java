package kz.kaspi.credit.contracts.dto;

import kz.kaspi.credit.contracts.enums.ApplicationStatus;
import kz.kaspi.credit.contracts.enums.BorrowerRiskTier;
import kz.kaspi.credit.contracts.enums.CreditProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationResponse {

    private UUID applicationId;
    private String iin;
    private ApplicationStatus status;
    private CreditProductType productType;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private Integer approvedTermMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal interestRate;
    private BigDecimal calculatedDti; // КДН (Коэффициент долговой нагрузки)
    private Integer creditScore;
    private BorrowerRiskTier borrowerTier; // Сегмент заемщика по скорингу
    private String rejectionReason;
    private Instant decisionTimestamp;
}
