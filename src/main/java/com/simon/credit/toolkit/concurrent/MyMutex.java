package com.simon.credit.toolkit.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 互斥锁
 * @author XUZIMING 2019-11-24
 */
public class MyMutex implements Lock, Serializable {
	private static final long serialVersionUID = 4694907976021142532L;

	/** The sync object does all the hard work. We just forward to it. */
	private final Sync sync = new Sync();

	/** 使用同步器的模板方法实现自己的同步语义 */
	@Override
	public void lock() {
		sync.acquire(1);
	}

	@Override
	public boolean tryLock() {
		return sync.tryAcquire(1);
	}

	@Override
	public void unlock() {
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		return sync.newCondition();
	}

	public boolean isLocked() {
		return sync.isHeldExclusively();
	}

	public boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}

	/** Our internal helper class (继承AQS的静态内存类 ) */
	private static class Sync extends MyAbstractQueuedSynchronizer {
		private static final long serialVersionUID = -2123311670815087954L;

		/** Reports whether in locked state */
		@Override
		protected boolean isHeldExclusively() {
			return getState() == 1;// 是否持有排它锁
		}

		/** Acquires the lock if state is zero */
		@Override
		public boolean tryAcquire(int acquires) {
			assert acquires == 1; // 目标是加锁
			if (compareAndSetState(0, 1)) {// 0:初始状态(默认为零), 1:加锁状态(排它锁)
				// 设置排它锁拥有者线程
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;// 当前有其它线程占用锁, 加锁不成功, 返回false
		}

		/** Releases the lock by setting state to zero */
		@Override
		protected boolean tryRelease(int releases) {
			assert releases == 1; // 当前是加锁状态才能释放
			if (getState() == 0) {// 如果当前是初始状态(没加锁), 则不能释放锁, 直接抛出非法监听器状态异常
				throw new IllegalMonitorStateException();
			}
			setExclusiveOwnerThread(null);// 锁释放之后, 置空排它锁拥有者
			setState(0);// 同步状态置为初始状态(未加锁状态)
			return true;
		}

		Condition newCondition() {
			return new ConditionObject();
		}

		/** Deserializes properly */
		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			setState(0);// 重置为未上锁状态
		}
	}

}