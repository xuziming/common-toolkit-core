package com.simon.credit.toolkit.core;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public interface MyBlockingQueue<E> extends BlockingQueue<E> {

	/**
	 * 当阻塞队列满时，再往队列里add插入元素会抛出IllegalStateException: Queue full
	 */
	boolean add(E e);

	/**
	 * 插入方法，成功返回true，失败则返回false
	 */
	boolean offer(E e);

	/**
	 * 当阻塞队列满时，生产者线程继续往队列里put元素，队列会一直阻塞生产线程直到put数据或响应中断退出
	 */
	void put(E e) throws InterruptedException;

	/**
	 * 当阻塞队列满时，队列会阻塞生产者线程一定时间，超过限时后生产者线程会退出
	 */
	boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * 当阻塞队列空时，消费者线程试图从队列里take元素，队列会一直阻塞消费者线程直到队列可用
	 */
	E take() throws InterruptedException;

	/**
	 * 当阻塞队列空时，队列会阻塞消费者线程一定时间，超过限时后消费者线程会退出
	 */
	E poll(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * 返回队列剩余可用容量
	 */
	int remainingCapacity();

	/**
	 * 当阻塞队列空时，再向队列里remove移除元素会抛出NoSuchElementException
	 */
	boolean remove(Object obj);

	/**
	 * 判断队列是否包含指定元素
	 */
	public boolean contains(Object obj);

	/**
	 * 将队列中值，全部移除，并发设置到给定的集合中
	 *
	 * <pre>
	 * 一次性从BlockingQueue获取所有可用的数据对象(还可以指定获取数据的个数)，
	 * 通过该方法，可以提升获取数据效率；不需要多次分批加锁或释放锁。
	 * </pre>
	 */
	int drainTo(Collection<? super E> coll);

	/**
	 * 将队列中值，全部移除，并发设置到给定的集合中
	 *
	 * <pre>
	 * 一次性从BlockingQueue获取所有可用的数据对象(还可以指定获取数据的个数)，
	 * 通过该方法，可以提升获取数据效率；不需要多次分批加锁或释放锁。
	 * </pre>
	 */
	@Override
	int drainTo(Collection<? super E> coll, int maxElements);

	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/**
	 * 检查(获取队列头元素)
	 */
	@Override
	E element();

	/**
	 * 检查(获取队列头元素)
	 */
	@Override
	E peek();

	/**
	 * 移除方法，成功时返回出队的元素，队列里没有则返回null
	 */
	@Override
	E poll();

	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
}