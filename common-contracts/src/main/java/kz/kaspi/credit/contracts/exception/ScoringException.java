package kz.kaspi.credit.contracts.exception;

public class ScoringException extends RuntimeException {

    private final String errorCode;

    public ScoringException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ScoringException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
