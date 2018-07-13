package com.vn.infrastructure.cache.redis;

import io.lettuce.core.KeyValue;
import io.lettuce.core.ScoredValue;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class CommandsTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Get found results:\nAdded and get \"test\" value\n" + GetFound("test"));
        System.out.println("Get not found results:\n" + GetNotFound());
        System.out.println("Set results:\n" + SetOk());
        SetEXTest();
        DelTest();
        DbSizeTest();
        IncrTest();
        ZCommandsTests();
    }

    public static void ZCommandsTests() {
        List<String> results = new ArrayList<>();

        try (RedisClientTen redis = new RedisClientTen(true)) {
            String key = String.format("key-%s", System.nanoTime());
            redis.ZAdd(key,
                    ScoredValue.just(1, "test1")
                    , ScoredValue.just(2, "test2")
                    , ScoredValue.just(3, "test3")
                    , ScoredValue.just(4, "test4")
                    , ScoredValue.just(5, "test5"));
            redis.ZCard(key);
            redis.ZRank(KeyValue.just(key, "test4"));
            redis.ZRange(key, 1, 3, false);
            redis.Del(key);
            redis.ExecMultiCommands().forEach(r -> results.add(String.valueOf(r)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print("\nZ Commands results:\nCommands order: ZAdd 5 values [test1, ..., test5], ZCard, ");
        System.out.println("ZRank on value 'test4', ZRange from 1 to 3, Del scored values");
        results.forEach(r -> System.out.println(r));
    }

    public static void IncrTest() {
        List<String> results = new ArrayList<>();

        try (RedisClientTen redis = new RedisClientTen(true)) {
            String key = String.format("key%s", System.nanoTime());
            redis.Incr(key);
            redis.Incr(key);
            redis.Incr(key);
            redis.Incr(key);
            redis.Incr(key);
            redis.Del(key);

            redis.ExecMultiCommands().forEach(r -> results.add(String.valueOf(r)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nIncr results:\nIncremented a value 5 times and deleted it");
        results.forEach(r -> System.out.println(r));
    }

    public static void DelTest() {
        List<String> results = Del(5);
        System.out.println("\nDel results:\nAdded and deleted 5 values");
        results.forEach(r -> System.out.println(r));
    }

    public static void SetEXTest() throws InterruptedException {
        List<String> results = SetEx(5);
        System.out.println("SetEX results of commands SetEX and Get:");
        results.forEach(i -> {
            if (results.indexOf(i) != 0) System.out.println(i);
        });
        System.out.println("\"test\" is setted up for 5 seconds, lets wait...");
        Thread.sleep(5000);
        String notFound = "";
        System.out.println("Ok, lets try take it now...");

        try (RedisClientTen redis = new RedisClientTen()) {
            notFound = String.valueOf(redis.Get(results.get(0)));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(notFound);
        System.out.println("... ops, it's gone!");
    }

    public static void DbSizeTest() {
        List<String> results = new ArrayList<>();
        results.add("\nDbSize results:\nGet DbSize, set 5 values, get DbSize again, and delete values");

        try (RedisClientTen redis = new RedisClientTen(true)) {
            String[] keys = new String[5];
            redis.DbSize();
            for (int i = 0; i < 5; i++) {
                keys[i] = String.format("key-%s", System.nanoTime());
                redis.Set(KeyValue.just(keys[i], "test"));
            }
            redis.DbSize();
            redis.Del(keys);
            redis.ExecMultiCommands().forEach(r -> results.add(String.valueOf(r)));

            results.forEach(r -> System.out.println(r));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String GetFound(String value) {
        String response = "";
        try (RedisClientTen redis = new RedisClientTen(true)) {
            String key = String.format("test-%s", System.nanoTime());
            redis.Set(KeyValue.just(key, value));
            redis.Get(key);
            redis.Del(key);
            List<Object> responseValues = new ArrayList<>();
            redis.ExecMultiCommands().forEach(val -> {
                responseValues.add(val);
            });

            if (responseValues.size() >= 2) response = String.valueOf(responseValues.get(1));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String GetNotFound() {
        String response = "";
        try (RedisClientTen redis = new RedisClientTen(true)) {
            String key = String.format("test-%s", System.nanoTime());
            redis.Del(key);
            redis.Get(key);
            List<Object> responseValues = new ArrayList<>();
            redis.ExecMultiCommands().forEach(val -> {
                responseValues.add(val);
            });

            if (responseValues.size() >= 2) response = String.valueOf(responseValues.get(1));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String SetOk() {
        String response = "";
        try (RedisClientTen redis = new RedisClientTen(true)) {
            String key = String.format("test-%s", System.nanoTime());
            redis.Set(KeyValue.just(key, "test"));
            redis.Del(key);
            List<Object> responseValues = new ArrayList<>();
            redis.ExecMultiCommands().forEach(val -> {
                responseValues.add(val);
            });

            if (responseValues.size() >= 1) response = String.valueOf(responseValues.get(0));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static List<String> SetEx(long expire) {
        List<String> response = new ArrayList<>();
        try (RedisClientTen redis = new RedisClientTen(true)) {
            String key = String.format("test-%s", System.nanoTime());
            response.add(key);
            redis.SetEX(KeyValue.just(key, "test"), expire);
            redis.Get(key);
            redis.ExecMultiCommands().forEach(val -> {
                response.add(String.valueOf(val));
            });
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static List<String> Del(int keysQt) {
        List<String> responseValues = new ArrayList<>();

        try (RedisClientTen redis = new RedisClientTen(true)) {
            String[] keys = new String[keysQt];

            for (int i = 0; i < keysQt; i++) {
                keys[i] = String.format("test-%s", System.nanoTime());
                redis.Set(KeyValue.just(keys[i], "test"));
            }

            redis.Del(keys);
            redis.ExecMultiCommands().forEach(val -> {
                responseValues.add(String.valueOf(val));
            });
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseValues;
    }
}
