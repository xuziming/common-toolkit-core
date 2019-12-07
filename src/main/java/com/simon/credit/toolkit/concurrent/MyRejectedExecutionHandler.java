package com.simon.credit.toolkit.concurrent;

public interface MyRejectedExecutionHandler {

	void rejectedExecution(Runnable runnable, MyThreadPoolExecutor executor);

}