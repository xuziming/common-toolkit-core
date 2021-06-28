package com.simon.credit.toolkit.cache;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 超时淘汰的LRU(Least Recently Used:最近最少使用)缓存
 * @author xuziming 2021-05-28
 * @param <K>
 * @param <V>
 */
public class TtlLRUCache<K, V> extends LRUCache<K, V> {

    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
    private static final ScheduledExecutorService CLEANER = new ScheduledThreadPoolExecutor(CPU_NUM);

    private Map<K, CleanTask> cleanTaskMap;

    public TtlLRUCache() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public TtlLRUCache(int maxCapacity) {
        super(maxCapacity);
        cleanTaskMap = new ConcurrentHashMap<>();
    }

    /**
     * 移除年龄最大的键值对
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean isFull = super.removeEldestEntry(eldest);
        if (isFull) {
            cancelCleanTask(eldest.getKey());
        }
        return isFull;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            return null;
        }

        // 过期续约检查与执行
        scheduleExpirationRenewal((K) key);

        return value;
    }

    public V put(K key, V value, long duration, TimeUnit timeUnit) {
        V oldValue = super.put(key, value);

        if (oldValue == null) {
            // 过期调度
            scheduleExpiration(key, duration, timeUnit);
        } else {
            // 过期续约检查与执行
            scheduleExpirationRenewal((K) key);
        }

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        cancelCleanTask((K) key);
        return super.remove(key);
    }

    @Override
    public void clear() {
        for (K key : cleanTaskMap.keySet()) {
            cancelCleanTask(key);
        }
        super.clear();
    }

    private Future<?> submitCleanTask(K key, long duration, TimeUnit timeUnit) {
        Future<?> future = CLEANER.schedule(() -> remove(key), duration, timeUnit);
        cleanTaskMap.put(key, CleanTask.of(future, duration, timeUnit));
        return future;
    }

    private boolean cancelCleanTask(K key) {
        CleanTask cleanTask = cleanTaskMap.remove(key);
        if (cleanTask != null) {
            // 取消之前的清理任务
            return cleanTask.cancel();
        }
        return false;
    }

    protected void scheduleExpiration(K key, long duration, TimeUnit timeUnit) {
        // 提交新的清理任务
        submitCleanTask(key, duration, timeUnit);
    }

    protected void scheduleExpirationRenewal(K key) {
        CleanTask cleanTask = cleanTaskMap.get(key);
        if (cleanTask != null) {
            // 取消旧的清理任务
            cleanTask.cancel();
            // 提交新的清理任务
            submitCleanTask(key, cleanTask.getDuration(), cleanTask.getTimeUnit());
        }
    }

    static class CleanTask {
        private Future<?> future;
        private long duration;
        private TimeUnit timeUnit;

        public static final CleanTask of(Future<?> future, long duration, TimeUnit timeUnit) {
            CleanTask instance = new CleanTask();
            instance.future   = future;
            instance.duration = duration;
            instance.timeUnit = timeUnit;
            return instance;
        }

        /** 取消之前的清理任务 */
        public boolean cancel() {
            return future.cancel(true);
        }

        public Future<?> getFuture() {
            return future;
        }

        public long getDuration() {
            return duration;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TtlLRUCache<Integer, Integer> cache = new TtlLRUCache<>(4);
        for (int i = 1; i <= 20; i++) {
            cache.put(i, i, 15, TimeUnit.SECONDS);
        }

        TimeUnit.SECONDS.sleep(2);
        System.out.println(cache.get(17));

        TimeUnit.SECONDS.sleep(3);
        cache.put(18, 118);

        TimeUnit.SECONDS.sleep(4);
        System.out.println(cache.get(18));
    }

}