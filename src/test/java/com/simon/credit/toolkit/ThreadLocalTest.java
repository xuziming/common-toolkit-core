package com.simon.credit.toolkit;

import com.simon.credit.toolkit.lang.MyThreadLocal;

import java.util.Random;

public class ThreadLocalTest {

    private static final MyThreadLocal<Integer> MTL = new MyThreadLocal<>();

    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                int data = new Random().nextInt();
                System.out.println(Thread.currentThread().getName() + " has put data :" + data);
                MTL.set(data);
                MyThreadScopeData.getThreadInstance().setName("name" + data);
                MyThreadScopeData.getThreadInstance().setAge(data);
                new A().get();
                new B().get();
            }).start();
        }
    }

    static class A {
        public void get() {
            int data = MTL.get();
            System.out.println("A from " + Thread.currentThread().getName() + " get data :" + data);
            MyThreadScopeData myData = MyThreadScopeData.getThreadInstance();
            System.out.println("A from " + Thread.currentThread().getName() + " getMyData: " + myData.getName() + "," + myData.getAge());
        }
    }

    static class B {
        public void get() {
            int data = MTL.get();
            System.out.println("B from " + Thread.currentThread().getName() + " get data :" + data);
            MyThreadScopeData myData = MyThreadScopeData.getThreadInstance();
            System.out.println("B from " + Thread.currentThread().getName() + " getMyData: " + myData.getName() + "," + myData.getAge());
        }
    }

    static class MyThreadScopeData {
        private static final MyThreadLocal<MyThreadScopeData> TL = new MyThreadLocal<MyThreadScopeData>();

        private MyThreadScopeData() {}

        public static MyThreadScopeData getThreadInstance() {
            MyThreadScopeData instance = TL.get();
            if (instance == null) {
                instance = new MyThreadScopeData();
                TL.set(instance);
            }
            return instance;
        }

        private String name;
        private int age;

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

}