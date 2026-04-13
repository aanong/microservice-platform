package com.demo.common.logging;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class TraceContext {

    private TraceContext() {
    }

    public static String getOrCreateTraceId() {
        String traceId = MDC.get(TraceConstants.TRACE_ID);
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        traceId = generateTraceId();
        MDC.put(TraceConstants.TRACE_ID, traceId);
        if (!StringUtils.hasText(MDC.get(TraceConstants.SPAN_ID))) {
            MDC.put(TraceConstants.SPAN_ID, "0");
        }
        return traceId;
    }

    public static void bind(String traceId, String spanId, String serviceName, String env, String host) {
        if (StringUtils.hasText(traceId)) {
            MDC.put(TraceConstants.TRACE_ID, traceId);
        }
        MDC.put(TraceConstants.SPAN_ID, StringUtils.hasText(spanId) ? spanId : "0");
        putIfHasText(TraceConstants.SERVICE_NAME, serviceName);
        putIfHasText(TraceConstants.ENV, env);
        putIfHasText(TraceConstants.HOST, host);
    }

    public static void clear() {
        MDC.remove(TraceConstants.TRACE_ID);
        MDC.remove(TraceConstants.SPAN_ID);
        MDC.remove(TraceConstants.SERVICE_NAME);
        MDC.remove(TraceConstants.ENV);
        MDC.remove(TraceConstants.HOST);
        MDC.remove("http.method");
        MDC.remove("http.path");
        MDC.remove("http.status");
        MDC.remove("duration.ms");
        MDC.remove("mq.topic");
        MDC.remove("mq.consumerGroup");
        MDC.remove("mq.messageKey");
        MDC.remove("mq.tag");
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static void putIfHasText(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        }
    }
}
