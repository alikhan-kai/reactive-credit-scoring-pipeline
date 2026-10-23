package kz.kaspi.credit.gateway.controller;

import kz.kaspi.credit.contracts.dto.ErrorResponseDto;
import kz.kaspi.credit.contracts.exception.IdempotencyConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleIdempotencyConflict(
            IdempotencyConflictException ex, ServerWebExchange exchange) {
        log.warn("Idempotency conflict: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        List<ErrorResponseDto.ValidationErrorItem> validationErrors = ex.getFieldErrors().stream()
                .map(err -> ErrorResponseDto.ValidationErrorItem.builder()
                        .field(err.getField())
                        .message(err.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("VALIDATION_FAILED")
                .message("Ошибка валидации входных данных заявки")
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}