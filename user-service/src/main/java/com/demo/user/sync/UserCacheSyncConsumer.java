package com.demo.user.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncHandler;
import com.demo.common.cache.CanalFlatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
    topic = "${mall.cache-sync.topic:cache-sync-user}",
    consumerGroup = "${mall.cache-sync.consumer-group:user-cache-sync-group}")
public class UserCacheSyncConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(UserCacheSyncConsumer.class);

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final List<CacheSyncHandler> handlers;

    @Value("${mall.cache-sync.enabled:true}")
    private boolean enabled;

    @Value("${mall.cache-sync.max-lag-ms:500}")
    private long maxLagMs;

    public UserCacheSyncConsumer(ObjectMapper objectMapper,
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
        List<CacheSyncEvent> events = parseEvents(message);
        if (events.isEmpty()) {
            return;
        }
        for (CacheSyncEvent event : events) {
            if (event == null || event.getTable() == null) {
                continue;
            }
            if (!markIdempotent(event)) {
                log.debug("Skip duplicated cache sync event. service=user, eventId={}", resolveEventId(event));
                continue;
            }
            try {
                checkLag(event);
                for (CacheSyncHandler handler : handlers) {
                    if (handler.supports(event.getTable())) {
                        handler.handle(event);
                    }
                }
                log.info("Cache sync success. service=user, eventId={}, table={}, opType={}, pk={}, sourcePos={}",
                    resolveEventId(event), event.getTable(), event.getOpType(), event.getPk(), event.getSourcePos());
            } catch (Exception ex) {
                log.error("Cache sync failed. service=user, eventId={}, table={}, opType={}, pk={}, sourcePos={}",
                    resolveEventId(event), event.getTable(), event.getOpType(), event.getPk(), event.getSourcePos(), ex);
                throw ex;
            }
        }
    }

    private List<CacheSyncEvent> parseEvents(String message) {
        List<CacheSyncEvent> events = new ArrayList<CacheSyncEvent>();
        CacheSyncEvent syncEvent = parseCacheSyncEvent(message);
        if (isStructuredCacheSyncEvent(syncEvent)) {
            events.add(syncEvent);
            return events;
        }

        CanalFlatMessage flatMessage = parseFlatMessage(message);
        if (flatMessage == null || flatMessage.getTable() == null || flatMessage.getData() == null || flatMessage.getData().isEmpty()) {
            return events;
        }
        for (int i = 0; i < flatMessage.getData().size(); i++) {
            CacheSyncEvent event = convertFlatMessage(flatMessage, i);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

    private CacheSyncEvent parseCacheSyncEvent(String message) {
        try {
            return objectMapper.readValue(message, CacheSyncEvent.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isStructuredCacheSyncEvent(CacheSyncEvent event) {
        if (event == null || event.getTable() == null) {
            return false;
        }
        return event.getPk() != null || event.getBefore() != null || event.getAfter() != null;
    }

    private CanalFlatMessage parseFlatMessage(String message) {
        try {
            return objectMapper.readValue(message, CanalFlatMessage.class);
        } catch (Exception ex) {
            log.warn("Skip invalid cache sync payload: {}", message, ex);
            return null;
        }
    }

    private CacheSyncEvent convertFlatMessage(CanalFlatMessage flatMessage, int index) {
        if (flatMessage.getData() == null || flatMessage.getData().size() <= index) {
            return null;
        }
        Map<String, Object> row = toObjectMap(flatMessage.getData().get(index));
        String opType = flatMessage.getType() == null ? "UPDATE" : flatMessage.getType().toUpperCase();

        Map<String, Object> before = null;
        Map<String, Object> after = null;
        if ("DELETE".equals(opType)) {
            before = row;
        } else if ("UPDATE".equals(opType)) {
            after = row;
            before = new HashMap<String, Object>(row);
            if (flatMessage.getOld() != null && flatMessage.getOld().size() > index && flatMessage.getOld().get(index) != null) {
                before.putAll(toObjectMap(flatMessage.getOld().get(index)));
            }
        } else {
            after = row;
        }

        CacheSyncEvent event = new CacheSyncEvent();
        event.setEventId((flatMessage.getId() == null ? "flat" : String.valueOf(flatMessage.getId())) + "-" + index);
        event.setDb(flatMessage.getDatabase());
        event.setTable(flatMessage.getTable());
        event.setOpType(opType);
        event.setBefore(before);
        event.setAfter(after);
        event.setTs(flatMessage.getEs() != null ? flatMessage.getEs() : flatMessage.getTs());
        event.setPk(extractPk(row));
        event.setSourcePos("flat:" + (flatMessage.getId() == null ? "_" : flatMessage.getId()));
        return event;
    }

    private Map<String, Object> toObjectMap(Map<String, String> source) {
        Map<String, Object> target = new HashMap<String, Object>();
        if (source == null) {
            return target;
        }
        target.putAll(source);
        return target;
    }

    private Map<String, Object> extractPk(Map<String, Object> row) {
        Map<String, Object> pk = new HashMap<String, Object>();
        addIfPresent(pk, row, "id");
        addIfPresent(pk, row, "order_id");
        addIfPresent(pk, row, "user_id");
        addIfPresent(pk, row, "shipment_id");
        addIfPresent(pk, row, "refund_id");
        return pk;
    }

    private void addIfPresent(Map<String, Object> pk, Map<String, Object> row, String key) {
        if (row.containsKey(key) && row.get(key) != null) {
            pk.put(key, row.get(key));
        }
    }

    private boolean markIdempotent(CacheSyncEvent event) {
        String key = "mall:cache:sync:dedup:user:" + buildEventKey(event);
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

    private String resolveEventId(CacheSyncEvent event) {
        return buildEventKey(event);
    }
}
