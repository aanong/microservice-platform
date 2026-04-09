package com.demo.user.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncEventUtils;
import com.demo.common.cache.CacheSyncHandler;
import com.demo.common.cache.RedisJsonCacheHelper;
import com.demo.user.cache.UserCacheKeys;
import org.springframework.stereotype.Component;

/**
 * 用户服务缓存同步处理器。
 * <p>
 * 改用公共模块的 {@link RedisJsonCacheHelper} 替代直接使用 StringRedisTemplate，
 * 使用 {@link CacheSyncEventUtils} 消除重复工具方法。
 */
@Component
public class UserCacheSyncHandler implements CacheSyncHandler {

    private final RedisJsonCacheHelper cacheHelper;

    public UserCacheSyncHandler(RedisJsonCacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public boolean supports(String table) {
        return "user_account".equalsIgnoreCase(table);
    }

    @Override
    public void handle(CacheSyncEvent event) {
        Long userId = CacheSyncEventUtils.getLong(event, "id");
        if (userId != null) {
            cacheHelper.delete(UserCacheKeys.userProfile(userId));
        }
    }
}
