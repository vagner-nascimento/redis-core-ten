package com.vn.infrastructure.cache.redis;

public class AtomicityTest {
    public static void main(String[] args) {

        try (RedisClientTen redis = new RedisClientTen()) {
            redis.Set("atomic", "off");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Thread getSet = new Thread(new AssyncGetSet(), "getSet");
        getSet.start();

        Thread goMultiOn = new Thread(new AssyncMultiOn(), "goMultiOn");
        goMultiOn.start();

        Thread goMultiOff = new Thread(new AssyncMultiOff(), "goMultiOff");
        goMultiOff.start();
    }

    public static class AssyncGetSet implements Runnable {
        @Override
        public void run() {
            int count = 0;
            do {
                try (RedisClientTen redis = new RedisClientTen()) {
                    System.out.println("GET: " + redis.Get("atomic"));
                    System.out.println("SET: " + redis.Set("atomic", "break"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                count++;
            } while (count <= 100);
        }
    }

    public static class AssyncMultiOff implements Runnable {
        @Override
        public void run() {
            int count = 0;
            do {
                try (RedisClientTen redis = new RedisClientTen(true)) {
                    redis.Get("atomic");
                    redis.Set("atomic", "off");
                    redis.ExecMultiCommands().forEach(r -> System.out.println("OFF: " + r));
                    redis.Get("atomic");
                    redis.Set("atomic", "off2");
                    redis.Get("atomic");
                    redis.ExecMultiCommands().forEach(r -> System.out.println("OFF 2: " + r));
                } catch (Exception e) {
                    System.out.println("Thread AssyncMultiOff: " + e.getMessage());
                }
                count++;
            } while (count <= 100);
        }
    }

    public static class AssyncMultiOn implements Runnable {
        @Override
        public void run() {
            int count = 0;
            do {
                try (RedisClientTen redis = new RedisClientTen(true)) {
                    redis.Get("atomic");
                    redis.Set("atomic", "on");
                    redis.ExecMultiCommands().forEach(r -> System.out.println("ON: " + r));
                } catch (Exception e) {
                    System.out.println("Thread AssyncMultiOn: " + e.getMessage());
                }
                count++;
            } while (count <= 100);
        }
    }
}
