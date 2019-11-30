package com.simon.credit.toolkit;

import java.util.concurrent.atomic.AtomicInteger;

public class User {

	private String name;
	private int age;

	public User() {}

	public User(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	
	public static void main(String[] args) {
		AtomicInteger ai1 = new AtomicInteger(100);
		AtomicInteger ai2 = new AtomicInteger(888);
		
		ai1.compareAndSet(100, 200);
		ai2.compareAndSet(100, 200);

		ai2.getAndIncrement();
	}

}