package com.simon.credit.toolkit;

import com.simon.credit.toolkit.lang.MyStringBuffer;

public class MyStringBufferTest {

	public static void main(String[] args) {
		MyStringBuffer buffer = new MyStringBuffer();
		buffer.append("abc").append("def").append("ghi");
		System.out.println(buffer.toString());
		System.out.println(buffer.charAt(2));
	}

}