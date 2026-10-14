package kz.kaspi.credit.contracts.event;

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
public class CreditRejectedEvent {

    private UUID applicationId;
    private String iin;
    private CreditProductType productType;
    private BigDecimal requestedAmount;
    private Integer termMonths;
    private String rejectionCode;
    private String rejectionReason;
    private Instant rejectedAt;
}
