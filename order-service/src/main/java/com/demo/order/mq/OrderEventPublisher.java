package com.demo.order.mq;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final String orderEventTopic;
    private final boolean orderEventEnabled;

    public OrderEventPublisher(RocketMQTemplate rocketMQTemplate,
                               @Value("${mall.mq.order-event-topic:order-event-topic}") String orderEventTopic,
                               @Value("${mall.mq.order-event-enabled:true}") boolean orderEventEnabled) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.orderEventTopic = orderEventTopic;
        this.orderEventEnabled = orderEventEnabled;
    }

    public void publishPaidEvent(Long orderId, String orderNo, Long userId) {
        if (!orderEventEnabled) {
            return;
        }
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("event", "ORDER_PAID");
        payload.put("orderId", orderId);
        payload.put("orderNo", orderNo);
        payload.put("userId", userId);
//        if (1 == 1) {
//            throw new RuntimeException();
//        }
        try {
            rocketMQTemplate.convertAndSend(orderEventTopic, payload);
        } catch (Exception ex) {
            log.warn("Publish order event failed. topic={}, orderId={}, orderNo={}. Continue without blocking business.",
                orderEventTopic, orderId, orderNo, ex);
        }
    }
}
