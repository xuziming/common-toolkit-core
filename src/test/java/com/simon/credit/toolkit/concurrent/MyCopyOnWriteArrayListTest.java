package com.simon.credit.toolkit.concurrent;

public class MyCopyOnWriteArrayListTest {

	public static void main(String[] args) {
		MyCopyOnWriteArrayList<String> list = new MyCopyOnWriteArrayList<String>();
		list.add("aaa");
		list.add("bbb");
		list.add("ccc");
		System.out.println(list.size());
	}

}