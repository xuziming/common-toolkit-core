package com.simon.credit.toolkit.cache;

import com.simon.credit.toolkit.core.MyLinkedHashMap;

import java.util.Map;

/**
 * LRU(Least Recently Used:最近最少使用)缓存
 * @author XUZIMING 2019-11-19
 */
public class LRUCache<K, V> extends MyLinkedHashMap<K, V> {
	private static final long serialVersionUID = -5167631809472116969L;

	private static final float DEFAULT_LOAD_FACTOR  = 0.75f;
	protected static final int DEFAULT_MAX_CAPACITY = 1000;

	protected volatile int maxCapacity;

	public LRUCache() {
		this(DEFAULT_MAX_CAPACITY);
	}

	public LRUCache(int maxCapacity) {
		// 第3个参数设置为true ，代表linkedlist按访问顺序排序，可作为LRU缓存
		// 第3个参数设置为false，代表linkedlist按插入顺序排序，可作为FIFO缓存
		super(16, DEFAULT_LOAD_FACTOR, true);
		this.maxCapacity = maxCapacity;
	}

	/**
	 * 移除年龄最大的键值对
	 */
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCapacity;// 判断当前容量是否大于最大容量
	}

}