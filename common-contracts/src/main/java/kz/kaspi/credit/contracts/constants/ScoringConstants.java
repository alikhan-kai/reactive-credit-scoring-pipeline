package kz.kaspi.credit.contracts.constants;

import java.math.BigDecimal;

public final class ScoringConstants {

    private ScoringConstants() {
        // Константный класс
    }

    /** Максимально допустимый коэффициент долговой нагрузки (КДН) по нормативу АРРФР (50%) */
    public static final BigDecimal MAX_ALLOWED_DTI = new BigDecimal("0.50");

    /** Ставка обязательных пенсионных взносов (ОПВ) в ГЦВП (10%) */
    public static final BigDecimal OPV_RATE = new BigDecimal("0.10");

    /** Минимальная сумма заявки (10 000 KZT) */
    public static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("10000.00");

    /** Максимальная сумма заявки (3 000 000 KZT) */
    public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("3000000.00");

    /** Минимальный срок кредитования (месяцев) */
    public static final int MIN_TERM_MONTHS = 3;

    /** Максимальный срок кредитования (месяцев) */
    public static final int MAX_TERM_MONTHS = 24;

    /** Годовая процентная переплата по рассрочке Kaspi 0-0-12 (0%) */
    public static final BigDecimal INSTALLMENT_INTEREST_RATE = BigDecimal.ZERO;
}
