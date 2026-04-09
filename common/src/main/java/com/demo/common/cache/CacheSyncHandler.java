package com.demo.common.cache;

public interface CacheSyncHandler {

    boolean supports(String table);

    void handle(CacheSyncEvent event);
}
