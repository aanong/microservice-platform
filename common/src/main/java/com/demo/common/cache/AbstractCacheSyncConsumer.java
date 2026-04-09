package com.demo.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 缓存同步消费者抽象基类。
 * <p>
 * 封装了各服务中完全重复的消息解析、幂等去重、延迟检测、Handler 分发等逻辑。
 * 子类只需：
 * <ol>
 *   <li>添加 {@code @Component} 和 {@code @RocketMQMessageListener} 注解</li>
 *   <li>实现 {@link #getServiceName()} 返回服务标识（用于日志和去重键）</li>
 * </ol>
 *
 * <pre>
 * &#64;Component
 * &#64;RocketMQMessageListener(
 *     topic = "${mall.cache-sync.topic:cache-sync-order}",
 *     consumerGroup = "${mall.cache-sync.consumer-group:order-cache-sync-group}")
 * public class OrderCacheSyncConsumer extends AbstractCacheSyncConsumer {
 *
 *     public OrderCacheSyncConsumer(ObjectMapper objectMapper,
 *                                   StringRedisTemplate stringRedisTemplate,
 *                                   List&lt;CacheSyncHandler&gt; handlers) {
 *         super(objectMapper, stringRedisTemplate, handlers);
 *     }
 *
 *     &#64;Override
 *     protected String getServiceName() {
 *         return "order";
 *     }
 * }
 * </pre>
 */
public abstract class AbstractCacheSyncConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCacheSyncConsumer.class);

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final List<CacheSyncHandler> handlers;

    @Value("${mall.cache-sync.enabled:true}")
    private boolean enabled;

    @Value("${mall.cache-sync.max-lag-ms:500}")
    private long maxLagMs;

    protected AbstractCacheSyncConsumer(ObjectMapper objectMapper,
                                        StringRedisTemplate stringRedisTemplate,
                                        List<CacheSyncHandler> handlers) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.handlers = handlers;
    }

    /**
     * 返回服务标识，用于日志输出和 Redis 去重键前缀。
     * 例如 "order"、"stock"、"user"
     */
    protected abstract String getServiceName();

    @Override
    public void onMessage(String message) {
        if (!enabled) {
            return;
        }
        List<CacheSyncEvent> events = parseEvents(message);
        if (events.isEmpty()) {
            return;
        }
        String serviceName = getServiceName();
        for (CacheSyncEvent event : events) {
            if (event == null || event.getTable() == null) {
                continue;
            }
            if (!markIdempotent(event, serviceName)) {
                log.debug("Skip duplicated cache sync event. service={}, eventId={}", serviceName, resolveEventId(event));
                continue;
            }
            try {
                checkLag(event);
                for (CacheSyncHandler handler : handlers) {
                    if (handler.supports(event.getTable())) {
                        handler.handle(event);
                    }
                }
                log.info("Cache sync success. service={}, eventId={}, table={}, opType={}, pk={}, sourcePos={}",
                    serviceName, resolveEventId(event), event.getTable(), event.getOpType(), event.getPk(), event.getSourcePos());
            } catch (Exception ex) {
                log.error("Cache sync failed. service={}, eventId={}, table={}, opType={}, pk={}, sourcePos={}",
                    serviceName, resolveEventId(event), event.getTable(), event.getOpType(), event.getPk(), event.getSourcePos(), ex);
                throw ex;
            }
        }
    }

    // ===== 消息解析 =====

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

    // ===== 幂等去重 =====

    private boolean markIdempotent(CacheSyncEvent event, String serviceName) {
        String key = "mall:cache:sync:dedup:" + serviceName + ":" + buildEventKey(event);
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

    // ===== 延迟检测 =====

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
