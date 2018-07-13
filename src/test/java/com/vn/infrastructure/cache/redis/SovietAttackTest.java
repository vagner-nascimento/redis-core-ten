package com.vn.infrastructure.cache.redis;

import io.lettuce.core.KeyValue;

import java.net.ConnectException;

/**
 * A test of stress and charge with a determined number of threads.
 * Be careful, the soviets are strog guys... hehehe
 */
public class SovietAttackTest {
    public static void main(String[] args) {
        // I thing 10k was good number of threads if you will run dockerized REDIS and JVM on a normal PC with Linux :>)
        int maxThreads = 1000;
        System.out.println(String.format("FIIIREEEEEE!! Attack Initialized with %d threads", maxThreads));
        StartSet(maxThreads);
        StartGet(maxThreads);
        StartDel(maxThreads);
        StartIncr(maxThreads);
    }

    public static void StartSet(int maxThreads) {
        int count = 1;

        do {
            Thread ready = new Thread(new AssyncSet(count), "set" + count);
            ready.start();
            count++;
        } while (count <= maxThreads);
    }

    public static void StartGet(int maxThreads) {
        int count = 1;

        do {
            Thread ready = new Thread(new AssyncGet(count), "get" + count);
            ready.start();
            count++;
        } while (count <= maxThreads);
    }

    public static void StartDel(int maxThreads) {
        int count = 1;

        do {
            Thread ready = new Thread(new AssyncDel(count), "del" + count);
            ready.start();
            count++;
        } while (count <= maxThreads);
    }

    public static void StartIncr(int maxThreads) {
        int count = 1;

        do {
            Thread ready = new Thread(new AssyncIncr(), "incr" + count);
            ready.start();
            count++;
        } while (count <= maxThreads);
    }
}

class AssyncSet implements Runnable {
    private int count;

    public AssyncSet(int count) {
        this.count = count;
    }

    @Override
    public void run() {
        try (RedisClientTen redis = new RedisClientTen()) {
            System.out.println("SET: " + redis.Set(KeyValue.just(String.format("key%d", this.count), String.format("Soviet Missile %d", this.count))));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class AssyncGet implements Runnable {
    private int count;

    public AssyncGet(int count) {
        this.count = count;
    }

    @Override
    public void run() {
        try (RedisClientTen redis = new RedisClientTen()) {
            System.out.println("GET: " + redis.Get("key" + this.count));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class AssyncDel implements Runnable {
    private int count;

    public AssyncDel(int count) {
        this.count = count;
    }

    @Override
    public void run() {
        try (RedisClientTen redis = new RedisClientTen()) {
            System.out.println("DEL: " + redis.Del("key" + this.count));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class AssyncIncr implements Runnable {
    @Override
    public void run() {
        try (RedisClientTen redis = new RedisClientTen()) {
            System.out.println("INCR: " + redis.Incr("num"));
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
