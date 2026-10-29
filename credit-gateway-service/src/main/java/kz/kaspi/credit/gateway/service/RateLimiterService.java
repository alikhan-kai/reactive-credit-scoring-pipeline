package kz.kaspi.credit.gateway.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:ip:";

    private final ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate;

    @Value("${security.rate-limit.max-requests-per-window:10}")
    private long maxRequestsPerWindow;

    @Value("${security.rate-limit.window-seconds:60}")
    private long windowSeconds;

    /**
     * Проверяет, не превышен ли лимит запросов с указанного IP-адреса за фиксированное окно.
     * Возвращает true, если запрос разрешен.
     * Возвращает false, если лимит исчерпан.
     */
    public Mono<Boolean> isAllowed(String clientIp) {
        String key = RATE_LIMIT_PREFIX + clientIp;

        return reactiveStringRedisTemplate.opsForValue()
                .increment(key)
                .flatMap(currentCount -> {
                    if (currentCount == 1) {
                        // Первый запрос в текущем окне: выставляем TTL
                        return reactiveStringRedisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
                                .thenReturn(true);
                    }
                    if (currentCount <= maxRequestsPerWindow) {
                        return Mono.just(true);
                    }
                    log.warn("Превышен Rate Limit для IP {}: {} запросов (максимум {})", clientIp, currentCount, maxRequestsPerWindow);
                    return Mono.just(false);
                });
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }
}
