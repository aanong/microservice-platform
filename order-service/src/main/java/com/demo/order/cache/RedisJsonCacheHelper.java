package com.demo.order.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisJsonCacheHelper {

    private static final String NULL_PLACEHOLDER = "__NULL__";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.ttl.detail-seconds:300}")
    private long detailTtlSeconds;

    @Value("${cache.ttl.list-seconds:60}")
    private long listTtlSeconds;

    @Value("${cache.ttl.null-seconds:30}")
    private long nullTtlSeconds;

    public RedisJsonCacheHelper(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T getObject(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        if (NULL_PLACEHOLDER.equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ex) {
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    public <T> List<T> getList(String key, TypeReference<List<T>> typeReference) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        if (NULL_PLACEHOLDER.equals(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception ex) {
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    public void setDetail(String key, Object value) {
        setJson(key, value, Duration.ofSeconds(detailTtlSeconds));
    }

    public void setList(String key, Object value) {
        setJson(key, value, Duration.ofSeconds(listTtlSeconds));
    }

    public void setString(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void setNull(String key) {
        stringRedisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, Duration.ofSeconds(nullTtlSeconds));
    }

    public Long incrementVersion(String versionKey) {
        return stringRedisTemplate.opsForValue().increment(versionKey);
    }

    public String getVersion(String versionKey) {
        String value = stringRedisTemplate.opsForValue().get(versionKey);
        return value == null ? "1" : value;
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    private void setJson(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception ignored) {
            // Keep business flow non-blocking when cache serialization fails.
        }
    }
}
