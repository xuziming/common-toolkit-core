package com.simon.credit.toolkit.proxy;

/**
 * 目标类，对应Target
 * @author simon 2020-03-10
 */
public class RealSubject implements Subject {

	public void sayHello(String name) {
		System.out.println("hello " + name);
	}

}