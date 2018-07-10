package com.vn.infrastructure.cache.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScoredValue;

import java.util.List;

public interface IRedisCommands {
    Object Get(Object key);

    Object Set(KeyValue<Object, Object> item);

    Object SetEX(KeyValue<Object, Object> item, long expireSeconds);

    Long Del(Object... keys);

    Long DbSize();

    Long Incr(Object key);

    Long ZAdd(Object key, ScoredValue<Object>... registries);

    Long ZCard(Object key);

    Long ZRank(KeyValue<Object, Object> item);

    List<Object> ZRange(Object key, long start, long stop, boolean withScores);
}
