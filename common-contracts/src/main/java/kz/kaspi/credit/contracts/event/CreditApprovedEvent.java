package kz.kaspi.credit.contracts.event;

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
public class CreditApprovedEvent {

    private UUID applicationId;
    private String iin;
    private CreditProductType productType;
    private BigDecimal approvedAmount;
    private Integer termMonths;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private Integer creditScore;
    private BorrowerRiskTier borrowerTier;
    private BigDecimal dti;
    private Instant approvedAt;
}
