package com.simon.credit.toolkit;

import java.util.concurrent.locks.Lock;

import com.simon.credit.toolkit.concurrent.MyMutex;

/**
 * 排它(互斥)锁测试
 * @author XUZIMING 2019-11-28
 */
public class MyMutexTest {

	public static void main(String[] args) {
		//  创建排它(互斥)锁
		Lock mutex = new MyMutex();
		for (int i = 1; i <= 5; i++) {
			new Thread(() -> {
				mutex.lock();
				try {
					System.out.println(Thread.currentThread().getName() + " started.");
					Thread.sleep(2000);
					System.out.println(Thread.currentThread().getName() + " finished.");
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					mutex.unlock();
				}
			}, "t" + i).start();
		}
	}

}
