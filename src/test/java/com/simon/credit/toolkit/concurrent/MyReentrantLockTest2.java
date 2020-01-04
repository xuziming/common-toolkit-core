package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.simon.credit.toolkit.concurrent.MyReentrantLock;

/**
 * 可重入锁测试
 * <pre>
 * 编写一个程序, 开启3个线程, 这三个线程的ID分别为A、B、C，每个线程将自己的ID在屏幕上打印20遍, 
 * 要求输出的结果必须按顺序显示, 如: ABBCCCABBCCCABBCCC…… 依次递归
 * </pre>
 */
public class MyReentrantLockTest2 {

	public static void main(String[] args) {
		AlternateLoop loop = new AlternateLoop();

		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i <= 20; i++) {
					loop.loopA(i);
				}
			}
		}, "A").start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i <= 20; i++) {
					loop.loopB(i);
				}
			}
		}, "B").start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i <= 20; i++) {
					loop.loopC(i);
					System.out.println("-----------------------------------");
				}
			}
		}, "C").start();
	}

}

/**
 * 交替循环
 */
class AlternateLoop {

	private int number = 1; // 当前正在执行线程的标记

	private Lock lock = new MyReentrantLock();// 创建可重入锁

	private Condition condition1 = lock.newCondition();
	private Condition condition2 = lock.newCondition();
	private Condition condition3 = lock.newCondition();

	/**
	 * @param loopTimes : 循环第几轮
	 */
	public void loopA(int loopTimes) {
		lock.lock();

		try {
			// 1. 判断
			if (number != 1) {
				condition1.await();
			}

			// 2. 打印
			for (int i = 1; i <= 1; i++) {
				System.out.println(Thread.currentThread().getName() + "\t" + i + "\t" + loopTimes);
			}

			// 3. 唤醒
			number = 2;
			condition2.signal();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void loopB(int loopTimes) {
		lock.lock();

		try {
			// 1. 判断
			if (number != 2) {
				condition2.await();
			}

			// 2. 打印
			for (int i = 1; i <= 2; i++) {
				System.out.println(Thread.currentThread().getName() + "\t" + i + "\t" + loopTimes);
			}

			// 3. 唤醒
			number = 3;
			condition3.signal();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void loopC(int loopTimes) {
		lock.lock();

		try {
			// 1. 判断
			if (number != 3) {
				condition3.await();
			}

			// 2. 打印
			for (int i = 1; i <= 3; i++) {
				System.out.println(Thread.currentThread().getName() + "\t" + i + "\t" + loopTimes);
			}

			// 3. 唤醒
			number = 1;
			condition1.signal();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

}