package kz.kaspi.credit.externalmock.controller;

import kz.kaspi.credit.contracts.external.AisOipDebtorDto;
import kz.kaspi.credit.contracts.external.GcvpDeductionsDto;
import kz.kaspi.credit.contracts.external.PkbReportDto;
import kz.kaspi.credit.externalmock.config.MockProperties;
import kz.kaspi.credit.externalmock.domain.BorrowerArchetype;
import kz.kaspi.credit.externalmock.service.MockDataProvider;
import kz.kaspi.credit.externalmock.util.IinGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockApiControllerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockProperties properties = new MockProperties();
        properties.setPkbDelayMs(0);
        properties.setGcvpDelayMs(0);
        properties.setAisOipDelayMs(0);
        properties.setTimeoutDelayMs(0);
        MockDataProvider dataProvider = new MockDataProvider();
        MockApiController controller = new MockApiController(dataProvider, properties);
        this.webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    @DisplayName("GET /mock/pkb/{iin} должен возвращать валидный отчет ПКБ")
    void shouldReturnPkbReport() {
        String primeIin = IinGenerator.generateForArchetype(BorrowerArchetype.PRIME, 1);

        webTestClient.get()
                .uri("/mock/pkb/{iin}", primeIin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PkbReportDto.class)
                .value(report -> {
                    assertNotNull(report);
                    assertEquals(primeIin, report.getIin());
                    assertEquals(840, report.getCreditScore());
                    assertEquals(0, report.getActiveCreditsCount());
                });
    }

    @Test
    @DisplayName("GET /mock/ais-oip/{iin} должен возвращать статус должника")
    void shouldReturnAisOipStatus() {
        String legalBlockIin = IinGenerator.generateForArchetype(BorrowerArchetype.LEGAL_BLOCK, 1);

        webTestClient.get()
                .uri("/mock/ais-oip/{iin}", legalBlockIin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AisOipDebtorDto.class)
                .value(debtor -> {
                    assertNotNull(debtor);
                    assertEquals(legalBlockIin, debtor.getIin());
                    assertTrue(debtor.isDebtor());
                    assertTrue(debtor.isHasActiveAccountArrest());
                });
    }

    @Test
    @DisplayName("GET /mock/gcvp/{iin} для PRIME заемщика должен возвращать 6 месяцев отчислений")
    void shouldReturnGcvpDeductionsForPrime() {
        String primeIin = IinGenerator.generateForArchetype(BorrowerArchetype.PRIME, 2);

        webTestClient.get()
                .uri("/mock/gcvp/{iin}", primeIin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GcvpDeductionsDto.class)
                .value(gcvp -> {
                    assertNotNull(gcvp);
                    assertEquals(primeIin, gcvp.getIin());
                    assertEquals(6, gcvp.getMonthlyDeductions().size());
                    assertEquals(new BigDecimal("750000.00"), gcvp.getCalculatedAverageMonthlyIncome());
                });
    }
}
