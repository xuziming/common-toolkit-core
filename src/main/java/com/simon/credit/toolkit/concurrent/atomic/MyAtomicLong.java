package com.simon.credit.toolkit.concurrent.atomic;

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import com.simon.credit.toolkit.concurrent.UnsafeToolkits;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class MyAtomicLong extends Number implements java.io.Serializable {
	private static final long serialVersionUID = 5804945265167030684L;

	private static final Unsafe unsafe;
	private static final long valueOffset;

	static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();

	private static native boolean VMSupportsCS8();

	static {
		try {
			unsafe = UnsafeToolkits.getUnsafe();
			valueOffset = unsafe.objectFieldOffset(MyAtomicLong.class.getDeclaredField("value"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	private volatile long value;

	public MyAtomicLong(long initialValue) {
		value = initialValue;
	}

	public MyAtomicLong() {}

	public final long get() {
		return value;
	}

	public final void set(long newValue) {
		value = newValue;
	}

	public final void lazySet(long newValue) {
		unsafe.putOrderedLong(this, valueOffset, newValue);
	}

	public final long getAndSet(long newValue) {
		return unsafe.getAndSetLong(this, valueOffset, newValue);
	}

	public final boolean compareAndSet(long expect, long update) {
		return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
	}

	public final boolean weakCompareAndSet(long expect, long update) {
		return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
	}

	public final long getAndIncrement() {
		return unsafe.getAndAddLong(this, valueOffset, 1L);
	}

	public final long getAndDecrement() {
		return unsafe.getAndAddLong(this, valueOffset, -1L);
	}

	public final long getAndAdd(long delta) {
		return unsafe.getAndAddLong(this, valueOffset, delta);
	}

	public final long incrementAndGet() {
		return unsafe.getAndAddLong(this, valueOffset, 1L) + 1L;
	}

	public final long decrementAndGet() {
		return unsafe.getAndAddLong(this, valueOffset, -1L) - 1L;
	}

	public final long addAndGet(long delta) {
		return unsafe.getAndAddLong(this, valueOffset, delta) + delta;
	}

	public final long getAndUpdate(LongUnaryOperator updateFunction) {
		long prev, next;
		do {
			prev = get();
			next = updateFunction.applyAsLong(prev);
		} while (!compareAndSet(prev, next));
		return prev;
	}

	public final long updateAndGet(LongUnaryOperator updateFunction) {
		long prev, next;
		do {
			prev = get();
			next = updateFunction.applyAsLong(prev);
		} while (!compareAndSet(prev, next));
		return next;
	}

	public final long getAndAccumulate(long x, LongBinaryOperator accumulatorFunction) {
		long prev, next;
		do {
			prev = get();
			next = accumulatorFunction.applyAsLong(prev, x);
		} while (!compareAndSet(prev, next));
		return prev;
	}

	public final long accumulateAndGet(long x, LongBinaryOperator accumulatorFunction) {
		long prev, next;
		do {
			prev = get();
			next = accumulatorFunction.applyAsLong(prev, x);
		} while (!compareAndSet(prev, next));
		return next;
	}

	public String toString() {
		return Long.toString(get());
	}

	public int intValue() {
		return (int) get();
	}

	public long longValue() {
		return get();
	}

	public float floatValue() {
		return (float) get();
	}

	public double doubleValue() {
		return (double) get();
	}

}