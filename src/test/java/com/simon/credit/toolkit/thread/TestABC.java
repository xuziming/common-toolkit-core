package com.simon.credit.toolkit.thread;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class TestABC {

    public static void main(String[] args) throws Exception {
        testABC3();
    }

    private static void testABC3() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);

        Thread threadA = new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());

            // 冲破栅栏代表A线程结束
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
                throw new RuntimeException("cylicBarrier.await()拋出異常：", e);
            }
        }, "Thread-A");

        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());

            // 冲破栅栏代表B线程结束
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
                throw new RuntimeException("cylicBarrier.await()拋出異常：", e);
            }
        }, "Thread-B");

        Thread threadC = new Thread(() -> {
            // 等待前两个(A/B)线程结束，只有前两个(A/B)线程结束了才能满足3个线程都冲破栅栏，
            try {
                // 等待栅栏被冲破，冲破栅栏的条件是：A/B/C三个线程都到达await()。
                // 只有栅栏冲破，才能向下执行，否则先到达的线程等待。
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
                throw new RuntimeException("cylicBarrier.await()拋出異常：", e);
            }
            // 满足了三个线程都冲破栅栏才向下执行
            System.out.println(Thread.currentThread().getName());
        }, "Thread-C");

        threadA.start();
        threadB.start();
        threadC.start();
    }

    private static void testABC2() {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        Thread threadA = new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, "Thread-A");

        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, "Thread-B");

        Thread threadC = new Thread(() -> {
            // 在C中等待A/B运行结束
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("CountDownLatch等待失败。。。", e);
            }
            System.out.println(Thread.currentThread().getName());
        }, "Thread-C");

        threadA.start();
        threadB.start();
        threadC.start();
    }

    private static void testABC1() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Semaphore semaphoreC = new Semaphore(1);

        Thread threadA = new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, "Thread-A");

        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, "Thread-B");

        Thread threadC = new Thread(() -> {
            try {
                semaphoreC.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            semaphoreC.release();
        }, "Thread-C");

        // 占用C锁，直到A/B线程完成后，才释放C锁。
        semaphoreC.acquire();

        threadA.start();
        threadB.start();
        threadC.start();

        countDownLatch.await();
        // 释放C锁，让C线程有获取锁的可能
        semaphoreC.release();
    }

}