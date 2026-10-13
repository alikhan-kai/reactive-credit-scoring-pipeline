package kz.kaspi.credit.contracts.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public final class IinValidator {

    private static final int[] WEIGHTS_1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1 };
    private static final int[] WEIGHTS_2 = { 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2 };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);

    private IinValidator() {
        // Utility class
    }

    public static boolean isValid(String iin) {
        if (iin == null || iin.length() != 12 || !iin.chars().allMatch(Character::isDigit)) {
            return false;
        }

        int[] digits = new int[12];
        for (int i = 0; i < 12; i++) {
            digits[i] = Character.getNumericValue(iin.charAt(i));
        }

        // 1. Проверка века и пола (7-я цифра: от 1 до 6)
        int centuryGender = digits[6];
        if (centuryGender < 1 || centuryGender > 6) {
            return false;
        }

        // 2. Проверка валидности даты рождения
        if (!isDateValid(iin, centuryGender)) {
            return false;
        }

        // 3. Проверка контрольного разряда (12-я цифра)
        return checkControlDigit(digits);
    }

    private static boolean isDateValid(String iin, int centuryGender) {
        String yearPrefix;
        switch (centuryGender) {
            case 1, 2 -> yearPrefix = "18";
            case 3, 4 -> yearPrefix = "19";
            case 5, 6 -> yearPrefix = "20";
            default -> {
                return false;
            }
        }

        String fullDateStr = yearPrefix + iin.substring(0, 6);
        try {
            LocalDate birthDate = LocalDate.parse(fullDateStr, DATE_FORMATTER);
            return !birthDate.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkControlDigit(int[] digits) {
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

        if (control == 10) {
            return false;
        }

        return control == digits[11];
    }
}