package kz.kaspi.credit.externalmock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mock.simulation")
public class MockProperties {

    /** Задержка ответа ПКБ (в миллисекундах) */
    private long pkbDelayMs = 300;

    /** Задержка ответа ГЦВП (в миллисекундах) */
    private long gcvpDelayMs = 600;

    /** Задержка проверки в реестре должников АИС ОИП (в миллисекундах) */
    private long aisOipDelayMs = 150;

    /** Искусственная задержка для проверки Circuit Breaker таймаута (в миллисекундах) */
    private long timeoutDelayMs = 3000;

    /** Вероятность генерации сбоя (HTTP 500/504) для проверки отказоустойчивости (0.0 - 1.0) */
    private double gcvpFailureRate = 0.05;
}
