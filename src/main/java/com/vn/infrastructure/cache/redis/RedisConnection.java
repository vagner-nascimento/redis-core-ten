package com.vn.infrastructure.cache.redis;


import io.lettuce.core.api.StatefulRedisConnection;

public interface RedisConnection {
    String get(String key);

    String set(String key, String value);
}
