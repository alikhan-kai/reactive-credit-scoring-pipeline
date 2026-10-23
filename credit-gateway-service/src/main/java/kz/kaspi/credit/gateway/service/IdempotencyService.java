package kz.kaspi.credit.gateway.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration LOCK_TTL = Duration.ofSeconds(120);
    private static final String KEY_PREFIX = "idempotency:credit:";

    private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;

    /**
     * Атомарный захват ключа идемпотентности через Redis SETNX.
     * Возвращает true, если ключ был успешно установлен (первый запрос).
     * Возвращает false, если ключ уже существовал (повторный/дублирующий клик).
     */
    public Mono<Boolean> acquireLock(String idempotencyKey) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        return reactiveStringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", LOCK_TTL)
                .doOnNext(acquired -> {
                    if (Boolean.TRUE.equals(acquired)) {
                        log.info("Захвачен ключ идемпотентности {}", idempotencyKey);
                    } else {
                        log.warn("Обнаружен дубликат запроса с ключом идемпотентности {}", idempotencyKey);
                    }
                });
    }
}
