package kz.kaspi.credit.externalmock.util;

import kz.kaspi.credit.contracts.validation.IinValidator;
import kz.kaspi.credit.externalmock.domain.BorrowerArchetype;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IinGeneratorTest {

    @ParameterizedTest
    @EnumSource(BorrowerArchetype.class)
    @DisplayName("Сгенерированные ИИН для всех архетипов должны проходить валидацию ИИН РК")
    void shouldGenerateValidIinForAllArchetypes(BorrowerArchetype archetype) {
        for (int i = 0; i < 10; i++) {
            String iin = IinGenerator.generateForArchetype(archetype, i);
            assertNotNull(iin);
            assertTrue(IinValidator.isValid(iin), "Сгенерированный ИИН должен быть валидным: " + iin);
        }
    }

    @Test
    @DisplayName("Генератор должен создавать детерминированные уникальные ИИН при разных индексах")
    void shouldGenerateUniqueIinForDifferentIndices() {
        String iin1 = IinGenerator.generateForArchetype(BorrowerArchetype.PRIME, 1);
        String iin2 = IinGenerator.generateForArchetype(BorrowerArchetype.PRIME, 2);
        assertTrue(IinValidator.isValid(iin1));
        assertTrue(IinValidator.isValid(iin2));
        org.junit.jupiter.api.Assertions.assertNotEquals(iin1, iin2);
    }
}
