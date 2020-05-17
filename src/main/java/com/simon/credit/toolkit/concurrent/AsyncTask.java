package com.simon.credit.toolkit.concurrent;

/**
 * 异步任务
 * @author XUZIMING 2020-03-29
 * @param <T>
 */
public abstract class AsyncTask<T> implements IAsyncTask<T> {

	@Override
	public T call() throws Exception {
		return execute();
	}

	/**
	 * 异步任务执行方法
	 * @return
	 */
	protected abstract T execute();

}
