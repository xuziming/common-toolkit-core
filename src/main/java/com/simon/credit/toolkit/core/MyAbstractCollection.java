package com.simon.credit.toolkit.core;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public abstract class MyAbstractCollection<E> implements Collection<E> {

	protected MyAbstractCollection() {}

	public abstract Iterator<E> iterator();

	public abstract int size();

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean contains(Object obj) {
		Iterator<E> it = iterator();
		if (obj == null) {
			while (it.hasNext()) {
				if (it.next() == null) {
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				if (obj.equals(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	public Object[] toArray() {
		// Estimate size of array; be prepared to see more or fewer elements
		Object[] array = new Object[size()];
		Iterator<E> it = iterator();
		for (int i = 0; i < array.length; i++) {
			if (!it.hasNext()) {// fewer elements than expected
				return Arrays.copyOf(array, i);
			}
			array[i] = it.next();
		}
		return it.hasNext() ? finishToArray(array, it) : array;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		// Estimate size of array; be prepared to see more or fewer elements
		int size = size();
		T[] r = a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		Iterator<E> it = iterator();

		for (int i = 0; i < r.length; i++) {
			if (!it.hasNext()) { // fewer elements than expected
				if (a != r) {
					return Arrays.copyOf(r, i);
				}
				r[i] = null; // null-terminate
				return r;
			}
			r[i] = (T) it.next();
		}
		return it.hasNext() ? finishToArray(r, it) : r;
	}

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	@SuppressWarnings("unchecked")
	private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
		int i = r.length;
		while (it.hasNext()) {
			int cap = r.length;
			if (i == cap) {
				int newCap = cap + (cap >> 1) + 1;
				// overflow-conscious code
				if (newCap - MAX_ARRAY_SIZE > 0) {
					newCap = hugeCapacity(cap + 1);
				}
				r = Arrays.copyOf(r, newCap);
			}
			r[i++] = (T) it.next();
		}
		// trim if overallocated
		return (i == r.length) ? r : Arrays.copyOf(r, i);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) {// overflow
			throw new OutOfMemoryError("Required array size too large");
		}
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		Iterator<E> it = iterator();
		if (o == null) {
			while (it.hasNext()) {
				if (it.next() == null) {
					it.remove();
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				if (o.equals(it.next())) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		for (Object e : c) {
			if (!contains(e)) {
				return false;
			}
		}
		return true;
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		for (E e : c) {
			if (add(e)) {
				modified = true;
			}
		}
		return modified;
	}

	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<?> it = iterator();
		while (it.hasNext()) {
			if (c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			if (!c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	public void clear() {
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	public String toString() {
		Iterator<E> it = iterator();
		if (!it.hasNext()) {
			return "[]";
		}

		StringBuilder builder = new StringBuilder();
		builder.append('[');
		for (;;) {
			E e = it.next();
			builder.append(e == this ? "(this Collection)" : e);
			if (!it.hasNext()) {
				return builder.append(']').toString();
			}
			builder.append(',').append(' ');
		}
	}

}