package com.simon.credit.toolkit.cache;

public class LRUCacheTest {
	
	public static void main(String[] args) {
		LRUCache<Integer, Integer> lruCache = new LRUCache<Integer, Integer>(8);
		lruCache.put(1, 1);
		lruCache.put(2, 2);
		lruCache.put(3, 3);
		System.out.println(lruCache);
		lruCache.get(1);
		System.out.println(lruCache);
		lruCache.put(4, 4);
		lruCache.put(5, 5);
		lruCache.put(6, 6);
		lruCache.put(7, 7);
		lruCache.get(1);
		lruCache.put(8, 8);
		lruCache.put(9, 9);
		lruCache.put(10, 10);
		lruCache.put(11, 11);
		lruCache.put(12, 12);
		lruCache.put(13, 13);
		lruCache.put(14, 14);
		lruCache.get(1);
		lruCache.put(15, 15);
		lruCache.put(16, 16);
		lruCache.put(17, 17);
		lruCache.put(18, 18);
		lruCache.put(19, 19);
		lruCache.put(20, 20);
		lruCache.put(21, 21);
		lruCache.get(1);
		lruCache.put(22, 22);

		
		lruCache.get(1);
		System.out.println(lruCache);
	}

}