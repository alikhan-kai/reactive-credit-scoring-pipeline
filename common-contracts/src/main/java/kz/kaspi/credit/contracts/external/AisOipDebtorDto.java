package kz.kaspi.credit.contracts.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AisOipDebtorDto {

    private String iin;
    private boolean isDebtor; // Включен ли в Единый реестр должников Минюста
    private boolean hasActiveAccountArrest; // Наличие арестов на банковские счета
    private boolean hasTravelBan; // Наличие запрета на выезд из Республики Казахстан
    private BigDecimal totalBailiffDebt; // Сумма задолженности по исполнительным производствам
    private int activeProceedingsCount; // Количество активных производств ЧСИ
}
