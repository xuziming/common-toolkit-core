package com.simon.credit.toolkit.core;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Queue;

public abstract class MyAbstractQueue<E> extends MyAbstractCollection<E> implements Queue<E> {

	protected MyAbstractQueue() {}

	public boolean add(E e) {
		if (offer(e)) {
			return true;
		} else {
			throw new IllegalStateException("Queue full");
		}
	}

	public E remove() {
		E x = poll();
		if (x != null) {
			return x;
		} else {
			throw new NoSuchElementException();
		}
	}

	public E element() {
		E x = peek();
		if (x != null) {
			return x;
		} else {
			throw new NoSuchElementException();
		}
	}

	public void clear() {
		while (poll() != null) {
			;
		}
	}

	public boolean addAll(Collection<? extends E> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		if (c == this) {
			throw new IllegalArgumentException();
		}
		boolean modified = false;
		for (E e : c) {
			if (add(e)) {
				modified = true;
			}
		}
		return modified;
	}

}
