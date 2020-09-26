package com.simon.credit.toolkit.concurrent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 异步任务处理器
 * @author XUZIMING 2020-03-29
 * @param <T>
 */
public class AsyncTaskHandler<T> {

	// private static final ForkJoinPool EXECUTOR = new ForkJoinPool(20);

	/**
	 * 线程池里有很多线程需要同时执行，旧的可用线程将被新的任务触发重新执行，如果线程超过60秒内没执行，那么将被终止并从池中删除
	 */
	private static final ExecutorService EXECUTOR = OptimizedThreadPool.jdkCachedThreadPool();
	// private static final ExecutorService EXECUTOR = OptimizedThreadPool.newCachedThreadPool(16, 1024);
	// private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	public Future<T> handle(IAsyncTask<T> task) {
		return EXECUTOR.submit(task);
	}

	/**
	 * 同步处理任务列表
	 * @param tasks
	 * @return
	 */
	public List<T> syncHandle(List<IAsyncTask<T>> tasks) {
		Map<String, IAsyncTask<T>> taskMap = new LinkedHashMap<>(16);
		for (int i = 0; i < tasks.size(); i++) {
			taskMap.put(Integer.toString(i), tasks.get(i));
		}

		Map<String, T> resultMap = syncHandle(taskMap);
		return new ArrayList<>(resultMap.values());
	}

	/**
	 * 同步处理任务列表
	 * @param tasks
	 * @return
	 */
	public Map<String, T> syncHandle(Map<String, IAsyncTask<T>> tasks) {
		long start = System.currentTimeMillis();
		FastFailCountDownLatch latch = new FastFailCountDownLatch(tasks.size());
		Map<String, Future<T>> futures = new LinkedHashMap<>(16);

		for (Map.Entry<String, IAsyncTask<T>> entry : tasks.entrySet()) {
			try {
				Future<T> future = handle(entry.getValue());
				futures.put(entry.getKey(), future);
			} catch (Exception e) {
				e.printStackTrace();
				latch.occurException(e);
			} finally {
				latch.countDown();
			}
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Map<String, T> results = new LinkedHashMap<>(10);
		for (Map.Entry<String, Future<T>> entry : futures.entrySet()) {
			try {
				T result = entry.getValue().get();
				results.put(entry.getKey(), result);
			} catch (Exception e) {
				e.printStackTrace();
				results.put(entry.getKey(), null);
			}
		}

		long end = System.currentTimeMillis();
		System.out.println("sync handle tasks waste time: " + (end - start));
		return results;
	}

	/**
	 * 销毁
	 */
	public void destroy() {
		EXECUTOR.shutdown();
	}

}