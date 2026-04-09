package com.demo.stock.mq;

import com.demo.common.entity.Product;
import com.demo.common.mq.AbstractEventPublisher;
import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 商品事件发布器。
 * <p>
 * 继承公共基类 {@link AbstractEventPublisher}，复用 enabled 开关和异常容错逻辑。
 */
@Component
public class ProductEventPublisher extends AbstractEventPublisher {

    public ProductEventPublisher(RocketMQTemplate rocketMQTemplate,
                                 @Value("${mall.mq.product-topic:mall-product-topic}") String productTopic,
                                 @Value("${mall.mq.product-event-enabled:true}") boolean productEventEnabled) {
        super(rocketMQTemplate, productTopic, productEventEnabled);
    }

    /**
     * 发布商品变更事件
     */
    public void publish(String action, Product product) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("action", action);
        payload.put("id", product.getId());
        payload.put("name", product.getName());
        payload.put("skuCode", product.getSkuCode());
        payload.put("categoryId", product.getCategoryId());
        payload.put("price", product.getPrice());
        payload.put("stock", product.getStock());
        payload.put("status", product.getStatus());
        sendEvent(payload);
    }
}
