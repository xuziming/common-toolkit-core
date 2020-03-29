package com.simon.credit.toolkit.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.concurrent.MyScheduledThreadPoolExecutor;

/**
 * 超时缓存
 * 
 * @author XUZIMING 2019-12-14
 */
public class TimeCache {

	private static final long DEFAULT_EXPIRE_SECONDS = 60L;// 默认超时失效时间: 60秒
	private static final Map<String, Object> DATA_MAP = new HashMap<String, Object>(64);
	private static final Map<String, TaskInfo> TASK_MAP = new HashMap<String, TaskInfo>(64);

	private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();
	private static final ScheduledExecutorService CLEANER = new MyScheduledThreadPoolExecutor(CPU_NUM);

	public static final void put(String key, Object data) {
		put(key, data, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
	}

	public static final void put(String key, Object data, long duration, TimeUnit timeUnit) {
		if (key == null || key.trim().isEmpty()) {
			throw new NullPointerException("key can not be null.");
		}

		if (DATA_MAP.containsKey(key)) {
			remove(key);
		}

		// 缓存数据
		DATA_MAP.put(key, data);

		// 定义清理任务
		Runnable task = newCleanTask(key);

		// 提交定时任务: 清理数据
		ScheduledFuture<?> future = CLEANER.schedule(task, duration, timeUnit);
		TASK_MAP.put(key, new TaskInfo(future, duration, timeUnit));
	}

	protected static final Runnable newCleanTask(final String key) {
		return new Runnable() {
			@Override
			public void run() {
				DATA_MAP.remove(key);
				// System.out.println("key: " + key + " 已过期!");
			}
		};
	}

	public static final Object get(String key) {
		TaskInfo taskInfo = TASK_MAP.get(key);
		ScheduledFuture<?> future = taskInfo.getFuture();
		if (future != null) {
			future.cancel(true);
			// 加入新的清理任务
			Runnable task = newCleanTask(key);
			future = CLEANER.schedule(task, taskInfo.getDuration(), taskInfo.getTimeUnit());
			// 覆盖
			TASK_MAP.put(key, new TaskInfo(future, taskInfo.getDuration(), taskInfo.getTimeUnit()));
		}

		return DATA_MAP.get(key);
	}

	public static final Object remove(String key) {
		if (key == null || key.trim().isEmpty()) {
			return null;
		}
		if (!DATA_MAP.containsKey(key)) {
			return null;
		}

		TaskInfo taskInfo = TASK_MAP.get(key);
		if (taskInfo != null) {
			// 取消之前的清理任务
			taskInfo.getFuture().cancel(true);
			TASK_MAP.remove(key);
		}

		return DATA_MAP.remove(key);
	}

	static class TaskInfo {
		private ScheduledFuture<?> future;
		private long duration;
		private TimeUnit timeUnit;

		public TaskInfo(ScheduledFuture<?> future, long duration, TimeUnit timeUnit) {
			this.future = future;
			this.duration = duration;
			this.timeUnit = timeUnit;
		}

		public ScheduledFuture<?> getFuture() {
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