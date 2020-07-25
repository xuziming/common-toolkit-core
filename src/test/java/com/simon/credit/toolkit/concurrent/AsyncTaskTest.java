package com.simon.credit.toolkit.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 异步任务测试
 * @author XUZIMING 2020-03-29
 */
public class AsyncTaskTest {

	volatile boolean hasException = false;

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		AsyncTaskHandler<Object> asyncTaskHandler = new AsyncTaskHandler<Object>();
		List<Future<Object>> futureList = new ArrayList<Future<Object>>(10);

		int asyncTaskCount = 1000;// 异步任务数
		FastFailCountDownLatch latch = new FastFailCountDownLatch(asyncTaskCount);

		for (int i = 0; i < asyncTaskCount; i++) {
			futureList.add(asyncTaskHandler.handle(new AsyncTask<Object>() {
				@Override
				public Object execute() {
					try {
						return mockSlowProcessing();
					} catch (Exception e) {
						latch.occurException(e);
						return null;
					} finally {
						latch.countDown();
					}
				}
			}));
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();// log exception
		}

		if (latch.existsException()) {// 存在异常
			System.out.println("has exception");
			//throw new RuntimeException("occur exception.");// tx rollback
		}

		System.out.println("over");
		long end = System.currentTimeMillis();
		System.out.println(String.format("waste time: %s ms", (end - start)));
		asyncTaskHandler.destroy();
	}

	@SuppressWarnings("unused")
	private void testAsync() {
		long startTime = System.currentTimeMillis();
		List<Object> locations = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		locations.parallelStream().map(location -> { return mockSlowProcessing(); }).collect(Collectors.toList());
		long endTime = System.currentTimeMillis();
		System.out.println("waste time: " + (endTime - startTime));
	}

	/**
	 * 模拟慢过程处理
	 */
	private static Object mockSlowProcessing() {
		try {
			Thread.sleep(3000);
			System.out.println("process over!");
		} catch (InterruptedException e) {
			e.printStackTrace();// log exception
		}
		return new Object();
	}

}
