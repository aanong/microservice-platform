package com.demo.stock.sync;

import com.demo.common.cache.AbstractCacheSyncConsumer;
import com.demo.common.cache.CacheSyncHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 库存服务缓存同步消费者。
 * <p>
 * 继承公共基类 {@link AbstractCacheSyncConsumer}，复用消息解析、幂等去重、延迟检测等逻辑。
 */
@Component
@RocketMQMessageListener(
    topic = "${mall.cache-sync.topic:cache-sync-stock}",
    consumerGroup = "${mall.cache-sync.consumer-group:stock-cache-sync-group}")
public class StockCacheSyncConsumer extends AbstractCacheSyncConsumer {

    private final String topic;
    private final String consumerGroup;

    public StockCacheSyncConsumer(ObjectMapper objectMapper,
                                  StringRedisTemplate stringRedisTemplate,
                                  List<CacheSyncHandler> handlers,
                                  @org.springframework.beans.factory.annotation.Value("${mall.cache-sync.topic:cache-sync-stock}") String topic,
                                  @org.springframework.beans.factory.annotation.Value("${mall.cache-sync.consumer-group:stock-cache-sync-group}") String consumerGroup) {
        super(objectMapper, stringRedisTemplate, handlers);
        this.topic = topic;
        this.consumerGroup = consumerGroup;
    }

    @Override
    protected String getServiceName() {
        return "stock";
    }

    @Override
    protected String getTopic() {
        return topic;
    }

    @Override
    protected String getConsumerGroup() {
        return consumerGroup;
    }
}
