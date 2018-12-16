package com.simon.credit.toolkit.reflect;

/**
 * 数据抓取接口
 * @author XUZIMING 2018-07-29
 * @param <E> 元素对象类型
 * @param <T> 结果值类型
 */
public interface DataFetcher<E, T> {

	T fetch(E e);

}