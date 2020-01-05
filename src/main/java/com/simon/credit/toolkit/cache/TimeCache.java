package com.simon.credit.toolkit.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.concurrent.MyScheduledThreadPoolExecutor;

/**
 * 超时缓存
 * @author XUZIMING 2019-12-14
 */
public class TimeCache {

	private static final long DEFAULT_EXPIRE_SECONDS = 60L;// 默认超时失效时间: 60秒
	private static final Map<String, Object> DATA_MAP = new HashMap<String, Object>(64);

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

		// 提交定时任务: 清理数据
		cleaner.schedule(new Runnable() {
			@Override
			public void run() {
				DATA_MAP.remove(key);
				System.out.println("key: " + key + " 已过期, 清除!");
			}
		}, duration, timeUnit);
	}

	public static final Object get(String key) {
		return DATA_MAP.get(key);
	}

}