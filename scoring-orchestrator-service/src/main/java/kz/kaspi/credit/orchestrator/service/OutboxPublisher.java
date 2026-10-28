package kz.kaspi.credit.orchestrator.service;

import kz.kaspi.credit.orchestrator.domain.OutboxEvent;
import kz.kaspi.credit.orchestrator.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void startOutboxProcessing() {
        log.info("Запуск фонового реактивного Outbox Publisher (интервал 200ms)...");

        Flux.interval(Duration.ofMillis(200))
                .flatMap(tick -> processPendingEvents())
                .onErrorContinue((err, obj) -> log.error("Ошибка в цикле Outbox: {}", err.getMessage()))
                .subscribe();
    }

    private Flux<OutboxEvent> processPendingEvents() {
        return outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING")
                .flatMap(this::publishToKafka);
    }

    private Mono<OutboxEvent> publishToKafka(OutboxEvent event) {
        return Mono.fromFuture(() -> kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()))
                .flatMap(sendResult -> {
                    event.setStatus("SENT");
                    event.setSentAt(Instant.now());
                    event.setNewRecord(false); // Для R2DBC это update
                    return outboxEventRepository.save(event);
                })
                .doOnSuccess(saved -> log.info("Событие Outbox {} успешно опубликовано в Kafka [топик {}]",
                        saved.getId(), saved.getTopic()))
                .doOnError(e -> log.error("Сбой публикации события Outbox {}: {}", event.getId(), e.getMessage()));
    }
}