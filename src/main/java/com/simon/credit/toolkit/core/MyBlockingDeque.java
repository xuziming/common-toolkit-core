package com.simon.credit.toolkit.core;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

public interface MyBlockingDeque<E> extends BlockingDeque<E>, Deque<E> {

	void addFirst(E e);

	void addLast(E e);

	boolean offerFirst(E e);

	boolean offerLast(E e);

	void putFirst(E e) throws InterruptedException;

	void putLast(E e) throws InterruptedException;

	boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException;

	boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException;

	E takeFirst() throws InterruptedException;

	E takeLast() throws InterruptedException;

	E pollFirst(long timeout, TimeUnit unit) throws InterruptedException;

	E pollLast(long timeout, TimeUnit unit) throws InterruptedException;

	boolean removeFirstOccurrence(Object o);

	boolean removeLastOccurrence(Object o);

	// *** BlockingQueue methods ***

	boolean add(E e);

	boolean offer(E e);

	void put(E e) throws InterruptedException;

	boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

	E remove();

	E poll();

	E take() throws InterruptedException;

	E poll(long timeout, TimeUnit unit) throws InterruptedException;

	E element();

	E peek();

	boolean remove(Object o);

	public boolean contains(Object o);

	public int size();

	Iterator<E> iterator();

	// *** Stack methods ***

	void push(E e);

}