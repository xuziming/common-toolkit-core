package com.simon.credit.toolkit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABADemo2 {
	private static AtomicReference<Integer> ar = new AtomicReference<>(100);
	private static AtomicStampedReference<Integer> asr = new AtomicStampedReference<>(100, 1);

	public static void main(String[] args) {
		System.out.println("===以下是ABA问题的产生===");
		new Thread(() -> {
			ar.compareAndSet(100, 101);
			ar.compareAndSet(101, 100);
		}, "t1").start();

		new Thread(() -> {
			// 先暂停1秒 保证完成ABA
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(ar.compareAndSet(100, 2019) + "\t" + ar.get());
		}, "t2").start();
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("===以下是ABA问题的解决===");

		new Thread(() -> {
			int stamp = asr.getStamp();
			System.out.println(
					Thread.currentThread().getName() + "\t 第1次版本号" + stamp + "\t值是" + asr.getReference());
			// 暂停1秒钟t3线程
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			asr.compareAndSet(100, 101, asr.getStamp(), asr.getStamp() + 1);
			System.out.println(Thread.currentThread().getName() + "\t 第2次版本号" + asr.getStamp() + "\t值是"
					+ asr.getReference());
			asr.compareAndSet(101, 100, asr.getStamp(), asr.getStamp() + 1);
			System.out.println(Thread.currentThread().getName() + "\t 第3次版本号" + asr.getStamp() + "\t值是"
					+ asr.getReference());
		}, "t3").start();

		new Thread(() -> {
			int stamp = asr.getStamp();
			System.out.println(
					Thread.currentThread().getName() + "\t 第1次版本号" + stamp + "\t值是" + asr.getReference());
			// 保证线程3完成1次ABA
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			boolean result = asr.compareAndSet(100, 2019, stamp, stamp + 1);
			System.out.println(
					Thread.currentThread().getName() + "\t 修改成功否" + result + "\t最新版本号" + asr.getStamp());
			System.out.println("最新的值\t" + asr.getReference());
		}, "t4").start();
	}

}