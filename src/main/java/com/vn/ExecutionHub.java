package com.vn;

import com.vn.infrastructure.cache.redis.RedisClientTen;
import com.vn.infrastructure.cache.redis.RedisCommandBuilder;
import io.lettuce.core.RedisCommandExecutionException;

import java.net.ConnectException;

public class ExecutionHub implements Runnable {
    private String command;

    public ExecutionHub() {
    }

    public ExecutionHub(String command) {
        this.command = command == null ? "" : command;
    }

    public Object Execute() throws RedisCommandExecutionException, IllegalArgumentException, ConnectException {
        return this.Execute(this.command);
    }

    public Object Execute(String command) throws RedisCommandExecutionException, IllegalArgumentException, ConnectException {

        RedisCommandBuilder commandBuilder = new RedisCommandBuilder(command);

        switch (commandBuilder.getCommand()) {
            case GET:
                if (commandBuilder.getKey() == null) {
                    return "Key not informed for GET command";
                }
                try (RedisClientTen client = new RedisClientTen()) {
                    return client.Get(commandBuilder.getKey());
                }
            case SET:
                if (commandBuilder.getKeyValue() == null) {
                    return "Key and/or Value are not informed for SET command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.Set(commandBuilder.getKeyValue());
                }
            case SETEX:
                if (commandBuilder.getKeyValue() == null || commandBuilder.getExpiration() <= 0) {
                    return "Invalid arguments for SETEX command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.SetEX(commandBuilder.getKeyValue(), commandBuilder.getExpiration());
                }
            case DEL:
                if (commandBuilder.getKeysToDelete() == null) {
                    return "No keys informed for DEL command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.Del(commandBuilder.getKeysToDelete());
                }
            case DBSIZE:
                try (RedisClientTen client = new RedisClientTen()) {
                    return client.DbSize();
                }
            case INCR:
                if (commandBuilder.getKey() == null) {
                    return "Key not informed for INCR command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.Incr(commandBuilder.getKey());
                }
            case ZADD:
                RedisClientTen lClient = null;

                try {
                    if (commandBuilder.getKey() == null || commandBuilder.getScoredValues().length <= 0) {
                        return "Invalid parameters for ZADD method";
                    }

                    lClient = new RedisClientTen();
                    return lClient.ZAdd(commandBuilder.getKey(), commandBuilder.getScoredValues());
                } finally {
                    if (lClient != null) lClient.close();
                }
            case ZCARD:
                if (commandBuilder.getKey() == null) {
                    return "Key not informed for ZCARD command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.ZCard(commandBuilder.getKey());
                }
            case ZRANK:
                if (commandBuilder.getKeyValue() == null) {
                    return "Key and/or member not informed for ZRANK command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.ZRank(commandBuilder.getKeyValue());
                }
            case ZRANGE:
                if (commandBuilder.getKey() == null
                        || commandBuilder.getStartPos() == null
                        || commandBuilder.getStopPos() == null) {
                    return "Key, start and/or stop are not informed for ZRANGE command";
                }

                try (RedisClientTen client = new RedisClientTen()) {
                    return client.ZRange(commandBuilder.getKey(),
                            commandBuilder.getStartPos(),
                            commandBuilder.getStopPos(),
                            commandBuilder.isWithScores());
                }
            default:
                return "Command not implemented";

        }
    }

    @Override
    public void run() {
        try {
            System.out.println(this.Execute());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid command, please try again...");
        } catch (RedisCommandExecutionException | ConnectException | UnsupportedOperationException eRedis) {
            System.out.println(eRedis.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
