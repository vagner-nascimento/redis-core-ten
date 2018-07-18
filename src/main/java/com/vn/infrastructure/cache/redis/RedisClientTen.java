package com.vn.infrastructure.cache.redis;

import com.vn.infrastructure.cache.ICacheCommands;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RedisClientTen implements Closeable, ICacheCommands {

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private RedisCommands multiCommands = null;
    private boolean isMulti = false;

    public RedisClientTen() throws ConnectException {
        this(0, false);
    }

    public RedisClientTen(boolean isMultiCommand) throws ConnectException {
        this(0, isMultiCommand);
    }

    public RedisClientTen(int dataBase, boolean isMulti) throws ConnectException {
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
                        .withDatabase(dataBase)
                        .withTimeout(Duration.ofSeconds(15))
                        .build());
        this.connection = redisClient.connect();
        this.isMulti = isMulti;

        if (this.isMulti) {
            this.multiCommands = this.connection.sync();
            this.multiCommands.multi();
        }
    }

    @Override
    public String Set(Object key, Object value) {
        return this.SetEX(key, value, -1);
    }

    @Override
    public String SetEX(Object key, Object value, long expireSeconds) {
        SetArgs commandArgs = new SetArgs();

        if (expireSeconds > 0) commandArgs = SetArgs.Builder.ex(expireSeconds);

        String result = "";

        if (this.isMulti) {
            this.multiCommands.set(key, value, commandArgs);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            result = syncCommands.set(key, value, commandArgs);
        }
        return result;
    }

    @Override
    public Object Get(Object key) {
        Object value = "";

        if (this.isMulti) {
            this.multiCommands.get(key);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            value = syncCommands.get(key);
        }
        return value;
    }

    @Override
    public Long Del(Object... keys) {
        Long deleted = 0L;

        if (this.isMulti) {
            this.multiCommands.del(keys);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            deleted = syncCommands.del(keys);
        }
        return deleted;
    }

    @Override
    public Long DbSize() {
        Long dbSize = 0L;

        if (this.isMulti) {
            this.multiCommands.dbsize();
        } else {
            RedisCommands syncCommands = this.connection.sync();
            dbSize = syncCommands.dbsize();
        }
        return dbSize;
    }

    @Override
    public Long Incr(Object key) {
        Long incremented = 0L;

        if (this.isMulti) {
            this.multiCommands.incr(key);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            incremented = syncCommands.incr(key);
        }

        return incremented;
    }

    @Override
    public Long ZAdd(Object key, List<Map.Entry<Double, Object>> registries) {
        Long added = 0L;
        ScoredValue<Object>[] scoredValues = new ScoredValue[registries.size()];
        int index = 0;

        for (Map.Entry<Double, Object> r : registries) {
            scoredValues[index] = ScoredValue.just(r.getKey(), r.getValue());
            index++;
        }

        if (this.isMulti) {
            this.multiCommands.zadd(key, scoredValues);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            added = syncCommands.zadd(key, scoredValues);
        }
        return added;
    }

    @Override
    public Long ZCard(Object key) {
        Long found = 0L;

        if (this.isMulti) {
            this.multiCommands.zcard(key);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            found = syncCommands.zcard(key);
        }

        return found;
    }

    @Override
    public Long ZRank(Object key, Object value) {
        Long rank = 0L;

        if (this.isMulti) {
            this.multiCommands.zrank(key, value);
        } else {
            RedisCommands syncCommands = this.connection.sync();
            rank = syncCommands.zrank(key, value);
        }

        return rank;
    }

    @Override
    public List<Object> ZRange(Object key, long start, long stop, boolean withScores) {
        List rank = new ArrayList();

        if (this.isMulti) {
            if (withScores) this.multiCommands.zrangeWithScores(key, start, stop);
            else this.multiCommands.zrange(key, start, stop);
        } else {
            RedisCommands syncCommands = this.connection.sync();

            if (withScores) rank = syncCommands.zrangeWithScores(key, start, stop);
            else rank = syncCommands.zrange(key, start, stop);
        }

        return rank;
    }

    @NotNull
    @Override
    public List<Object> ExecMultiCommands() throws UnsupportedOperationException {
        if (!this.isMulti) {
            throw new UnsupportedOperationException("This instance is not Multi Command");
        }

        List<Object> results = new ArrayList<>();

        this.multiCommands.exec().forEach(r -> results.add(r));

        if (this.connection != null && this.connection.isOpen()) {
            this.multiCommands = this.connection.sync();
            this.multiCommands.multi();
        }

        return results;
    }

    @Override
    public void close() {
        if (this.connection != null && this.connection.isOpen()) {
            this.connection.flushCommands(); //Flush all commands just for guaranteed
            this.connection.close();
        }

        if (this.redisClient != null) this.redisClient.shutdown();
    }
}
