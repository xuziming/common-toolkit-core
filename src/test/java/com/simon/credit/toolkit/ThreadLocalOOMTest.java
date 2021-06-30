package com.simon.credit.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadLocal造成OOM测试
 * @author xuziming 2021-06-30
 */
public class ThreadLocalOOMTest {

    private static final int THREAD_LOOP_SIZE = 500;
    private static final int MOCK_BIG_DATA_LOOP_SIZE = 10000;
    private static ThreadLocal<List<User>> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_LOOP_SIZE);
        for (int i = 0; i < MOCK_BIG_DATA_LOOP_SIZE; i++) {
            executorService.execute(() -> {
                threadLocal.set(new ThreadLocalOOMTest().addBigList());
                Thread t = Thread.currentThread();
                System.out.println(t.getName());
            });
            try {
                Thread.sleep(200L);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private List<User> addBigList() {
        List<User> params = new ArrayList<>(MOCK_BIG_DATA_LOOP_SIZE);
        for (int i = 0; i < MOCK_BIG_DATA_LOOP_SIZE; i++) {
            params.add(new User("XXX", "password" + i, "男", i));
        }

        return params;
    }

    class User {
        private String userName;
        private String password;
        private String sex;
        private int age;

        public User(String userName, String password, String sex, int age) {
            this.userName = userName;
            this.password = password;
            this.sex = sex;
            this.age = age;
        }
    }

}