package com.simon.credit.toolkit.reflect;

/**
 * 数据抓取接口
 * @author XUZIMING 2018-07-29
 * @param <E> 集合元素类型(Element)
 * @param <R> 返回结果类型(Result)
 */
public interface DataFetcher<E, R> {
	R fetch(E e);
}