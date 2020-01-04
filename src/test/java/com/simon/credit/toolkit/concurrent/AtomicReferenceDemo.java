package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.atomic.AtomicReference;

class SampleUser {
	private String name;
	private int age;

	public SampleUser(String name, int age) {
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
		SampleUser zs = new SampleUser("zs", 22);
		SampleUser ls = new SampleUser("ls", 22);
		AtomicReference<SampleUser> userAtomicReference = new AtomicReference<SampleUser>();
		userAtomicReference.set(zs);
		System.out.println(userAtomicReference.compareAndSet(zs, ls) + "\t" + userAtomicReference.get().toString());
		System.out.println(userAtomicReference.compareAndSet(zs, ls) + "\t" + userAtomicReference.get().toString());
	}

}