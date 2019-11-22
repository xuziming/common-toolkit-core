package com.simon.credit.toolkit.batch;

import java.util.concurrent.atomic.AtomicReference;

class User {
	private String name;
	private int age;

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
}

public class AtomicReferenceDemo {
	public static void main(String[] args) {
		User zs = new User("zs", 22);
		User ls = new User("ls", 22);
		AtomicReference<User> userAtomicReference = new AtomicReference<>();
		userAtomicReference.set(zs);
		System.out.println(userAtomicReference.compareAndSet(zs, ls) + "\t" + userAtomicReference.get().toString());
		System.out.println(userAtomicReference.compareAndSet(zs, ls) + "\t" + userAtomicReference.get().toString());
	}
}
