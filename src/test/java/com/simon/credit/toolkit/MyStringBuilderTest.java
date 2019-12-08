package com.simon.credit.toolkit;

import com.simon.credit.toolkit.lang.MyStringBuilder;

public class MyStringBuilderTest {

	public static void main(String[] args) {
		MyStringBuilder builder = new MyStringBuilder();
		builder.append("abc").append("def").append("ghi");
		System.out.println(builder.toString());
		System.out.println(builder.charAt(2));
	}

}