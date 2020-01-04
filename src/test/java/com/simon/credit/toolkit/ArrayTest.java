package com.simon.credit.toolkit;

import java.util.Arrays;

import com.simon.credit.toolkit.lang.ArrayToolkits;

public class ArrayTest {

	public static void main(String[] args) {
		System.out.println(ArrayToolkits.indexOf(new String[] { "123", "456" }, "456"));
		System.out.println(ArrayToolkits.contains(new String[] { "123", "456" }, "123"));
		System.out.println(ArrayToolkits.notContains(new String[] { "123", "456" }, "456"));

		long[] array = new long[] { 123, 456, 222, 101, 911, 996, 700 };
		Arrays.sort(array);
		ArrayToolkits.sort(array);
		for (Object i : array) {
			System.out.println(i);
		}
	}

}