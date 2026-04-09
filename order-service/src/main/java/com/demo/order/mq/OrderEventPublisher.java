package com.demo.order.mq;

import com.demo.common.mq.AbstractEventPublisher;
import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 订单事件发布器。
 * <p>
 * 继承公共基类 {@link AbstractEventPublisher}，复用 enabled 开关和异常容错逻辑。
 */
@Component
public class OrderEventPublisher extends AbstractEventPublisher {

    public OrderEventPublisher(RocketMQTemplate rocketMQTemplate,
                               @Value("${mall.mq.order-event-topic:order-event-topic}") String orderEventTopic,
                               @Value("${mall.mq.order-event-enabled:true}") boolean orderEventEnabled) {
        super(rocketMQTemplate, orderEventTopic, orderEventEnabled);
    }

    /**
     * 发布订单已支付事件
     */
    public void publishPaidEvent(Long orderId, String orderNo, Long userId) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("event", "ORDER_PAID");
        payload.put("orderId", orderId);
        payload.put("orderNo", orderNo);
        payload.put("userId", userId);
        sendEvent(payload);
    }
}
