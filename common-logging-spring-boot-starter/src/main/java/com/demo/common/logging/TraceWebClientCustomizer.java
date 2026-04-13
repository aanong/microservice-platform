package com.demo.common.logging;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.ClientRequest;

public class TraceWebClientCustomizer implements WebClientCustomizer {

    private final ServiceMetadata metadata;

    public TraceWebClientCustomizer(ServiceMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void customize(org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder) {
        webClientBuilder.filter((request, next) -> {
            String traceId = TraceContext.getOrCreateTraceId();
            ClientRequest newRequest = ClientRequest.from(request)
                .header(TraceConstants.TRACE_HEADER, traceId)
                .header("X-Service-Name", metadata.getServiceName())
                .build();
            return next.exchange(newRequest);
        });
    }
}
