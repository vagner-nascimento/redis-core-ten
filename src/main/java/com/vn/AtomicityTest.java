package com.vn;

import com.vn.infrastructure.cache.redis.RedisClientTen;
import io.lettuce.core.KeyValue;

public class AtomicityTest {
    public static void main(String[] args) {

        try (RedisClientTen redis = new RedisClientTen()) {
            redis.Set(KeyValue.just("atomic", "off"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Thread get = new Thread(new AssyncGet(), "get");
        //get.start();

        Thread goMultiOn = new Thread(new AssyncMultiOn(), "goMultiOn");
        goMultiOn.start();

        Thread goMultiOff = new Thread(new AssyncMultiOff(), "goMultiOff");
        goMultiOff.start();

        /*
        try (RedisClientTen reisMulti = new RedisClientTen(true)) {
            Thread.sleep(500);
            reisMulti.Get("atomic");
            reisMulti.Set(KeyValue.just("atomic", "on"));
            reisMulti.ExecMultiCommands().forEach(c -> System.out.println("Multi return: " + c));
        } catch (Exception e) {
            System.out.println("main Thread" + e.getMessage());
        }
        */
    }

    public static class AssyncGet implements Runnable {
        @Override
        public void run() {
            int count = 0;
            do {
                try (RedisClientTen redis = new RedisClientTen()) {
                    System.out.println(redis.Get("atomic"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                count++;
            } while (count <= 80);
        }
    }

    public static class AssyncMultiOff implements Runnable {
        @Override
        public void run() {
            int count = 0;
            do {
                try (RedisClientTen redis = new RedisClientTen(true)) {
                    redis.Get("atomic");
                    redis.Set(KeyValue.just("atomic", "off"));
                    redis.ExecMultiCommands().forEach(r -> System.out.println(r));
                } catch (Exception e) {
                    System.out.println("Thread AssyncMultiOff: " + e.getMessage());
                }
                count++;
            } while (count <= 80);
        }
    }

    public static class AssyncMultiOn implements Runnable {
        @Override
        public void run() {
            int count = 0;
            do {
                try (RedisClientTen redis = new RedisClientTen(true)) {
                    redis.Get("atomic");
                    redis.Set(KeyValue.just("atomic", "on"));
                    redis.ExecMultiCommands().forEach(r -> System.out.println(r));
                } catch (Exception e) {
                    System.out.println("Thread AssyncMultiOn: " + e.getMessage());
                }
                count++;
            } while (count <= 80);
        }
    }
}
