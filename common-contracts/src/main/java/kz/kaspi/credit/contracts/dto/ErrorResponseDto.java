package kz.kaspi.credit.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String errorCode;
    private String message;
    private String path;
    private int status;
    private Instant timestamp;
    private List<ValidationErrorItem> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationErrorItem {
        private String field;
        private String message;
    }
}
