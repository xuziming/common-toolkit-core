package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 异步任务处理器
 * @author XUZIMING 2020-03-29
 * @param <T>
 */
public class AsyncTaskHandler<T> {

	// private static final ForkJoinPool EXECUTOR = new ForkJoinPool(20);

	// 线程池里有很多线程需要同时执行，旧的可用线程将被新的任务触发重新执行，如果线程超过60秒内没执行，那么将被终止并从池中删除
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	public Future<T> handle(AsyncTask<T> task) {
		return EXECUTOR.submit(task);
	}

}
