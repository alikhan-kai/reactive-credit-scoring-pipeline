package kz.kaspi.credit.contracts.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final String errorCode;
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.errorCode = "RATE_LIMIT_EXCEEDED";
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
