package com.simon.credit.toolkit.cache;

import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.lang.ThreadToolkits;

public class TimeCacheTest {

	public static void main(String[] args) {
		TimeCache.put("abc", "abc", 20, TimeUnit.SECONDS);
		TimeCache.put("def", "def");
		TimeCache.put("123", 123);

		ThreadToolkits.sleep(21, TimeUnit.SECONDS);
		TimeCache.get("123");
	}

}