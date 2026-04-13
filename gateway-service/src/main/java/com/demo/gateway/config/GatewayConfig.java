package com.demo.gateway.config;

import java.util.List;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder(ObjectProvider<WebClientCustomizer> customizers) {
        WebClient.Builder builder = WebClient.builder();
        List<WebClientCustomizer> orderedCustomizers = customizers.orderedStream()
            .collect(java.util.stream.Collectors.toList());
        for (WebClientCustomizer customizer : orderedCustomizers) {
            customizer.customize(builder);
        }
        return builder;
    }
}
