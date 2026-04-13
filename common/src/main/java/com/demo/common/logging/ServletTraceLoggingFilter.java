package com.demo.common.logging;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class ServletTraceLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ServletTraceLoggingFilter.class);

    private final LoggingPlatformProperties properties;
    private final ServiceMetadata metadata;

    public ServletTraceLoggingFilter(LoggingPlatformProperties properties, ServiceMetadata metadata) {
        this.properties = properties;
        this.metadata = metadata;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long start = System.currentTimeMillis();
        boolean success = false;
        String traceId = LoggingUtils.resolveOrCreateTraceId(request.getHeader(TraceConstants.TRACE_HEADER));
        TraceContext.bind(traceId, "0", metadata.getServiceName(), metadata.getEnv(), metadata.getHostName());
        response.setHeader(TraceConstants.TRACE_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
            success = true;
        } catch (Exception ex) {
            logHttp(request, response, start, ex);
            throw ex;
        } finally {
            if (success && properties.isAccessLogEnabled()) {
                logHttp(request, response, start, null);
            }
            TraceContext.clear();
        }
    }

    private void logHttp(HttpServletRequest request, HttpServletResponse response, long start, Exception ex) {
        long duration = System.currentTimeMillis() - start;
        MDC.put("http.method", request.getMethod());
        MDC.put("http.path", LoggingUtils.normalizePath(request.getRequestURI()));
        MDC.put("http.status", String.valueOf(response.getStatus()));
        MDC.put("duration.ms", String.valueOf(duration));

        Map<String, String> headers = LoggingUtils.maskedHeaders(request, properties.getMaskHeaders());
        if (ex == null) {
            if (duration >= properties.getSlowRequestThresholdMs()) {
                log.warn("http request completed slowly. method={}, path={}, status={}, durationMs={}, headers={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, headers);
            } else {
                log.info("http request completed. method={}, path={}, status={}, durationMs={}, headers={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, headers);
            }
        } else {
            log.error("http request failed. method={}, path={}, status={}, durationMs={}, headers={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(), duration, headers, ex);
        }
    }
}
