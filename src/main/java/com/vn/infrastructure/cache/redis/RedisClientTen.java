package com.vn.infrastructure.cache.redis;


import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.Closeable;
import java.net.ConnectException;

public class RedisClientTen implements Closeable, RedisConnection {

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
    public String set(String key, String value) {

        RedisCommands<String, String> syncCommands = this.connection.sync();

        String registry = syncCommands.set(key, value);

        this.connection.reset();

        return registry;
    }

    @Override
    public String get(String key) {

        RedisCommands<String, String> syncCommands = this.connection.sync();

        String value = syncCommands.get(key);

        this.connection.reset();

        return value;
    }

    @Override
    public void close() {
        if (this.redisClient != null) {
            this.redisClient.shutdown();
        }

        if (this.connection != null && this.connection.isOpen()) {
            this.connection.close();
        }
    }
}
