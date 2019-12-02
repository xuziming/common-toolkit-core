package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.TimeUnit;

public class MyCountDownLatch {

	private final Sync sync;

	public MyCountDownLatch(int count) {
		if (count < 0) {
			throw new IllegalArgumentException("count < 0");
		}
		this.sync = new Sync(count);
	}

	public void await() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
	}

	/**
	 * 将计数值减1，如果计数值变为0(最小为0)，则释放所有等待线程
	 */
	public void countDown() {
		sync.releaseShared(1);
	}

	public long getCount() {
		return sync.getCount();
	}

	public String toString() {
		return super.toString() + "[Count = " + sync.getCount() + "]";
	}

	/**
	 * 同步器器(继承AQS)
	 */
	private static final class Sync extends MyAbstractQueuedSynchronizer {
		private static final long serialVersionUID = 4982264981922014374L;

		Sync(int count) {
			setState(count);
		}

		int getCount() {
			return getState();
		}

		protected int tryAcquireShared(int acquires) {
			// 当state==0时，表示无锁状态，且一旦state变为0，就永远处于无锁状态了
			return (getState() == 0) ? 1 : -1;
		}

		protected boolean tryReleaseShared(int releases) {
			// decrement count; signal when transition(转变) to zero
			for (;;) {
				int count = getState();
				if (count == 0) {
					return false;
				}
				int nextCount = count - 1;// count递减1
				if (compareAndSetState(count, nextCount)) {
					return nextCount == 0;
				}
			}
		}
	}

}
