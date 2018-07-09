package com.vn.infrastructure.cache.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScoredValue;

public interface IRedisCommands {
    String Get(String key);

    String Set(KeyValue<String, String> item);

    String SetEX(KeyValue<String, String> item, long expireSeconds);

    Long Del(String... keys);

    Long DbSize();

    Long Incr(String key);

    Long ZAdd(String key, ScoredValue<String>... registries);
}
