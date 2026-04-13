package com.demo.common.logging;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logging.platform")
public class LoggingPlatformProperties {

    private boolean enabled = true;
    private String serviceName;
    private boolean accessLogEnabled = true;
    private long slowRequestThresholdMs = 1000L;
    private List<String> maskHeaders = new ArrayList<String>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isAccessLogEnabled() {
        return accessLogEnabled;
    }

    public void setAccessLogEnabled(boolean accessLogEnabled) {
        this.accessLogEnabled = accessLogEnabled;
    }

    public long getSlowRequestThresholdMs() {
        return slowRequestThresholdMs;
    }

    public void setSlowRequestThresholdMs(long slowRequestThresholdMs) {
        this.slowRequestThresholdMs = slowRequestThresholdMs;
    }

    public List<String> getMaskHeaders() {
        return maskHeaders;
    }

    public void setMaskHeaders(List<String> maskHeaders) {
        this.maskHeaders = maskHeaders;
    }
}
