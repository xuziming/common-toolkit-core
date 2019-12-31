package com.simon.credit.toolkit.concurrent.atomic;

import java.io.Serializable;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import com.simon.credit.toolkit.concurrent.UnsafeToolkits;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class MyAtomicInteger extends Number implements Serializable {
	private static final long serialVersionUID = -8534993684151147344L;

	private static final Unsafe unsafe;
	private static final long valueOffset;

	static {
		try {
			unsafe = UnsafeToolkits.getUnsafe();
			valueOffset = unsafe.objectFieldOffset(MyAtomicInteger.class.getDeclaredField("value"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	private volatile int value;

	public MyAtomicInteger(int initialValue) {
		value = initialValue;
	}

	public MyAtomicInteger() {
	}

	public final int get() {
		return value;
	}

	public final void set(int newValue) {
		value = newValue;
	}

	public final void lazySet(int newValue) {
		unsafe.putOrderedInt(this, valueOffset, newValue);
	}

	public final int getAndSet(int newValue) {
		return unsafe.getAndSetInt(this, valueOffset, newValue);
	}

	public final boolean compareAndSet(int expect, int update) {
		return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
	}

	public final boolean weakCompareAndSet(int expect, int update) {
		return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
	}

	public final int getAndIncrement() {
		return unsafe.getAndAddInt(this, valueOffset, 1);
	}

	public final int getAndDecrement() {
		return unsafe.getAndAddInt(this, valueOffset, -1);
	}

	public final int getAndAdd(int delta) {
		return unsafe.getAndAddInt(this, valueOffset, delta);
	}

	public final int incrementAndGet() {
		return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
	}

	public final int decrementAndGet() {
		return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
	}

	public final int addAndGet(int delta) {
		return unsafe.getAndAddInt(this, valueOffset, delta) + delta;
	}

	public final int getAndUpdate(IntUnaryOperator updateFunction) {
		int prev, next;
		do {
			prev = get();
			next = updateFunction.applyAsInt(prev);
		} while (!compareAndSet(prev, next));
		return prev;
	}

	public final int updateAndGet(IntUnaryOperator updateFunction) {
		int prev, next;
		do {
			prev = get();
			next = updateFunction.applyAsInt(prev);
		} while (!compareAndSet(prev, next));
		return next;
	}

	public final int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
		int prev, next;
		do {
			prev = get();
			next = accumulatorFunction.applyAsInt(prev, x);
		} while (!compareAndSet(prev, next));
		return prev;
	}

	public final int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
		int prev, next;
		do {
			prev = get();
			next = accumulatorFunction.applyAsInt(prev, x);
		} while (!compareAndSet(prev, next));
		return next;
	}

	public String toString() {
		return Integer.toString(get());
	}

	public int intValue() {
		return get();
	}

	public long longValue() {
		return (long) get();
	}

	public float floatValue() {
		return (float) get();
	}

	public double doubleValue() {
		return (double) get();
	}

}