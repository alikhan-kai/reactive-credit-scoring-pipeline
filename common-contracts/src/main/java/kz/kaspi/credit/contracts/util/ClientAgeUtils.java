package kz.kaspi.credit.contracts.util;

import kz.kaspi.credit.contracts.validation.IinValidator;

import java.time.LocalDate;
import java.time.Period;

public final class ClientAgeUtils {

    private ClientAgeUtils() {
        // Утилитный класс
    }

    /**
     * Извлекает дату рождения из валидного ИИН РК.
     * Возвращает null, если ИИН некорректен.
     */
    public static LocalDate extractBirthDate(String iin) {
        if (!IinValidator.isValid(iin)) {
            return null;
        }

        int centuryGender = Character.getNumericValue(iin.charAt(6));
        int yearPrefix = switch (centuryGender) {
            case 1, 2 -> 1800;
            case 3, 4 -> 1900;
            case 5, 6 -> 2000;
            default -> 0;
        };

        int year = yearPrefix + Integer.parseInt(iin.substring(0, 2));
        int month = Integer.parseInt(iin.substring(2, 4));
        int day = Integer.parseInt(iin.substring(4, 6));

        try {
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Рассчитывает полный возраст заемщика в годах на текущий момент.
     */
    public static int calculateAge(String iin) {
        LocalDate birthDate = extractBirthDate(iin);
        if (birthDate == null) {
            return -1;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Проверяет соответствие заемщика возрастным нормативам кредитования (от 18 до 70 лет).
     */
    public static boolean isEligibleAge(String iin) {
        int age = calculateAge(iin);
        return age >= 18 && age <= 70;
    }
}
