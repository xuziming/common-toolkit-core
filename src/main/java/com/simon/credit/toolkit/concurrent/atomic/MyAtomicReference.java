package com.simon.credit.toolkit.concurrent.atomic;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import com.simon.credit.toolkit.concurrent.UnsafeToolkits;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class MyAtomicReference<V> implements java.io.Serializable {
	private static final long serialVersionUID = -1848883965231344442L;

	private static final Unsafe unsafe;
	private static final long valueOffset;

	static {
		try {
			unsafe = UnsafeToolkits.getUnsafe();
			valueOffset = unsafe.objectFieldOffset(MyAtomicReference.class.getDeclaredField("value"));
		} catch (Exception ex) {
			throw new Error(ex);
		}
	}

	private volatile V value;

	public MyAtomicReference(V initialValue) {
		value = initialValue;
	}

	public MyAtomicReference() {
	}

	public final V get() {
		return value;
	}

	public final void set(V newValue) {
		value = newValue;
	}

	public final void lazySet(V newValue) {
		unsafe.putOrderedObject(this, valueOffset, newValue);
	}

	public final boolean compareAndSet(V expect, V update) {
		return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
	}

	public final boolean weakCompareAndSet(V expect, V update) {
		return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
	}

	@SuppressWarnings("unchecked")
	public final V getAndSet(V newValue) {
		return (V) unsafe.getAndSetObject(this, valueOffset, newValue);
	}

	public final V getAndUpdate(UnaryOperator<V> updateFunction) {
		V prev, next;
		do {
			prev = get();
			next = updateFunction.apply(prev);
		} while (!compareAndSet(prev, next));
		return prev;
	}

	public final V updateAndGet(UnaryOperator<V> updateFunction) {
		V prev, next;
		do {
			prev = get();
			next = updateFunction.apply(prev);
		} while (!compareAndSet(prev, next));
		return next;
	}

	public final V getAndAccumulate(V x, BinaryOperator<V> accumulatorFunction) {
		V prev, next;
		do {
			prev = get();
			next = accumulatorFunction.apply(prev, x);
		} while (!compareAndSet(prev, next));
		return prev;
	}

	public final V accumulateAndGet(V x, BinaryOperator<V> accumulatorFunction) {
		V prev, next;
		do {
			prev = get();
			next = accumulatorFunction.apply(prev, x);
		} while (!compareAndSet(prev, next));
		return next;
	}

	public String toString() {
		return String.valueOf(get());
	}

}