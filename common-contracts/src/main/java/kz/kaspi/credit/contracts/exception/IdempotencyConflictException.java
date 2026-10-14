package kz.kaspi.credit.contracts.exception;

public class IdempotencyConflictException extends ScoringException {

    public IdempotencyConflictException(String idempotencyKey) {
        super(
                "IDEMPOTENCY_CONFLICT",
                "Запрос с ключом идемпотентности '" + idempotencyKey + "' уже обрабатывается или был обработан ранее"
        );
    }
}
