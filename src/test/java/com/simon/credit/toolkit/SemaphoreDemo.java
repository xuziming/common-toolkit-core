package com.simon.credit.toolkit;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.simon.credit.toolkit.lang.ThreadToolkits;

/**
 * 信号灯/信号量示例
 * <pre>场景模拟：争车位</pre>
 * @author xuziming 2019-11-03
 */
public class SemaphoreDemo {

	public static void main(String[] args) {
		Semaphore semaphore = new Semaphore(3);

		for (int i = 1; i <= 20; i++) {// 模拟20辆汽车
			new Thread(() -> {
				try {
					semaphore.acquire();
					System.out.println(Thread.currentThread().getName() + "\t抢到车位");
					ThreadToolkits.sleep(3, TimeUnit.SECONDS);
					System.out.println(Thread.currentThread().getName() + "\t停车三秒后离开车位");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					semaphore.release();
				}
			}).start();
		}
	}

}
