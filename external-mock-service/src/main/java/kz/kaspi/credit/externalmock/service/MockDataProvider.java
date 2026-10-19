package kz.kaspi.credit.externalmock.service;

import kz.kaspi.credit.contracts.external.AisOipDebtorDto;
import kz.kaspi.credit.contracts.external.GcvpDeductionsDto;
import kz.kaspi.credit.contracts.external.PkbReportDto;
import kz.kaspi.credit.externalmock.domain.BorrowerArchetype;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class MockDataProvider {

    public BorrowerArchetype resolveArchetype(String iin) {
        if (iin != null && iin.length() >= 6) {
            String prefix = iin.substring(0, 6);
            if ("900115".equals(prefix)) return BorrowerArchetype.PRIME;
            if ("881125".equals(prefix)) return BorrowerArchetype.DEFAULTER;
            if ("850330".equals(prefix)) return BorrowerArchetype.LEGAL_BLOCK;
            if ("940810".equals(prefix)) return BorrowerArchetype.OVERLEVERAGED;
            if ("991212".equals(prefix)) return BorrowerArchetype.CHAOS_TIMEOUT;
        }

        int bucket = Math.abs(iin != null ? iin.hashCode() : 0) % 10;
        return switch (bucket) {
            case 0, 1 -> BorrowerArchetype.PRIME;
            case 2, 3, 4, 5 -> BorrowerArchetype.AVERAGE;
            case 6, 7 -> BorrowerArchetype.OVERLEVERAGED;
            case 8 -> BorrowerArchetype.DEFAULTER;
            default -> BorrowerArchetype.LEGAL_BLOCK;
        };
    }

    public PkbReportDto getPkbReport(String iin) {
        BorrowerArchetype archetype = resolveArchetype(iin);

        int score = switch (archetype) {
            case PRIME -> 840;
            case AVERAGE -> 690;
            case OVERLEVERAGED -> 630;
            case DEFAULTER -> 390;
            case LEGAL_BLOCK -> 450;
            case CHAOS_TIMEOUT -> 710;
        };

        int activeCredits = switch (archetype) {
            case PRIME -> 0;
            case AVERAGE -> 1;
            case OVERLEVERAGED -> 4;
            case DEFAULTER -> 2;
            case LEGAL_BLOCK -> 1;
            case CHAOS_TIMEOUT -> 1;
        };

        BigDecimal monthlyPayment = switch (archetype) {
            case PRIME -> BigDecimal.ZERO;
            case AVERAGE -> new BigDecimal("45000.00");
            case OVERLEVERAGED -> new BigDecimal("145000.00");
            case DEFAULTER -> new BigDecimal("60000.00");
            case LEGAL_BLOCK -> new BigDecimal("30000.00");
            case CHAOS_TIMEOUT -> new BigDecimal("35000.00");
        };

        int maxDpd = switch (archetype) {
            case PRIME, AVERAGE, OVERLEVERAGED, CHAOS_TIMEOUT -> 0;
            case LEGAL_BLOCK -> 45;
            case DEFAULTER -> 95;
        };

        boolean fpd = (archetype == BorrowerArchetype.DEFAULTER);

        return PkbReportDto.builder()
                .iin(iin)
                .creditScore(score)
                .activeCreditsCount(activeCredits)
                .totalMonthlyPayment(monthlyPayment)
                .totalOutstandingDebt(monthlyPayment.multiply(BigDecimal.valueOf(10)))
                .maxDpdLast12Months(maxDpd)
                .firstPaymentDefault(fpd)
                .reportGeneratedAt(Instant.now())
                .build();
    }

    public GcvpDeductionsDto getGcvpDeductions(String iin) {
        BorrowerArchetype archetype = resolveArchetype(iin);

        BigDecimal baseSalary = switch (archetype) {
            case PRIME -> new BigDecimal("750000.00");
            case AVERAGE -> new BigDecimal("320000.00");
            case OVERLEVERAGED -> new BigDecimal("220000.00");
            case DEFAULTER -> new BigDecimal("150000.00");
            case LEGAL_BLOCK -> new BigDecimal("180000.00");
            case CHAOS_TIMEOUT -> new BigDecimal("300000.00");
        };

        BigDecimal monthlyOpv = baseSalary.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        List<GcvpDeductionsDto.MonthlyDeduction> deductions = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 1; i <= 6; i++) {
            deductions.add(GcvpDeductionsDto.MonthlyDeduction.builder()
                    .yearMonth(current.minusMonths(i))
                    .opvAmount(monthlyOpv)
                    .employerBin("010240001234")
                    .employerName("ТОО Тестовая Компания")
                    .build());
        }

        return GcvpDeductionsDto.builder()
                .iin(iin)
                .monthlyDeductions(deductions)
                .calculatedAverageMonthlyIncome(baseSalary)
                .consecutiveMonthsCount(6)
                .hasIrregularGaps(false)
                .build();
    }

    public AisOipDebtorDto getAisOipDebtor(String iin) {
        BorrowerArchetype archetype = resolveArchetype(iin);
        boolean isBlock = (archetype == BorrowerArchetype.LEGAL_BLOCK);

        return AisOipDebtorDto.builder()
                .iin(iin)
                .isDebtor(isBlock)
                .hasActiveAccountArrest(isBlock)
                .hasTravelBan(isBlock)
                .totalBailiffDebt(isBlock ? new BigDecimal("350000.00") : BigDecimal.ZERO)
                .activeProceedingsCount(isBlock ? 2 : 0)
                .build();
    }
}
