package com.demo.gateway.filter;

import com.demo.gateway.dto.ValidateTokenResponse;
import java.net.URI;
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

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    public AuthGlobalFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (!isOrderDomainPath(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authorization.substring(7);
        URI validateUri = URI.create("http://user-service/api/user/internal/validate?token=" + token);

        return webClientBuilder.build()
            .get()
            .uri(validateUri)
            .retrieve()
            .bodyToMono(ValidateTokenResponse.class)
            .flatMap(resp -> {
                if (resp == null || !resp.isValid() || resp.getUserId() == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                ServerHttpRequest mutated = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", String.valueOf(resp.getUserId()))
                    .build();
                return chain.filter(exchange.mutate().request(mutated).build());
            })
            .onErrorResume(ex -> {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            });
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isOrderDomainPath(String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/order/")
            || path.equals("/api/cart") || path.startsWith("/api/cart/")
            || path.equals("/api/orders") || path.startsWith("/api/orders/")
            || path.equals("/api/payments") || path.startsWith("/api/payments/")
            || path.equals("/api/coupons") || path.startsWith("/api/coupons/")
            || path.equals("/api/refunds") || path.startsWith("/api/refunds/")
            || path.equals("/api/logistics") || path.startsWith("/api/logistics/");
    }
}
