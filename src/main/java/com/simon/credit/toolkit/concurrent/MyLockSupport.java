package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * LockSupport类使用了一种名为Permit（许可）的概念来做到阻塞和唤醒线程的功能，
 * 可以把许可看成是一种(0,1)信号量（Semaphore），但与 Semaphore 不同的是，许可的累加上限是1。
 * 初始时，permit为0，当调用unpark()方法时，线程的permit加1，
 * 当调用park()方法时，如果permit为0，则调用线程进入阻塞状态。
 */
@SuppressWarnings("restriction")
public class MyLockSupport {

	private static final sun.misc.Unsafe UNSAFE;
	private static final long parkBlockerOffset;
	@SuppressWarnings("unused")
	private static final long SEED;
	@SuppressWarnings("unused")
	private static final long PROBE;
	private static final long SECONDARY;

	static {
		try {
			UNSAFE = UnsafeToolkits.getUnsafe();
			Class<?> threadClass = Thread.class;
			parkBlockerOffset = UNSAFE.objectFieldOffset(threadClass.getDeclaredField("parkBlocker"));

			SEED      = UNSAFE.objectFieldOffset(threadClass.getDeclaredField("threadLocalRandomSeed"));
			PROBE     = UNSAFE.objectFieldOffset(threadClass.getDeclaredField("threadLocalRandomProbe"));
			SECONDARY = UNSAFE.objectFieldOffset(threadClass.getDeclaredField("threadLocalRandomSecondarySeed"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	private MyLockSupport() {} // Cannot be instantiated.

	private static void setBlocker(Thread thread, Object blocker) {
		// Even though volatile, hotspot doesn't need a write barrier here.
		UNSAFE.putObject(thread, parkBlockerOffset, blocker);
	}

	/** 唤醒指定线程 */
	public static void unpark(Thread thread) {
		if (thread != null) {
			UNSAFE.unpark(thread);
		}
	}

	/** 阻塞当前调用线程 */
	public static void park(Object blocker) {
		Thread currentThread = Thread.currentThread();
		setBlocker(currentThread, blocker);
		UNSAFE.park(false, 0L);
		setBlocker(currentThread, null);
	}

	public static void parkNanos(Object blocker, long nanos) {
		if (nanos > 0) {
			Thread currentThread = Thread.currentThread();
			setBlocker(currentThread, blocker);
			UNSAFE.park(false, nanos);
			setBlocker(currentThread, null);
		}
	}

	public static void parkUntil(Object blocker, long deadline) {
		Thread currentThread = Thread.currentThread();
		setBlocker(currentThread, blocker);
		UNSAFE.park(true, deadline);
		setBlocker(currentThread, null);
	}

	public static Object getBlocker(Thread thread) {
		if (thread == null) {
			throw new NullPointerException();
		}
		return UNSAFE.getObjectVolatile(thread, parkBlockerOffset);
	}

	public static void park() {
		UNSAFE.park(false, 0L);
	}

	public static void parkNanos(long nanos) {
		if (nanos > 0) {
			UNSAFE.park(false, nanos);
		}
	}

	public static void parkUntil(long deadline) {
		UNSAFE.park(true, deadline);
	}

	static final int nextSecondarySeed() {
		int r;
		Thread currentThread = Thread.currentThread();
		if ((r = UNSAFE.getInt(currentThread, SECONDARY)) != 0) {
			r ^= r << 13; // xorshift
			r ^= r >>> 17;
			r ^= r << 5;
		} else if ((r = ThreadLocalRandom.current().nextInt()) == 0) {
			r = 1; // avoid zero
		}
		UNSAFE.putInt(currentThread, SECONDARY, r);
		return r;
	}

}