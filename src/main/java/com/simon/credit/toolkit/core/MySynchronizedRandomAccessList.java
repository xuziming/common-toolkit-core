package com.simon.credit.toolkit.core;

import java.util.List;
import java.util.RandomAccess;

public class MySynchronizedRandomAccessList<E> extends MySynchronizedList<E> implements RandomAccess {

	MySynchronizedRandomAccessList(List<E> list) {
		super(list);
	}

	MySynchronizedRandomAccessList(List<E> list, Object mutex) {
		super(list, mutex);
	}

	public List<E> subList(int fromIndex, int toIndex) {
		synchronized (mutex) {
			return new MySynchronizedRandomAccessList<>(list.subList(fromIndex, toIndex), mutex);
		}
	}

	private static final long serialVersionUID = 1530674583602358482L;

	private Object writeReplace() {
		return new MySynchronizedList<>(list);
	}

}