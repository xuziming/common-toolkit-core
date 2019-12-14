package com.simon.credit.toolkit.core;

import java.util.EmptyStackException;

public class MyStack<E> extends MyVector<E> {
	private static final long serialVersionUID = 6697992257574549119L;

	public MyStack() {}

	public E push(E item) {
		addElement(item);
		return item;
	}

	public synchronized E pop() {
		E obj;
		int len = size();

		obj = peek();
		removeElementAt(len - 1);

		return obj;
	}

	public synchronized E peek() {
		int len = size();

		if (len == 0) {
			throw new EmptyStackException();
		}
		return elementAt(len - 1);
	}

	public boolean empty() {
		return size() == 0;
	}

	public synchronized int search(Object obj) {
		int index = lastIndexOf(obj);

		if (index >= 0) {
			return size() - index;
		}
		return -1;
	}

}