package com.rikkeibank.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Lọc JWT tại biên. Mọi request (trừ path công khai) phải mang Bearer token hợp lệ.
 * Gateway KHÔNG tự giải mã token mà gọi identity-service /internal/introspect,
 * nhờ đó ADMIN thu hồi phiên (tăng token_version) có hiệu lực NGAY LẬP TỨC.
 * Sau khi hợp lệ, gateway gắn X-User-Id / X-User-Role cho downstream và xóa
 * mọi header X-User-* do client tự bịa (chống giả mạo).
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final WebClient introspectClient;

    public JwtAuthenticationFilter(WebClient.Builder builder) {
        this.introspectClient = builder.baseUrl("http://identity-service").build();
    }

    private static final List<String> PUBLIC = List.of("/auth/login", "/auth/refresh");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (PUBLIC.stream().anyMatch(path::startsWith)) {
            return chain.filter(stripUserHeaders(exchange));
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return deny(exchange, HttpStatus.UNAUTHORIZED);
        }
        String token = auth.substring(7);

        return introspectClient.post()
                .uri("/internal/introspect")
                .bodyValue(Map.of("token", token))
                .retrieve()
                .bodyToMono(Introspection.class)
                .flatMap(res -> {
                    if (res == null || !res.valid()) {
                        return deny(exchange, HttpStatus.UNAUTHORIZED);
                    }
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(res.uid()))
                            .header("X-User-Role", res.role())
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(e -> deny(exchange, HttpStatus.SERVICE_UNAVAILABLE));
    }

    // Xóa header X-User-* nếu client cố gắng tự đặt trên path công khai
    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest r = exchange.getRequest().mutate()
                .headers(h -> { h.remove("X-User-Id"); h.remove("X-User-Role"); })
                .build();
        return exchange.mutate().request(r).build();
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() { return -1; }

    public record Introspection(boolean valid, Long uid, String role) {}
}
