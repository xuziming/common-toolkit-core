package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.Callable;

/**
 * 异步任务
 * @author XUZIMING 2020-03-29
 * @param <T>
 */
public abstract class AsyncTask<T> implements Callable<T> {

	@Override
	public T call() throws Exception {
		return execute();
	}

	protected abstract T execute();

}
