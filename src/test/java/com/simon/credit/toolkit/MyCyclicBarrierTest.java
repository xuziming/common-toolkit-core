package com.simon.credit.toolkit;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicInteger;

import com.simon.credit.toolkit.concurrent.MyCyclicBarrier;

/**
 * 模拟：集齐7颗龙珠召唤神龙
 * @author XUZIMING 2019-11-28
 */
public class MyCyclicBarrierTest {

	public static void main(String[] args) {
		MyCyclicBarrier cyclicBarrier = new MyCyclicBarrier(7, () -> {
			System.out.println("******召唤神龙******");
		});
		AtomicInteger count = new AtomicInteger(0);

		for (int i = 1; i <= 7; i++) {
			new Thread(() -> {
				System.out.println(Thread.currentThread().getName() + " 收集到第" + count.incrementAndGet() + "颗龙珠");
				try {
					cyclicBarrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			}, String.valueOf(i)).start();
		}
	}

}
