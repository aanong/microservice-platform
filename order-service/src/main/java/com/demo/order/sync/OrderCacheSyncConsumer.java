package com.demo.order.sync;

import com.demo.common.cache.AbstractCacheSyncConsumer;
import com.demo.common.cache.CacheSyncHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单服务缓存同步消费者。
 * <p>
 * 继承公共基类 {@link AbstractCacheSyncConsumer}，复用消息解析、幂等去重、延迟检测等逻辑。
 */
@Component
@RocketMQMessageListener(
    topic = "${mall.cache-sync.topic:cache-sync-order}",
    consumerGroup = "${mall.cache-sync.consumer-group:order-cache-sync-group}")
public class OrderCacheSyncConsumer extends AbstractCacheSyncConsumer {

    public OrderCacheSyncConsumer(ObjectMapper objectMapper,
                                  StringRedisTemplate stringRedisTemplate,
                                  List<CacheSyncHandler> handlers) {
        super(objectMapper, stringRedisTemplate, handlers);
    }

    @Override
    protected String getServiceName() {
        return "order";
    }
}
