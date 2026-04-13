package com.demo.common.config;

import com.demo.common.logging.LoggingPlatformProperties;
import com.demo.common.logging.ReactiveTraceLoggingWebFilter;
import com.demo.common.logging.ServiceMetadata;
import com.demo.common.logging.ServletTraceLoggingFilter;
import com.demo.common.logging.TraceWebClientCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableConfigurationProperties(LoggingPlatformProperties.class)
@ConditionalOnProperty(prefix = "logging.platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PlatformLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceMetadata serviceMetadata(LoggingPlatformProperties properties, Environment environment) {
        return new ServiceMetadata(properties, environment);
    }

    @Bean
    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(name = "platformServletTraceLoggingFilter")
    public ServletTraceLoggingFilter platformServletTraceLoggingFilter(LoggingPlatformProperties properties,
                                                                      ServiceMetadata metadata) {
        return new ServletTraceLoggingFilter(properties, metadata);
    }

    @Bean
    @ConditionalOnClass(WebFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean(name = "platformReactiveTraceLoggingWebFilter")
    public ReactiveTraceLoggingWebFilter platformReactiveTraceLoggingWebFilter(LoggingPlatformProperties properties,
                                                                               ServiceMetadata metadata) {
        return new ReactiveTraceLoggingWebFilter(properties, metadata);
    }

    @Bean
    @ConditionalOnClass(WebClientCustomizer.class)
    @ConditionalOnMissingBean
    public TraceWebClientCustomizer traceWebClientCustomizer(ServiceMetadata metadata) {
        return new TraceWebClientCustomizer(metadata);
    }
}
