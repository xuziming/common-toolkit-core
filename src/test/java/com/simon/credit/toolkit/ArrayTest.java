package com.simon.credit.toolkit;

import com.simon.credit.toolkit.lang.ArrayToolkits;

public class ArrayTest {

	public static void main(String[] args) {
		System.out.println(ArrayToolkits.indexOf(new String[] { "123", "456" }, "456"));
		System.out.println(ArrayToolkits.contains(new String[] { "123", "456" }, "123"));
		System.out.println(ArrayToolkits.notContains(new String[] { "123", "456" }, "456"));
	}

}
