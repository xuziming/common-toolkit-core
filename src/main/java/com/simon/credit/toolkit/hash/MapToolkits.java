package com.simon.credit.toolkit.hash;

import com.simon.credit.toolkit.common.CommonToolkits;
import com.simon.credit.toolkit.reflect.DataFetcher;
import com.simon.credit.toolkit.reflect.NotNullDataFetcher;
import com.simon.credit.toolkit.reflect.PropertyToolkits;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map工具类
 * @author XUZIMING 2018-07-28
 */
public class MapToolkits {

	public static final int   DEFAULT_INITIAL_CAPACITY 	= 1 << 4;// 16
	public static final int   MAX_POWER_OF_TWO 			= 1 << (Integer.SIZE - 2);
	public static final float DEFAULT_LOAD_FACTOR 		= 0.75f;

	public static <K, V> Map<K, V> newHashMap() {
		return new HashMap<K, V>(DEFAULT_INITIAL_CAPACITY);
	}

	/**
	 * HashMap构造方法new HashMap(4), 本希望设置初始值, 避免扩容(resize)开销. 
	 * 但没有考虑当元素数量达到容量的75%时将出现resize的情况.
	 * @param expectedSize
	 */
	public static <K, V> Map<K, V> newHashMap(int expectedSize) {
		return new HashMap<K, V>(capacity(expectedSize));
	}

	public static <K, V> Map<K, V> initHashMap(K key, V value) {
		Map<K, V> map = newHashMap();
		map.put(key, value);
		return map;
	}

	public static <K, V> Map<K, V> initHashMap(K key, V value, int expectedSize) {
		Map<K, V> map = newHashMap(expectedSize);
		map.put(key, value);
		return map;
	}

	public static <K, V> Map<K, V> stableMap(int expectedSize) {
		int capacity = 2;
		while (capacity < expectedSize) {
			capacity <<= 1;
		}
		return new HashMap<>(capacity, 1.0f);
	}

	private static int capacity(int expectedSize) {
		if (expectedSize < 3) {
			checkNonnegative(expectedSize, "expectedSize");
			return expectedSize + 1;
		}

		if (expectedSize >= MAX_POWER_OF_TWO) {
			return MAX_POWER_OF_TWO;
		}

		int capacity = 2;
		while (capacity < expectedSize) {
			capacity <<= 1;
		}

		return capacity < MAX_POWER_OF_TWO ? capacity : MAX_POWER_OF_TWO;
	}

	/**
	 * 检查容量值是否正数(大于0的数)
	 * @param value
	 * @param name
	 * @return
	 */
	private static int checkNonnegative(int value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
		}
		return value;
	}

	/**
	 * 转为线程安全的Map
	 * @param map 原始的(线程不安全的)Map
	 * @return ConcurrentHashMap(效率高)或SynchronizedMap(效率一般)
	 */
	public static <K, V> Map<K, V> threadSafeMap(Map<K, V> map) {
		// 1、效率一般
		// return Collections.synchronizedMap(map);

		// 2、效率较高
		return new ConcurrentHashMap<K, V>(map);
	}

	/**
	 * 将集合解析为Map
	 * @param coll 对象集合
	 * @param dataFetcher key值抓取接口
	 * @return
	 */
	public static <K, V> Map<K, V> parseMap(Collection<V> coll, DataFetcher<V, K> dataFetcher) {
		if (CommonToolkits.isEmpty(coll)) {
			return null;
		}

		Map<K, V> map = newHashMap(coll.size());
		for (V element : coll) {
			K key = dataFetcher.fetch(element);
			if (dataFetcher instanceof NotNullDataFetcher && key == null) {
				continue;
			}
			map.put(key, element);
		}

		return map;
	}

	/**
	 * 将集合解析为Map
	 * @param coll 对象集合
	 * @param keyProperty key列对应的属性名
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static <K, V> Map<K, V> parseMap(Collection<V> coll, String keyProperty) {
		if (CommonToolkits.isEmpty(coll)) {
			return null;
		}

		Map<K, V> map = newHashMap(coll.size());
		for (V element : coll) {
			try {
				K key = PropertyToolkits.getProperty(element, keyProperty);
				map.put(key, element);
			} catch (Exception e) {
				continue;
			}
		}

		return map;
	}

}