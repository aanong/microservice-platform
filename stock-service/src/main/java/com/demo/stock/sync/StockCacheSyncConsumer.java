package com.demo.stock.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
    topic = "${mall.cache-sync.topic:cache-sync-stock}",
    consumerGroup = "${mall.cache-sync.consumer-group:stock-cache-sync-group}")
public class StockCacheSyncConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(StockCacheSyncConsumer.class);

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final List<CacheSyncHandler> handlers;

    @Value("${mall.cache-sync.enabled:true}")
    private boolean enabled;

    @Value("${mall.cache-sync.max-lag-ms:500}")
    private long maxLagMs;

    public StockCacheSyncConsumer(ObjectMapper objectMapper,
                                  StringRedisTemplate stringRedisTemplate,
                                  List<CacheSyncHandler> handlers) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.handlers = handlers;
    }

    @Override
    public void onMessage(String message) {
        if (!enabled) {
            return;
        }
        CacheSyncEvent event = parse(message);
        if (event == null || event.getTable() == null) {
            return;
        }
        if (!markIdempotent(event)) {
            return;
        }
        checkLag(event);
        for (CacheSyncHandler handler : handlers) {
            if (handler.supports(event.getTable())) {
                handler.handle(event);
            }
        }
    }

    private CacheSyncEvent parse(String message) {
        try {
            return objectMapper.readValue(message, CacheSyncEvent.class);
        } catch (Exception ex) {
            log.warn("Skip invalid cache sync payload: {}", message, ex);
            return null;
        }
    }

    private boolean markIdempotent(CacheSyncEvent event) {
        String key = "mall:cache:sync:dedup:stock:" + buildEventKey(event);
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofHours(24));
        return Boolean.TRUE.equals(success);
    }

    private String buildEventKey(CacheSyncEvent event) {
        if (event.getEventId() != null && !event.getEventId().trim().isEmpty()) {
            return event.getEventId();
        }
        String sourcePos = event.getSourcePos() == null ? "_" : event.getSourcePos();
        String pk = event.getPk() == null ? "_" : event.getPk().toString();
        return event.getTable() + ":" + pk + ":" + sourcePos;
    }

    private void checkLag(CacheSyncEvent event) {
        if (event.getTs() == null || maxLagMs <= 0L) {
            return;
        }
        long lag = System.currentTimeMillis() - event.getTs();
        if (lag > maxLagMs) {
            log.warn("Cache sync lag exceeded. table={}, lagMs={}, maxLagMs={}", event.getTable(), lag, maxLagMs);
        }
    }
}
