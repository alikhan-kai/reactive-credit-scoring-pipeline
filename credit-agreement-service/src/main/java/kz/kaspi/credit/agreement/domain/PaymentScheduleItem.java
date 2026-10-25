package kz.kaspi.credit.agreement.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payment_schedule_items")
public class PaymentScheduleItem implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID agreementId;
    private Integer paymentNumber;
    private LocalDate paymentDate;
    private BigDecimal monthlyPayment;
    private BigDecimal principalPayment;
    private BigDecimal interestPayment;
    private BigDecimal remainingDebt;
    private Instant createdAt;

    @Transient
    @Builder.Default
    private boolean isNewRecord = true;

    @Override
    public boolean isNew() {
        return isNewRecord;
    }
}
