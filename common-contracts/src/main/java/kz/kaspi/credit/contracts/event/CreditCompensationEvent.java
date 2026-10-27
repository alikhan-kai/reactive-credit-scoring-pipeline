package kz.kaspi.credit.contracts.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCompensationEvent {

    private UUID applicationId;
    private String iin;
    private String reason;
    private String failedService;
    private Instant timestamp;
}
