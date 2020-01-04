package com.simon.credit.toolkit.cache;

import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.simon.credit.toolkit.concurrent.MyReentrantLock;
import com.simon.credit.toolkit.core.MyLinkedHashMap;

/**
 * FIFO(First In First Out:先进先出)缓存
 * <pre>
 * 可以使用LinkedHashMap进行实现，当第三个参数传入为false或是默认时，可实现按插入顺序排序，即可实现FIFO缓存.
 * </pre>
 * @author XUZIMING 2020-01-04
 */
public class FIFOCache<K, V> extends MyLinkedHashMap<K, V> {
	private static final long serialVersionUID = -6864568404777207520L;

	private static final float DEFAULT_LOAD_FACTOR  = 0.75f;
	private static final int   DEFAULT_MAX_CAPACITY = 1000;

	private final Lock lock = new MyReentrantLock();
	private volatile int maxCapacity = DEFAULT_MAX_CAPACITY;

	public FIFOCache(int maxCapacity) {
		// 第3个参数设置为true ，代表linkedlist按访问顺序排序，可作为LRU缓存
		// 第3个参数设置为false，代表linkedlist按插入顺序排序，可作为FIFO缓存
		super(16, DEFAULT_LOAD_FACTOR, true);
		this.maxCapacity = maxCapacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCapacity;// 判断当前容量是否大于最大容量
	}

	@Override
	public V put(K key, V value) {
		try {
			lock.lock();
			return super.put(key, value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V get(Object key) {
		try {
			lock.lock();
			return super.get(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V remove(Object key) {
		try {
			lock.lock();
			return super.remove(key);
		} finally {
			lock.unlock();
		}
	}

}