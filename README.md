# Reactive Credit & Installment Scoring Pipeline (Kaspi 0-0-12)

Высокопроизводительная реактивная микросервисная платформа экспресс-кредитования и рассрочки (SLA < 2s).

## Архитектура микросервисов

| Модуль | Назначение | Порт |
| :--- | :--- | :--- |
| `common-contracts` | Общие DTO, события Kafka, алгоритм валидации ИИН РК | — |
| `credit-gateway-service` | Входной реактивный шлюз, идемпотентность (Redis), Rate Limiting | `8080` |
| `scoring-orchestrator-service` | Реактивный DAG-скоринг (Scatter-Gather, Resilience4j, Kafka) | `8082` |
| `external-mock-service` | Эмулятор внешних интеграций (ПКБ, ГЦВП, АИС ОИП) | `8083` |
| `credit-agreement-service` | Генерация договоров и графиков платежей (R2DBC Postgres) | `8084` |
| `audit-notification-service` | Аудит скоринговых решений и отправка нотификаций | `8085` |

## Стек технологий

* **Среда и фреймворк:** Java 21, Spring Boot, Spring WebFlux, Project Reactor, Netty
* **Брокер сообщений:** Apache Kafka (KRaft) + Reactor Kafka
* **Хранилища данных:** PostgreSQL 16 (R2DBC non-blocking) + Reactive Redis 7 (Lettuce)
* **Отказоустойчивость:** Resilience4j Reactor (Circuit Breaker, TimeLimiter)
* **Мониторинг:** Prometheus, Grafana, Zipkin Tracing
