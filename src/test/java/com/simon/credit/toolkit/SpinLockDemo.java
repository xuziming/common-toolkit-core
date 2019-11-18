package com.simon.credit.toolkit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 自旋锁示例
 * @author xuziming 2019-11-03
 */
public class SpinLockDemo {

	AtomicReference<Thread> atomicReference = new AtomicReference<Thread>();

	public void lock() {
		Thread thread = Thread.currentThread();
		System.out.println(Thread.currentThread().getName() + "\t come in");

		// 自旋获取锁
		while (!atomicReference.compareAndSet(null, thread)) {

		}
	}

	public void unlock() {
		Thread thread = Thread.currentThread();
		atomicReference.compareAndSet(thread, null);
		System.out.println(Thread.currentThread().getName() + "\t invoke unlock");
	}

	public static void main(String[] args) {
		SpinLockDemo spinLockDemo = new SpinLockDemo();

		new Thread(() -> {
			spinLockDemo.lock();
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
			spinLockDemo.unlock();
		}, "T1").start();

		new Thread(() -> {
			spinLockDemo.lock();
			spinLockDemo.unlock();
		}, "T2").start();
	}

}
