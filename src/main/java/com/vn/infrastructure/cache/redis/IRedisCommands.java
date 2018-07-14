package com.vn.infrastructure.cache.redis;

import java.util.List;
import java.util.Map;

public interface IRedisCommands {
    Object Get(Object key);

    Object Set(Object key, Object value);

    Object SetEX(Object key, Object value, long expireSeconds);

    Long Del(Object... keys);

    Long DbSize();

    Long Incr(Object key);

    Long ZAdd(Object key, List<Map.Entry<Double, Object>> registries);

    Long ZCard(Object key);

    Long ZRank(Object key, Object value);

    List<Object> ZRange(Object key, long start, long stop, boolean withScores);

    List<Object> ExecMultiCommands();
}
