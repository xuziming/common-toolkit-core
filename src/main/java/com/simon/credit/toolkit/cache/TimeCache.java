package com.simon.credit.toolkit.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 超时缓存
 * @author XUZIMING 2019-12-14
 */
public class TimeCache {

	private static final Map<String, Object> DATA_MAP = new HashMap<String, Object>(128);
	private static final Map<String, Long>   TIME_MAP = new HashMap<String, Long>(128);

	private static final long EXPIRE   = 10000L;
	private static final long START    = 10000L;
	private static final long INTERVAL = 10000L;

	private static ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

	static {
		cleaner.scheduleAtFixedRate(new Runnable() {
			public void run() {
				clean();
			}
		}, START, INTERVAL, TimeUnit.MILLISECONDS);
	}

	public static void put(String key, Object data) {
		put(key, data, EXPIRE, TimeUnit.MILLISECONDS);
	}

	public static void put(String key, Object data, long duration, TimeUnit timeUnit) {
		if (key == null || key.trim().isEmpty()) {
			throw new NullPointerException("key can not be null.");
		}
		// 缓存数据
		DATA_MAP.put(key, data);
		// 记录数据失效时间戳
		TIME_MAP.put(key, System.currentTimeMillis() + timeUnit.toMillis(duration));
	}

	public static Object get(String key) {
		return DATA_MAP.get(key);
	}

	private static void clean() {
		Iterator<Entry<String, Long>> it = TIME_MAP.entrySet().iterator();

		while (it.hasNext()) {
			Entry<String, Long> timeEntry = it.next();
			if (System.currentTimeMillis() >= timeEntry.getValue()) {
				DATA_MAP.remove(timeEntry.getKey());
				it.remove();
				System.out.println("key: " + timeEntry.getKey() + " 已过期, 清除!");
			}
		}
	}

	public static void main(String[] args) {
		TimeCache.put("adcdf", 111, 60, TimeUnit.SECONDS);
		TimeCache.put("rrrrr", 222);
		TimeCache.put("fghig", 111);
		TimeCache.put(null, 111);
	}

}