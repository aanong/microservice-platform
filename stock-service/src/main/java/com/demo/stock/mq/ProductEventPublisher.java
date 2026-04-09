package com.demo.stock.mq;

import com.demo.common.entity.Product;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final String productTopic;
    private final boolean productEventEnabled;

    public ProductEventPublisher(RocketMQTemplate rocketMQTemplate,
                                 @Value("${mall.mq.product-topic:mall-product-topic}") String productTopic,
                                 @Value("${mall.mq.product-event-enabled:true}") boolean productEventEnabled) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.productTopic = productTopic;
        this.productEventEnabled = productEventEnabled;
    }

    public void publish(String action, Product product) {
        if (!productEventEnabled) {
            return;
        }
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("action", action);
        payload.put("id", product.getId());
        payload.put("name", product.getName());
        payload.put("skuCode", product.getSkuCode());
        payload.put("categoryId", product.getCategoryId());
        payload.put("price", product.getPrice());
        payload.put("stock", product.getStock());
        payload.put("status", product.getStatus());
        try {
            rocketMQTemplate.convertAndSend(productTopic, payload);
        } catch (Exception ex) {
            log.warn("Publish product event failed. topic={}, action={}, productId={}. Continue without blocking business.",
                productTopic, action, product.getId(), ex);
        }
    }
}
