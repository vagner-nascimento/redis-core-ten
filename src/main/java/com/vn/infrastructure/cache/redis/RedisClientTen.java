package com.vn.infrastructure.cache.redis;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.Closeable;
import java.net.ConnectException;
import java.util.List;

public final class RedisClientTen implements Closeable, IRedisCommands {

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;

    public RedisClientTen() throws ConnectException {
        this(0);
    }

    public RedisClientTen(int dataBase) throws ConnectException {
        String redisServer = System.getenv("REDIS_SERVER");

        if (redisServer == null || redisServer.equals("")) {
            throw new ConnectException("The REDIS_SERVER environment var is not seted");
        }

        int redisPort = System.getenv("REDIS_PORT") == null || System.getenv("REDIS_PORT").equals("") ?
                6379 :
                Integer.valueOf(System.getenv("REDIS_PORT"));

        String redisPassword = System.getenv("REDIS_PASSWORD") == null ?
                new String("") :
                System.getenv("REDIS_PASSWORD");

        this.redisClient = RedisClient
                .create(RedisURI.
                        Builder.
                        redis(redisServer, redisPort)
                        .withPassword(redisPassword)
                        .withDatabase(dataBase).build());

        this.connection = redisClient.connect();
    }

    @Override
    public String Set(KeyValue<Object, Object> item) {
        return this.SetEX(item, -1);
    }

    @Override
    public String SetEX(KeyValue<Object, Object> item, long expireSeconds) {
        SetArgs commandArgs = new SetArgs();

        if (expireSeconds > 0) {
            commandArgs = SetArgs.Builder.ex(expireSeconds);
        }

        RedisCommands syncCommands = this.connection.sync();
        String result = syncCommands.set(item.getKey(), item.getValue(), commandArgs);
        this.connection.reset();
        return result;
    }

    @Override
    public Object Get(Object key) {
        RedisCommands syncCommands = this.connection.sync();
        Object value = syncCommands.get(key);
        this.connection.reset();
        return value;
    }

    @Override
    public Long Del(Object... keys) {
        RedisCommands syncCommands = this.connection.sync();
        Long deleted = syncCommands.del(keys);
        this.connection.reset();
        return deleted;
    }

    @Override
    public Long DbSize() {
        RedisCommands syncCommands = this.connection.sync();
        Long dbSize = syncCommands.dbsize();
        this.connection.reset();
        return dbSize;
    }

    @Override
    public Long Incr(Object key) {
        RedisCommands syncCommands = this.connection.sync();
        Long incremented = syncCommands.incr(key);
        this.connection.reset();
        return incremented;
    }

    @Override
    public Long ZAdd(Object key, ScoredValue<Object>... registries) {
        RedisCommands syncCommands = this.connection.sync();
        Long added = syncCommands.zadd(key, registries);
        this.connection.reset();
        return added;
    }

    @Override
    public Long ZCard(Object key) {
        RedisCommands syncCommands = this.connection.sync();
        Long found = syncCommands.zcard(key);
        this.connection.reset();
        return found;
    }

    @Override
    public Long ZRank(KeyValue<Object, Object> item) {
        RedisCommands syncCommands = this.connection.sync();
        Long rank = syncCommands.zrank(item.getKey(), item.getValue());
        this.connection.reset();
        return rank;
    }

    @Override
    public List<Object> ZRange(Object key, long start, long stop, boolean withScores) {
        RedisCommands syncCommands = this.connection.sync();
        List rank;

        if (withScores) rank = syncCommands.zrangeWithScores(key, start, stop);
        else rank = syncCommands.zrange(key, start, stop);

        this.connection.reset();
        return rank;
    }

    @Override
    public void close() {
        if (this.connection != null && this.connection.isOpen()) {
            this.connection.close();
        }

        if (this.redisClient != null) {
            this.redisClient.shutdown();
        }
    }
}
