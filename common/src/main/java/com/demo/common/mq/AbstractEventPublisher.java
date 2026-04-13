package com.demo.common.mq;

import com.demo.common.logging.LoggingUtils;
import com.demo.common.logging.TraceContext;
import java.util.Map;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * MQ 事件发布器抽象基类。
 * <p>
 * 封装了各服务中重复的消息发送逻辑，包括：
 * <ul>
 *   <li>enabled 开关控制</li>
 *   <li>发送异常容错处理（不阻塞业务流程）</li>
 *   <li>统一日志记录</li>
 * </ul>
 *
 * <p>子类只需调用 {@link #sendEvent(Map)} 或 {@link #sendEvent(String, Map)} 即可。
 *
 * <pre>
 * &#64;Component
 * public class OrderEventPublisher extends AbstractEventPublisher {
 *
 *     public OrderEventPublisher(RocketMQTemplate rocketMQTemplate,
 *                                &#64;Value("${mall.mq.order-event-topic:order-event-topic}") String topic,
 *                                &#64;Value("${mall.mq.order-event-enabled:true}") boolean enabled) {
 *         super(rocketMQTemplate, topic, enabled);
 *     }
 * }
 * </pre>
 */
public abstract class AbstractEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AbstractEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final String defaultTopic;
    private final boolean enabled;

    protected AbstractEventPublisher(RocketMQTemplate rocketMQTemplate,
                                      String defaultTopic,
                                      boolean enabled) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.defaultTopic = defaultTopic;
        this.enabled = enabled;
    }

    /**
     * 向默认 topic 发送事件消息
     *
     * @param payload 消息体
     */
    protected void sendEvent(Map<String, Object> payload) {
        sendEvent(defaultTopic, payload);
    }

    /**
     * 向指定 topic 发送事件消息
     *
     * @param topic   目标 topic
     * @param payload 消息体
     */
    protected void sendEvent(String topic, Map<String, Object> payload) {
        if (!enabled) {
            return;
        }
        String traceId = TraceContext.getOrCreateTraceId();
        payload.put("traceId", traceId);
        MDC.put("mq.topic", topic);
        MDC.put("mq.messageKey", traceId);
        try {
            rocketMQTemplate.convertAndSend(topic, payload);
            log.info("Publish event success. topic={}, traceId={}, payload={}",
                topic, traceId, LoggingUtils.shortenPayload(payload));
        } catch (Exception ex) {
            log.warn("Publish event failed. topic={}, payload={}. Continue without blocking business.",
                topic, payload, ex);
        } finally {
            MDC.remove("mq.topic");
            MDC.remove("mq.messageKey");
        }
    }

    /**
     * 获取默认 topic 名称
     */
    protected String getDefaultTopic() {
        return defaultTopic;
    }

    /**
     * 获取是否启用
     */
    protected boolean isEnabled() {
        return enabled;
    }
}
