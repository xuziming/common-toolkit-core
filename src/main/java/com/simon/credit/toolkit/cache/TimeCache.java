package com.simon.credit.toolkit.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.concurrent.MyScheduledThreadPoolExecutor;
import com.simon.credit.toolkit.lang.MyImmutableTriple;
import com.simon.credit.toolkit.lang.MyTriple;

/**
 * 超时缓存
 * @author XUZIMING 2019-12-14
 */
public class TimeCache {

	private static final long DEFAULT_EXPIRE_SECONDS = 30L;// 默认超时失效时间: 30秒
	private static final Map<String, Object> DATA_MAP = new HashMap<String, Object>(64);
	private static final Map<String, MyTriple<ScheduledFuture<?>, Long, TimeUnit>> TASK_MAP = 
							new HashMap<String, MyTriple<ScheduledFuture<?>, Long, TimeUnit>>(64);

	private static final ScheduledExecutorService cleaner = // 调度线程池
		new MyScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

	public static final void put(String key, Object data) {
		put(key, data, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
	}

	public static final void put(String key, Object data, long duration, TimeUnit timeUnit) {
		if (key == null || key.trim().isEmpty()) {
			throw new NullPointerException("key can not be null.");
		}

		// 缓存数据
		DATA_MAP.put(key, data);

		// 定义清理任务
		Runnable task = newCleanTask(key);

		// 提交定时任务: 清理数据
		ScheduledFuture<?> future = cleaner.schedule(task, duration, timeUnit);
		TASK_MAP.put(key, MyImmutableTriple.of(future, duration, timeUnit));
	}

	protected static final Runnable newCleanTask(final String key) {
		return new Runnable() {
			@Override
			public void run() {
				DATA_MAP.remove(key);
				System.out.println("key: " + key + " 已过期!");
			}
		};
	}

	public static final Object get(String key) {
		Object result = DATA_MAP.get(key);
		MyTriple<ScheduledFuture<?>, Long, TimeUnit> triple = TASK_MAP.remove(key);
		if (triple != null) {
			// 1、取消之前的清理任务
			ScheduledFuture<?> future = triple.getLeft();
			future.cancel(true);
			future = null;// help GC

			// 2、加入新的清理任务
			Runnable task = newCleanTask(key);
			long duration = triple.getMiddle();
			TimeUnit timeUnit = triple.getRight();
			ScheduledFuture<?> newFuture = cleaner.schedule(task, duration, timeUnit);
			TASK_MAP.put(key, MyImmutableTriple.of(newFuture, duration, timeUnit));
		}
		return result;
	}

}