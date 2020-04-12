package com.simon.credit.toolkit.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.concurrent.MyScheduledThreadPoolExecutor;

/**
 * 超时缓存
 * @author XUZIMING 2019-12-14
 */
public class TimeCache<K, V> {

	/** 默认超时失效时间: 60秒 */
	private static final long DEFAULT_EXPIRE_SECONDS = 60L;

	private Map<K, V> dataMap;
	private Map<K, CleanTaskInfo> cleanTaskInfoMap;

	private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
	private static final ScheduledExecutorService CLEANER = new MyScheduledThreadPoolExecutor(CPU_NUM);
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

	public TimeCache() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public TimeCache(int initialCapacity) {
		if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        dataMap = new HashMap<K, V>(initialCapacity);
        cleanTaskInfoMap = new HashMap<K, CleanTaskInfo>(initialCapacity);
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
		Runnable task = newCleanTask(key);

		// 提交定时任务: 清理数据
		ScheduledFuture<?> future = CLEANER.schedule(task, duration, timeUnit);
		cleanTaskInfoMap.put(key, new CleanTaskInfo(future, duration, timeUnit));
	}

	public Object get(K key) {
		CleanTaskInfo cleanTask = cleanTaskInfoMap.get(key);
		Future<?> future = cleanTask.getFuture();
		if (future != null) {
			future.cancel(true);
			// 加入新的清理任务
			Runnable newCleanTask = newCleanTask(key);
			future = CLEANER.schedule(newCleanTask, cleanTask.getDuration(), cleanTask.getTimeUnit());
			// 覆盖
			cleanTaskInfoMap.put(key, new CleanTaskInfo(future, cleanTask.getDuration(), cleanTask.getTimeUnit()));
		}

		return dataMap.get(key);
	}

	public Object remove(K key) {
		if (key == null) {
			return null;
		}
		if (!dataMap.containsKey(key)) {
			return null;
		}

		CleanTaskInfo taskInfo = cleanTaskInfoMap.get(key);
		if (taskInfo != null) {
			// 取消之前的清理任务
			taskInfo.getFuture().cancel(true);
			cleanTaskInfoMap.remove(key);
		}

		return dataMap.remove(key);
	}

	protected Runnable newCleanTask(final K key) {
		return new Runnable() {
			@Override
			@SuppressWarnings("rawtypes")
			public void run() {
				// System.out.println("key: " + key + " 已超时!");
				V value = dataMap.remove(key);
				// 中断正在执行的任务
				if (key   instanceof Future) ((Future) key  ).cancel(true);
				if (value instanceof Future) ((Future) value).cancel(true);
			}
		};
	}

	static class CleanTaskInfo {
		private Future<?> future;
		private long duration;
		private TimeUnit timeUnit;

		public CleanTaskInfo(Future<?> future, long duration, TimeUnit timeUnit) {
			this.future = future;
			this.duration = duration;
			this.timeUnit = timeUnit;
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