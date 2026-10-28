package kz.kaspi.credit.orchestrator.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import kz.kaspi.credit.orchestrator.domain.OutboxEvent;
import reactor.core.publisher.Flux;

@Repository
public interface OutboxEventRepository extends R2dbcRepository<OutboxEvent, UUID> {
    Flux<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
