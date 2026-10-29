package kz.kaspi.credit.gateway.filter;

import kz.kaspi.credit.contracts.dto.ErrorResponseDto;
import kz.kaspi.credit.gateway.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class ClientRateLimiterWebFilter implements WebFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Применяем Rate Limiter только к эндпоинтам подачи заявок
        if (!path.startsWith("/v1/credit/apply")) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(exchange);

        return rateLimiterService.isAllowed(clientIp)
                .flatMap(allowed -> {
                    if (Boolean.TRUE.equals(allowed)) {
                        return chain.filter(exchange);
                    }

                    // Превышен лимит запросов: формируем non-blocking HTTP 429
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    exchange.getResponse().getHeaders().set(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiterService.getWindowSeconds()));

                    ErrorResponseDto errorDto = ErrorResponseDto.builder()
                            .errorCode("RATE_LIMIT_EXCEEDED")
                            .message("Превышен лимит запросов на подачу кредитных заявок. Повторите попытку позже.")
                            .path(path)
                            .status(HttpStatus.TOO_MANY_REQUESTS.value())
                            .timestamp(Instant.now())
                            .build();

                    try {
                        byte[] bytes = objectMapper.writeValueAsString(errorDto).getBytes(StandardCharsets.UTF_8);
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                    } catch (JsonProcessingException e) {
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }
}
