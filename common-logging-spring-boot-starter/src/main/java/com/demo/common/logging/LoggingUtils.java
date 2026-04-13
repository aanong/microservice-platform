package com.demo.common.logging;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

public final class LoggingUtils {

    private LoggingUtils() {
    }

    public static String resolveOrCreateTraceId(String candidate) {
        if (StringUtils.hasText(candidate)) {
            return candidate;
        }
        return TraceContext.generateTraceId();
    }

    public static Map<String, String> maskedHeaders(HttpServletRequest request, Iterable<String> configuredHeaders) {
        Set<String> maskedNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        maskedNames.add(HttpHeaders.AUTHORIZATION);
        for (String header : configuredHeaders) {
            if (StringUtils.hasText(header)) {
                maskedNames.add(header.trim());
            }
        }

        Map<String, String> headers = new LinkedHashMap<String, String>();
        java.util.Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String header = names.nextElement();
            String value = request.getHeader(header);
            headers.put(header, maskedNames.contains(header) ? "***" : value);
        }
        return headers;
    }

    public static String shortenPayload(Object payload) {
        if (payload == null) {
            return "";
        }
        String text = String.valueOf(payload);
        if (text.length() <= 512) {
            return text;
        }
        return text.substring(0, 512) + "...";
    }

    public static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/";
        }
        return path.toLowerCase(Locale.ROOT);
    }
}
