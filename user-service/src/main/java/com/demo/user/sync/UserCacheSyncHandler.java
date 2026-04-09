package com.demo.user.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncHandler;
import com.demo.user.cache.UserCacheKeys;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserCacheSyncHandler implements CacheSyncHandler {

    private final StringRedisTemplate stringRedisTemplate;

    public UserCacheSyncHandler(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean supports(String table) {
        return "user_account".equalsIgnoreCase(table);
    }

    @Override
    public void handle(CacheSyncEvent event) {
        Long userId = getLong(event, "id");
        if (userId != null) {
            stringRedisTemplate.delete(UserCacheKeys.userProfile(userId));
        }
    }

    private Long getLong(CacheSyncEvent event, String key) {
        Object value = pick(event.getAfter(), key);
        if (value == null) {
            value = pick(event.getBefore(), key);
        }
        if (value == null && event.getPk() != null) {
            value = event.getPk().get(key);
        }
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Object pick(Map<String, Object> data, String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }
}
