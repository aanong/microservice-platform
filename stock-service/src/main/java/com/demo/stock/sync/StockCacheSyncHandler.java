package com.demo.stock.sync;

import com.demo.common.cache.CacheSyncEvent;
import com.demo.common.cache.CacheSyncHandler;
import com.demo.stock.cache.RedisJsonCacheHelper;
import com.demo.stock.cache.StockCacheKeys;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StockCacheSyncHandler implements CacheSyncHandler {

    private static final String TABLE_PRODUCT = "mall_product";
    private static final String TABLE_CATEGORY = "mall_category";

    private final RedisJsonCacheHelper cacheHelper;

    public StockCacheSyncHandler(RedisJsonCacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    @Override
    public boolean supports(String table) {
        return TABLE_PRODUCT.equalsIgnoreCase(table) || TABLE_CATEGORY.equalsIgnoreCase(table);
    }

    @Override
    public void handle(CacheSyncEvent event) {
        if (TABLE_PRODUCT.equalsIgnoreCase(event.getTable())) {
            Long id = getLong(event, "id");
            if (id != null) {
                cacheHelper.delete(StockCacheKeys.productDetail(id));
            }
            cacheHelper.incrementVersion(StockCacheKeys.productListVersion());
            return;
        }

        if (TABLE_CATEGORY.equalsIgnoreCase(event.getTable())) {
            Long id = getLong(event, "id");
            if (id != null) {
                cacheHelper.delete(StockCacheKeys.categoryDetail(id));
            }
            cacheHelper.incrementVersion(StockCacheKeys.categoryListVersion());
            cacheHelper.incrementVersion(StockCacheKeys.productListVersion());
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
