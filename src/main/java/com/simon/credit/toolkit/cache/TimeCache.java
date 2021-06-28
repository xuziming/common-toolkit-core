package com.simon.credit.toolkit.cache;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 超时缓存
 * @author XUZIMING 2019-12-14
 */
public class TimeCache<K, V> {

	/** 最大容量值 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	/** 默认超时失效时间: 60秒 */
	private static final long DEFAULT_EXPIRE_SECONDS = 60L;
	/** 过期数据清洁工 */
	private static final ScheduledExecutorService CLEANER =
			               new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

	private Map<K, V> dataMap;
	private Map<K, CleanTask> cleanTaskMap;

	public TimeCache() {
		this(16);
	}

	public TimeCache(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY) {
			initialCapacity = MAXIMUM_CAPACITY;
		}
		dataMap = new ConcurrentHashMap<>(initialCapacity);
		cleanTaskMap = new ConcurrentHashMap<>(initialCapacity);
	}

	public void put(K key, V data) {
		put(key, data, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
	}

	public void put(K key, V data, long duration, TimeUnit timeUnit) {
		if (key == null) {
			throw new NullPointerException("key can not be null.");
		}

		if (dataMap.containsKey(key)) {
			remove(key);
		}

		// 缓存数据
		dataMap.put(key, data);

		// 定义清理任务
		Runnable task = () -> remove(key);

		// 提交定时任务: 清理数据
		ScheduledFuture<?> future = CLEANER.schedule(task, duration, timeUnit);
		cleanTaskMap.put(key, CleanTask.of(future, duration, timeUnit));
	}

	public Object get(K key) {
		CleanTask oldCleanTask = cleanTaskMap.get(key);
		if (oldCleanTask == null) {
			return dataMap.get(key);
		}

		Future<?> future = oldCleanTask.getFuture();
		// 取消旧的清理任务
		future.cancel(true);
		// 加入新的清理任务
		future = CLEANER.schedule(() -> remove(key), oldCleanTask.getDuration(), oldCleanTask.getTimeUnit());
		// 覆盖任务
		cleanTaskMap.put(key, CleanTask.of(future, oldCleanTask.getDuration(), oldCleanTask.getTimeUnit()));

		return dataMap.get(key);
	}

	public Object remove(K key) {
		if (key == null) {
			return null;
		}

		CleanTask cleanTask = cleanTaskMap.remove(key);
		if (cleanTask != null) {
			// 取消之前的清理任务
			cleanTask.getFuture().cancel(true);
		}

		return dataMap.remove(key);
	}

	static class CleanTask {
		private Future<?> future;
		private long duration;
		private TimeUnit timeUnit;

		public static final CleanTask of(Future<?> future, long duration, TimeUnit timeUnit) {
			CleanTask instance = new CleanTask();
			instance.future = future;
			instance.duration = duration;
			instance.timeUnit = timeUnit;
			return instance;
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

}