package com.demo.common.config;

import com.demo.common.cache.RedisJsonCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 公共缓存自动配置类。
 * <p>
 * 当 classpath 中存在 StringRedisTemplate 时，自动注册 {@link RedisJsonCacheHelper}。
 * 通过 {@code META-INF/spring.factories} 被 Spring Boot 自动加载。
 */
@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
public class CommonCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisJsonCacheHelper redisJsonCacheHelper(StringRedisTemplate stringRedisTemplate,
                                                      ObjectMapper objectMapper) {
        return new RedisJsonCacheHelper(stringRedisTemplate, objectMapper);
    }
}
