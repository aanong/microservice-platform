package com.demo.user.cache;

public final class UserCacheKeys {

    private UserCacheKeys() {
    }

    public static String userProfile(Long userId) {
        return "mall:user:profile:" + userId;
    }
}
