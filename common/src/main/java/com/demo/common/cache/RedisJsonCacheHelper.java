package com.demo.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 统一的 Redis JSON 缓存工具类。
 * <p>
 * 合并了 order-service 和 stock-service 中的同名类，
 * 提供对象/列表的 JSON 序列化缓存、空值穿透保护、版本号管理等功能。
 * <p>
 * 通过 {@link com.demo.common.config.CommonCacheAutoConfiguration} 自动注册为 Spring Bean。
 */
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

    // ===== 对象缓存 =====

    /**
     * 从缓存中获取 JSON 对象，若值为空占位符或反序列化失败则自动清理
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || NULL_PLACEHOLDER.equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ex) {
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 从缓存中获取 JSON 列表，若值为空占位符则返回空列表
     */
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

    // ===== 写入缓存 =====

    /**
     * 以"详情"级别 TTL 写入缓存
     */
    public void setDetail(String key, Object value) {
        setJson(key, value, Duration.ofSeconds(detailTtlSeconds));
    }

    /**
     * 以"列表"级别 TTL 写入缓存
     */
    public void setList(String key, Object value) {
        setJson(key, value, Duration.ofSeconds(listTtlSeconds));
    }

    /**
     * 以自定义 TTL 写入缓存
     */
    public void setJson(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception ignored) {
            // 缓存序列化失败时不阻塞业务流程
        }
    }

    // ===== 字符串缓存（非 JSON） =====

    /**
     * 写入普通字符串值
     */
    public void setString(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 读取普通字符串值
     */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // ===== 空值穿透保护 =====

    /**
     * 设置空值占位符，防止缓存穿透
     */
    public void setNull(String key) {
        stringRedisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, Duration.ofSeconds(nullTtlSeconds));
    }

    // ===== 版本号管理（用于列表缓存过期策略） =====

    /**
     * 递增版本号并返回新值
     */
    public Long incrementVersion(String versionKey) {
        return stringRedisTemplate.opsForValue().increment(versionKey);
    }

    /**
     * 获取当前版本号，不存在时返回 "1"
     */
    public String getVersion(String versionKey) {
        String value = stringRedisTemplate.opsForValue().get(versionKey);
        return value == null ? "1" : value;
    }

    // ===== 删除 =====

    /**
     * 删除缓存键
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
