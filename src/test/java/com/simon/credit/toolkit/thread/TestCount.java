package com.simon.credit.toolkit.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCount {

    private static int num = 0;

    public static void main(String[] args) {
        TestCount instance = new TestCount();

        for (int i = 0; i < 100; i++) {
            new Thread(() -> instance.count(num)).start();
        }
    }

    private static ConcurrentHashMap<Object, LockRecord> lockMap = new ConcurrentHashMap<>(16);

    public void count(Object lockObj) {
        LockRecord lock = tryLock(lockObj);
        synchronized (lock) {
            try {
                // 处理业务
                System.out.println(++num);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                unLock(lock);
            }
        }
    }

    private LockRecord tryLock(Object lockObj) {
        LockRecord curVal = new LockRecord(lockObj);
        LockRecord preVal = lockMap.putIfAbsent(lockObj, curVal);
        if (preVal == null) {// preVal为null表示lockMap不存在key为lockObj的Entry
            curVal.inc();
            return curVal;
        } else {
            preVal.inc();
            return preVal;
        }
    }

    private void unLock(LockRecord lock) {
        if (lock.dec() <= 0) {
            lockMap.remove(lock.getKey());
        }
    }

    public class LockRecord {
        private Object key = 0;
        private AtomicInteger count = new AtomicInteger(0);

        public LockRecord(Object key) {
            this.key = key;
        }

        public int inc() {
            return count.incrementAndGet();
        }

        public int dec() {
            return count.decrementAndGet();
        }

        public Object getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "LockRecord [key=" + key + ", count=" + count + "]";
        }
    }

}