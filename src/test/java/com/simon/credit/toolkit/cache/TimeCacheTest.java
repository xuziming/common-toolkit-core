package com.simon.credit.toolkit.cache;

import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.lang.ThreadToolkits;

public class TimeCacheTest {

	public static void main(String[] args) {
		TimeCache<String, Object> timeCache = new TimeCache<String, Object>(8);
		timeCache.put("abc", "abc", 8, TimeUnit.SECONDS);
		timeCache.put("def", "def");
		timeCache.put("123", 123);

		ThreadToolkits.sleep(10, TimeUnit.SECONDS);
		System.out.println(timeCache.get("abc"));
		System.out.println(timeCache.get("123"));
	}

}