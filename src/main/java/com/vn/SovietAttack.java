package com.vn;

public class SovietAttack {
    public static void main(String[] args) throws InterruptedException {
        /* 10k was maximum safe threads for my old DELL notebook with Intel Core i5 and 6GB of ram running REDIS and JVM
         * on Ubuntu 18.4 (more than that it dies)
         */
        int maxThreads = 10000;
        System.out.println(String.format("FIIIREEEEEE!! Attack Initialized with %d threads", maxThreads));
        long start = System.nanoTime();
        //StartSet(maxThreads); //1000 = 8 sec - 10000 = 31 sec
        //StartGet(maxThreads); //1000 = 7 sec = 10000 = 32 sec
        //StartDel(maxThreads); //1000 = 7 dec - 10000 = 33 sec
        //StartIncr(maxThreads); //10000 = 34 sec
        System.out.println("Remaning time in milis: " + Math.round((System.nanoTime() - start) / 1000000));
    }

    public static void StartSet(int maxThreads) {
        int count = 1;
        StringBuffer auxStr = new StringBuffer();

        do {
            auxStr.append(String.format("set key%d Cool Value%d", count, count));
            Thread ready = new Thread(new ExecutionHub(auxStr.toString()), "ready" + count);
            ready.start();
            auxStr.delete(0, auxStr.length());
            count++;
        } while (count <= maxThreads);

    }

    public static void StartGet(int maxThreads) {
        int count = 1;
        StringBuffer auxStr = new StringBuffer();

        do {
            auxStr.append(String.format("get key%d", count));
            Thread ready = new Thread(new ExecutionHub(auxStr.toString()), "ready" + count);
            ready.start();
            auxStr.delete(0, auxStr.length());
            count++;
        } while (count <= maxThreads);
    }

    public static void StartDel(int maxThreads) {
        int count = 1;
        StringBuffer auxStr = new StringBuffer();

        do {
            auxStr.append(String.format("del key%d", count));
            Thread ready = new Thread(new ExecutionHub(auxStr.toString()), "ready" + count);
            ready.start();
            auxStr.delete(0, auxStr.length());
            count++;
        } while (count <= maxThreads);
    }

    public static void StartIncr(int maxThreads) {
        int count = 1;
        StringBuffer auxStr = new StringBuffer();

        do {
            auxStr.append("incr incr-test");
            Thread ready = new Thread(new ExecutionHub(auxStr.toString()), "ready" + count);
            ready.start();
            auxStr.delete(0, auxStr.length());
            count++;
        } while (count <= maxThreads);
    }
}
