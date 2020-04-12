package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.CountDownLatch;

/**
 * Fast-Fail CountDownLatch
 * @author XUZIMING 2020-03-28
 */
public class FastFailCountDownLatch extends CountDownLatch {

	private volatile Exception exception;

	public FastFailCountDownLatch(int count) {
		super(count);
	}

	/**
	 * 发生异常
	 */
	public void occurException(Exception e) {
		if (this.exception != null) {
			return;
		}

		this.exception = e;// record exception

		while (this.getCount() > 0) {
			this.countDown();
		}
	}

	/**
	 * 是否存在异常
	 */
	public boolean existsException() {
		return this.exception != null;
	}

}
