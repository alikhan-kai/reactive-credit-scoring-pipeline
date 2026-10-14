package kz.kaspi.credit.contracts.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kz.kaspi.credit.contracts.enums.CreditProductType;
import kz.kaspi.credit.contracts.validation.ValidIin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationRequest {

    @NotBlank(message = "ИИН обязателен для заполнения")
    @ValidIin
    private String iin;

    @NotNull(message = "Запрашиваемая сумма обязательна")
    @DecimalMin(value = "10000.00", message = "Минимальная сумма заявки - 10 000 KZT")
    @DecimalMax(value = "3000000.00", message = "Максимальная сумма заявки - 3 000 000 KZT")
    private BigDecimal requestedAmount;

    @NotNull(message = "Срок кредитования обязателен")
    @Min(value = 3, message = "Минимальный срок - 3 месяца")
    @Max(value = 24, message = "Максимальный срок - 24 месяца")
    private Integer termMonths;

    @NotNull(message = "Тип кредитного продукта обязателен")
    private CreditProductType productType;

    @Pattern(regexp = "^\\+77\\d{9}$", message = "Номер телефона должен соответствовать формату +77XXXXXXXXX")
    private String phoneNumber;
}
