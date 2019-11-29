package com.simon.credit.toolkit.concurrent;

import com.simon.credit.toolkit.concurrent.MySemaphore;
import com.simon.credit.toolkit.lang.ThreadToolkits;
import com.simon.credit.toolkit.lang.Time;

/**
 * 信号灯/信号量示例
 * <pre>场景模拟：争车位</pre>
 * @author xuziming 2019-11-03
 */
public class MySemaphoreTest {

	public static void main(String[] args) {
		MySemaphore mySemaphore = new MySemaphore(3);

		for (int i = 1; i <= 20; i++) {// 模拟20辆汽车
			new Thread(() -> {
				try {
					mySemaphore.acquire();
					System.out.println(Thread.currentThread().getName() + "\t抢到车位");
					ThreadToolkits.sleep(Time.TWO_SECONDS);
					System.out.println(Thread.currentThread().getName() + "\t停车三秒后离开车位");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mySemaphore.release();
				}
			}).start();
		}
	}

}
