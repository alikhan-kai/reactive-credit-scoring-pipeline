package kz.kaspi.credit.contracts.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientAgeUtilsTest {

    @Test
    @DisplayName("Должен корректно извлекать дату рождения и возраст из ИИН")
    void shouldExtractBirthDateAndCalculateAge() {
        // ИИН мужчины, родившегося 15 марта 1995 года
        String iin = "950315350127";

        LocalDate birthDate = ClientAgeUtils.extractBirthDate(iin);
        assertNotNull(birthDate);
        assertEquals(LocalDate.of(1995, 3, 15), birthDate);

        int age = ClientAgeUtils.calculateAge(iin);
        assertTrue(age >= 18);
        assertTrue(ClientAgeUtils.isEligibleAge(iin));
    }

    @Test
    @DisplayName("Должен возвращать null для невалидного ИИН")
    void shouldReturnNullForInvalidIin() {
        assertNull(ClientAgeUtils.extractBirthDate("invalid_iin"));
        assertEquals(-1, ClientAgeUtils.calculateAge("invalid_iin"));
        assertFalse(ClientAgeUtils.isEligibleAge("invalid_iin"));
    }
}
