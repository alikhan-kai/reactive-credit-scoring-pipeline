package kz.kaspi.credit.agreement.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
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
public class PaymentScheduleItem {

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
}
