package com.simon.credit.toolkit.concurrent;

public class MyConcurrentHashMapTest {
	
	public static void main(String[] args) {
		MyConcurrentHashMap<String, String> map = new MyConcurrentHashMap<String, String>(16);
		map.put("aaa", "111");
		map.put("bbb", "222");
		map.put("ccc", "333");
		System.out.println(map.size());
	}

}