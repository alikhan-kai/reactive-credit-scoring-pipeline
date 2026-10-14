package kz.kaspi.credit.contracts.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IinConstraintValidator implements ConstraintValidator<ValidIin, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return IinValidator.isValid(value);
    }
}
