package kz.kaspi.credit.agreement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credit_agreements")
public class CreditAgreement {

    @Id
    private UUID id;
    private UUID applicationId;
    private String iin;
    private String productType;
    private BigDecimal approvedAmount;
    private Integer termMonths;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
