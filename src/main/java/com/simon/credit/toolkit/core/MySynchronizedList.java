package com.simon.credit.toolkit.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.function.UnaryOperator;

public class MySynchronizedList<E> extends MySynchronizedCollection<E> implements List<E> {
	private static final long serialVersionUID = -7754090372962971524L;

	final List<E> list;

	MySynchronizedList(List<E> list) {
		super(list);
		this.list = list;
	}

	MySynchronizedList(List<E> list, Object mutex) {
		super(list, mutex);
		this.list = list;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		synchronized (mutex) {
			return list.equals(o);
		}
	}

	public int hashCode() {
		synchronized (mutex) {
			return list.hashCode();
		}
	}

	public E get(int index) {
		synchronized (mutex) {
			return list.get(index);
		}
	}

	public E set(int index, E element) {
		synchronized (mutex) {
			return list.set(index, element);
		}
	}

	public void add(int index, E element) {
		synchronized (mutex) {
			list.add(index, element);
		}
	}

	public E remove(int index) {
		synchronized (mutex) {
			return list.remove(index);
		}
	}

	public int indexOf(Object o) {
		synchronized (mutex) {
			return list.indexOf(o);
		}
	}

	public int lastIndexOf(Object o) {
		synchronized (mutex) {
			return list.lastIndexOf(o);
		}
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		synchronized (mutex) {
			return list.addAll(index, c);
		}
	}

	public ListIterator<E> listIterator() {
		return list.listIterator(); // Must be manually synched by user
	}

	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index); // Must be manually synched by user
	}

	public List<E> subList(int fromIndex, int toIndex) {
		synchronized (mutex) {
			return new MySynchronizedList<>(list.subList(fromIndex, toIndex), mutex);
		}
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		synchronized (mutex) {
			list.replaceAll(operator);
		}
	}

	@Override
	public void sort(Comparator<? super E> c) {
		synchronized (mutex) {
			list.sort(c);
		}
	}

	private Object readResolve() {
		return (list instanceof RandomAccess ? new MySynchronizedRandomAccessList<>(list) : this);
	}

}