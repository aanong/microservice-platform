package com.demo.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class ReactiveTraceLoggingWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ReactiveTraceLoggingWebFilter.class);

    private final LoggingPlatformProperties properties;
    private final ServiceMetadata metadata;

    public ReactiveTraceLoggingWebFilter(LoggingPlatformProperties properties, ServiceMetadata metadata) {
        this.properties = properties;
        this.metadata = metadata;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String traceId = LoggingUtils.resolveOrCreateTraceId(request.getHeaders().getFirst(TraceConstants.TRACE_HEADER));
        TraceContext.bind(traceId, "0", metadata.getServiceName(), metadata.getEnv(), metadata.getHostName());
        exchange.getResponse().getHeaders().set(TraceConstants.TRACE_HEADER, traceId);

        return chain.filter(exchange)
            .doOnSuccess(unused -> {
                if (properties.isAccessLogEnabled()) {
                    logRequest(exchange, start, null);
                }
            })
            .doOnError(ex -> logRequest(exchange, start, ex))
            .doFinally(signalType -> TraceContext.clear());
    }

    private void logRequest(ServerWebExchange exchange, long start, Throwable ex) {
        long duration = System.currentTimeMillis() - start;
        HttpStatus status = exchange.getResponse().getStatusCode();
        int statusCode = status == null ? 200 : status.value();
        MDC.put("http.method", exchange.getRequest().getMethodValue());
        MDC.put("http.path", LoggingUtils.normalizePath(exchange.getRequest().getURI().getPath()));
        MDC.put("http.status", String.valueOf(statusCode));
        MDC.put("duration.ms", String.valueOf(duration));

        if (ex == null) {
            if (duration >= properties.getSlowRequestThresholdMs()) {
                log.warn("http request completed slowly. method={}, path={}, status={}, durationMs={}",
                    exchange.getRequest().getMethodValue(), exchange.getRequest().getURI().getPath(), statusCode, duration);
            } else {
                log.info("http request completed. method={}, path={}, status={}, durationMs={}",
                    exchange.getRequest().getMethodValue(), exchange.getRequest().getURI().getPath(), statusCode, duration);
            }
        } else {
            log.error("http request failed. method={}, path={}, status={}, durationMs={}",
                exchange.getRequest().getMethodValue(), exchange.getRequest().getURI().getPath(), statusCode, duration, ex);
        }
    }
}
