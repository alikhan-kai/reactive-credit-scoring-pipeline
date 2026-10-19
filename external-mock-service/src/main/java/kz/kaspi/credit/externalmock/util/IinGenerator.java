package kz.kaspi.credit.externalmock.util;

import kz.kaspi.credit.contracts.validation.IinValidator;
import kz.kaspi.credit.externalmock.domain.BorrowerArchetype;

import java.time.LocalDate;

public final class IinGenerator {

    private static final int[] WEIGHTS_1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1};
    private static final int[] WEIGHTS_2 = {3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2};

    private IinGenerator() {
        // Утилитный класс
    }

    /**
     * Генерирует валидный ИИН для заданного архетипа заемщика.
     * Дата рождения кодирует тип заемщика для детерминированного маппинга.
     */
    public static String generateForArchetype(BorrowerArchetype archetype, int index) {
        LocalDate birthDate = switch (archetype) {
            case PRIME -> LocalDate.of(1990, 1, 15);
            case AVERAGE -> LocalDate.of(1992, 5, 20);
            case OVERLEVERAGED -> LocalDate.of(1994, 8, 10);
            case DEFAULTER -> LocalDate.of(1988, 11, 25);
            case LEGAL_BLOCK -> LocalDate.of(1985, 3, 30);
            case CHAOS_TIMEOUT -> LocalDate.of(1999, 12, 12);
        };

        // 3 - мужчина XX века, 4 - женщина XX века
        int genderCentury = 3;
        int sequenceNumber = 100 + (index % 800);

        return generateValidIin(birthDate, genderCentury, sequenceNumber);
    }

    public static String generateValidIin(LocalDate birthDate, int genderCentury, int sequenceNumber) {
        String yearStr = String.format("%02d", birthDate.getYear() % 100);
        String monthStr = String.format("%02d", birthDate.getMonthValue());
        String dayStr = String.format("%02d", birthDate.getDayOfMonth());
        String seqStr = String.format("%04d", sequenceNumber);

        String first11 = yearStr + monthStr + dayStr + genderCentury + seqStr;

        int[] digits = new int[12];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(first11.charAt(i));
        }

        int control = calculateControlDigit(digits);
        if (control == 10) {
            // Если остаток 10 в обоих рядах, подбираем следующий порядковый номер
            return generateValidIin(birthDate, genderCentury, sequenceNumber + 1);
        }

        String generated = first11 + control;
        if (!IinValidator.isValid(generated)) {
            return generateValidIin(birthDate, genderCentury, sequenceNumber + 1);
        }

        return generated;
    }

    private static int calculateControlDigit(int[] digits) {
        int sum1 = 0;
        for (int i = 0; i < 11; i++) {
            sum1 += digits[i] * WEIGHTS_1[i];
        }

        int control = sum1 % 11;
        if (control == 10) {
            int sum2 = 0;
            for (int i = 0; i < 11; i++) {
                sum2 += digits[i] * WEIGHTS_2[i];
            }
            control = sum2 % 11;
        }

        return control;
    }
}
