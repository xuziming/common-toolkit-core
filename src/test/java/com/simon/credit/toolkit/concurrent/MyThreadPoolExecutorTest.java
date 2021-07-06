package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPoolExecutorTest {

    public static void main(String[] args) {
        MyThreadPoolExecutor executor = null;
        try {
            // 创建线程池
            executor = new MyThreadPoolExecutor(2, 5, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2));

            for (int i = 1; i <= 50; i++) {
                Task task = new Task("task-" + i);
                executor.execute(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
        }
    }

}

class Task extends Thread {

    private String taskName;

    Task(String taskName) {
        this.taskName = taskName;
    }

    public void run() {
        try {
            System.out.println(taskName + " is running...");
            Thread.sleep(1000);// 线程睡眠一秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}