package com.demo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        String grpcOffset = System.getProperty("nacos.server.grpc.port.offset");
        if (grpcOffset == null || grpcOffset.trim().isEmpty()) {
            String envOffset = System.getenv("NACOS_SERVER_GRPC_PORT_OFFSET");
            System.setProperty("nacos.server.grpc.port.offset",
                (envOffset == null || envOffset.trim().isEmpty()) ? "1001" : envOffset);
        }
        SpringApplication.run(GatewayApplication.class, args);
    }
}
