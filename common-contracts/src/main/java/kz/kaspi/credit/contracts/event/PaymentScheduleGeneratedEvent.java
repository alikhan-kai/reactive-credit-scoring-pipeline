package kz.kaspi.credit.contracts.event;

import kz.kaspi.credit.contracts.dto.PaymentScheduleItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentScheduleGeneratedEvent {

    private UUID agreementId;
    private UUID applicationId;
    private String iin;
    private BigDecimal totalAmount;
    private int termMonths;
    private List<PaymentScheduleItemDto> scheduleItems;
    private Instant generatedAt;
}
