package com.demo.common.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class ServiceMetadata {

    private final String serviceName;
    private final String env;
    private final String hostName;

    public ServiceMetadata(LoggingPlatformProperties properties, Environment environment) {
        this.serviceName = resolveServiceName(properties, environment);
        this.env = resolveEnv(environment);
        this.hostName = resolveHostName();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEnv() {
        return env;
    }

    public String getHostName() {
        return hostName;
    }

    private String resolveServiceName(LoggingPlatformProperties properties, Environment environment) {
        if (StringUtils.hasText(properties.getServiceName())) {
            return properties.getServiceName();
        }
        String appName = environment.getProperty("spring.application.name");
        return StringUtils.hasText(appName) ? appName : "unknown-service";
    }

    private String resolveEnv(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length > 0) {
            return profiles[0];
        }
        String env = environment.getProperty("spring.profiles.active");
        return StringUtils.hasText(env) ? env : "default";
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknown-host";
        }
    }
}
