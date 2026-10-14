package kz.kaspi.credit.contracts.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IinValidatorTest {

    @Test
    @DisplayName("Должен успешно валидировать корректный реальный ИИН")
    void shouldValidateCorrectIin() {
        // Пример реального ИИН с валидной контрольной суммой: 950315350127
        // 95 год, 03 месяц, 15 число, 3 - мужчина XX века, контрольный разряд = 7
        assertTrue(IinValidator.isValid("950315350127"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "950315350124", // Неверная контрольная сумма (4 вместо 7)
            "951335350127", // Неверный месяц (13)
            "950231350127", // 31 февраля не существует
            "950315050127", // Неверный век/пол (0)
            "95031535012",   // 11 цифр (слишком короткий)
            "9503153501279", // 13 цифр (слишком длинный)
            "95031535012A", // Буква вместо цифры
            "",             // Пустая строка
    })
    @DisplayName("Должен отклонять невалидные ИИН")
    void shouldRejectInvalidIin(String invalidIin) {
        assertFalse(IinValidator.isValid(invalidIin));
    }
}
