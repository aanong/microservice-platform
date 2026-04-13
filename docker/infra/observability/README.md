# Observability Setup

This project now ships an opinionated observability stack:

- ELK for structured JSON logs
- SkyWalking OAP + UI for tracing and service topology
- RocketMQ trace fallback via `traceId` propagation in message payloads

## Start the stack

Run from `docker/infra`:

```bash
docker compose up -d elasticsearch logstash kibana skywalking-oap skywalking-ui
```

Useful endpoints:

- Elasticsearch: `http://<host>:9200`
- Kibana: `http://<host>:5601`
- SkyWalking UI: `http://<host>:8088`
- SkyWalking OAP gRPC: `<host>:11800`
- SkyWalking OAP HTTP: `http://<host>:12800`

## Spring Boot service settings

Each service should keep:

- `spring.application.name=<service-name>`
- `logging.platform.service-name=<service-name>`

The common module now auto-configures:

- JSON logs to `stdout`
- `X-Trace-Id` HTTP propagation
- servlet/WebFlux access logs
- RocketMQ producer and consumer trace logging

## SkyWalking Java agent

Mount the official agent package into each service process and add JVM options similar to:

```bash
-javaagent:/skywalking/agent/skywalking-agent.jar
-DSW_AGENT_NAME=order-service
-DSW_AGENT_COLLECTOR_BACKEND_SERVICES=192.168.5.79:11800
```

Recommended agent mount path:

- `/skywalking/agent`

## Verification checklist

1. Call the gateway and confirm the response includes `X-Trace-Id`.
2. Search the same `trace.id` in Kibana.
3. Trigger a RocketMQ event and confirm producer/consumer logs share the same `trace.id`.
4. Open SkyWalking UI and verify the gateway, user, order, stock, and shop services appear in topology.
