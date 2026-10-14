package kz.kaspi.credit.contracts.exception;

public class ExternalServiceTimeoutException extends ScoringException {

    public ExternalServiceTimeoutException(String serviceName, long timeoutMs) {
        super(
                "EXTERNAL_SERVICE_TIMEOUT",
                "Превышен лимит времени ожидания ответа от внешнего сервиса '" + serviceName + "' (" + timeoutMs + " мс)"
        );
    }
}
