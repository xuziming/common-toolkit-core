package com.simon.credit.toolkit.collection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.simon.credit.toolkit.common.CommonToolkits;
import com.simon.credit.toolkit.hash.MapToolkits;
import com.simon.credit.toolkit.reflect.DataFetcher;
import com.simon.credit.toolkit.reflect.NotNullDataFetcher;
import com.simon.credit.toolkit.reflect.TypeRef;

/**
 * 集合工具类
 * @author XUZIMING 2018-07-29
 */
public class CollectionToolkits {

	/**
	 * 收集某个属性值的集合
	 * @param coll 源集合对象
	 * @param dataFetcher 数据抓取接口
	 * @return
	 */
	public static <E, T> Collection<T> collect(Collection<E> coll, DataFetcher<E, T> dataFetcher) {
		if (CommonToolkits.isEmpty(coll)) {
			return null;
		}

		List<T> list = new ArrayList<T>();
		for (E e : coll) {
			T data = dataFetcher.fetch(e);
			if (dataFetcher instanceof NotNullDataFetcher && data == null) {
				continue;
			}

			list.add(data);
		}

		return list;
	}

	/**
	 * 收集某个属性值的集合
	 * @param coll 源集合对象
	 * @param dataFetcher 数据抓取接口
	 * @param typeRef 类型引用(获取的目标集合的元素类型)
	 * @return
	 */
	public static <E, T, C extends Collection<T>> C collect(
		Collection<E> coll, DataFetcher<E, T> dataFetcher, TypeRef<C> typeRef) {
		// 空集合判断
		if (CommonToolkits.isEmpty(coll)) {
			return null;
		}

		C newCollection = newCollection(typeRef);
		for (E e : coll) {
			T data = dataFetcher.fetch(e);
			if (dataFetcher instanceof NotNullDataFetcher && data == null) {
				continue;
			}

			newCollection.add(data);
		}

		return (C) newCollection;
	}

	/**
	 * 创建新集合对象
	 * @param typeRef 类型引用(泛型支持)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E, C extends Collection<E>> C newCollection(TypeRef<C> typeRef) {
		return newCollection((Class<C>) typeRef.getTargetClass());
	}

	@SuppressWarnings("unchecked")
	public static <E, C extends Collection<E>> C newCollection(Class<C> clazz) {
		if (List.class.isAssignableFrom(clazz)) {
			return (C) new ArrayList<E>();
		}

		if (Set.class.isAssignableFrom(clazz)) {
			return (C) new HashSet<E>();
		}

		if (Queue.class.isAssignableFrom(clazz)) {
			return (C) new ArrayDeque<E>();
		}

		throw new RuntimeException("not support class: " + clazz.getName());
	}

	/**
	 * Collection转为Map
	 * @param coll 集合对象
	 * @param dataFetcher 数据抓取接口
	 * @return
	 */
	public static <K, V> Map<K, V> toMap(Collection<V> coll, DataFetcher<V, K> keyFetcher) {
		return MapToolkits.parseMap(coll, keyFetcher);
	}

	/**
	 * 将集合分组
	 * @param coll 对象集合
	 * @param dataFetcher 数据抓取接口
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, Collection<V>> groupBy(Collection<V> coll, DataFetcher<V, K> dataFetcher) {
		if (CommonToolkits.isEmpty(coll)) return null;

		Map<K, Collection<V>> map = new HashMap<K, Collection<V>>(MapToolkits.DEFAULT_INITIAL_CAPACITY);

		for (V element : coll) {
			K key = dataFetcher.fetch(element);
			if (dataFetcher instanceof NotNullDataFetcher && key == null) {
				continue;
			}

			Collection<V> group = map.get(key);
			if (group == null) {
				group = CollectionToolkits.newCollection((Class<Collection<V>>) coll.getClass());
				map.put(key, group);
			}

			group.add(element);
		}

		return map;
	}

}
