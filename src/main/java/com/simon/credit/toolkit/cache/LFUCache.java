package com.simon.credit.toolkit.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * LFU(Least Frequently Used:最不经常使用)缓存
 * <pre>在一段时间内，数据被使用次数最少的，优先被淘汰</pre>
 * @author XUZIMING 2020-01-04
 */
public class LFUCache<K, V> {

	private Map<K, V> mapOfKeyAndValue;
	private Map<K, Integer> mapOfKeyAndVisitCount;
	private Map<Integer, Set<K>> mapOfVisitCountAndKeySet;

	private int capacity;
	private int leastFrequentlyUsed;// 最少使用次数(默认初始化为-1)

	public LFUCache(int capacity) {
		this.capacity = capacity;
		this.leastFrequentlyUsed = -1;
		mapOfKeyAndValue = new HashMap<K, V>();
		mapOfKeyAndVisitCount = new HashMap<K, Integer>();
		mapOfVisitCountAndKeySet = new HashMap<Integer, Set<K>>();
		mapOfVisitCountAndKeySet.put(1, new LinkedHashSet<K>());
	}

	public V get(K key) {
		if (!mapOfKeyAndValue.containsKey(key)) {
			return null;
		}

		int count = mapOfKeyAndVisitCount.get(key);
		mapOfKeyAndVisitCount.put(key, count + 1);// 访问次数加1

		mapOfVisitCountAndKeySet.get(count).remove(key);// 原key被访问之后从原次桶中移除
		if (count == leastFrequentlyUsed && mapOfVisitCountAndKeySet.get(count).size() == 0) {
			leastFrequentlyUsed++;// 原最小使用次数对应的桶为空，则最小使用次数桶往下移
		}

		if (!mapOfVisitCountAndKeySet.containsKey(count + 1)) {// 下移之后必须保证集合非空
			mapOfVisitCountAndKeySet.put(count + 1, new LinkedHashSet<K>());
		}
		mapOfVisitCountAndKeySet.get(count + 1).add(key);// 往下移动并放入

		return mapOfKeyAndValue.get(key);
	}

	public void put(K key, V value) {
		if (capacity <= 0) {
			return;
		}

		if (mapOfKeyAndValue.containsKey(key)) {
			mapOfKeyAndValue.put(key, value);
			get(key);
			return;
		}

		if (mapOfKeyAndValue.size() >= capacity) {// 超过最大容量，必须先按照策略移除元素
			K lfu = mapOfVisitCountAndKeySet.get(leastFrequentlyUsed).iterator().next();
			mapOfKeyAndValue.remove(lfu);
			mapOfKeyAndVisitCount.remove(lfu);
			mapOfVisitCountAndKeySet.get(leastFrequentlyUsed).remove(lfu);
		}

		mapOfKeyAndValue.put(key, value);
		mapOfKeyAndVisitCount.put(key, 1);
		mapOfVisitCountAndKeySet.get(1).add(key);
		leastFrequentlyUsed = 1;
	}

}