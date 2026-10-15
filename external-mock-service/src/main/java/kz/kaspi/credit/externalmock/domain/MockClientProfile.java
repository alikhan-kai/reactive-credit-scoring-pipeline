package kz.kaspi.credit.externalmock.domain;

import kz.kaspi.credit.contracts.external.AisOipDebtorDto;
import kz.kaspi.credit.contracts.external.GcvpDeductionsDto;
import kz.kaspi.credit.contracts.external.PkbReportDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockClientProfile {

    private String iin;
    private String fullName;
    private BorrowerArchetype archetype;
    private PkbReportDto pkbReport;
    private GcvpDeductionsDto gcvpDeductions;
    private AisOipDebtorDto aisOipDebtor;
}
