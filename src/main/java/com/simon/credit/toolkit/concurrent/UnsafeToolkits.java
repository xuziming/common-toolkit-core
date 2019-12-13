package com.simon.credit.toolkit.concurrent;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class UnsafeToolkits {

	public static final Unsafe getUnsafe() throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");// Internal reference
		unsafeField.setAccessible(true);
		return (Unsafe) unsafeField.get(null);
	}

}