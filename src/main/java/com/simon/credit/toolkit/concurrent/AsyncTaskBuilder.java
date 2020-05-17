package com.simon.credit.toolkit.concurrent;

import java.util.Arrays;
import java.util.List;

/**
 * 异步任务构造器
 * @author simon
 */
public class AsyncTaskBuilder<T> {

    /** 默认容量：10 */
    private static final int DEFAULT_CAPACITY = 10;

    /** 任务数组 */
    private IAsyncTask<T>[] tasks;

    /** 任务数量 */
    private int count;

    public AsyncTaskBuilder() {
        this(DEFAULT_CAPACITY);
    }

    public AsyncTaskBuilder(int capacity) {
        tasks = new IAsyncTask[capacity];
    }

    public AsyncTaskBuilder(IAsyncTask<T> task) {
        this(DEFAULT_CAPACITY);
        append(task);
    }

    /**
     * 追加一个任务
     * @param task
     * @return
     */
    public AsyncTaskBuilder append(IAsyncTask<T> task) {
        if (task == null) {
            throw new NullPointerException("task is null, cannot be append");
        }

        ensureCapacity(count + 1);
        tasks[count++] = task;

        return this;
    }

    /**
     * 构建任务
     * @return 任务数组
     */
    private IAsyncTask<T>[] build() {
        IAsyncTask<T>[] copy = new IAsyncTask[count];
        System.arraycopy(tasks, 0, copy, 0, count);
        return copy;
    }

    /**
     * 构建任务列表
     * @return 返回的任务列表不支持add、delete等操作
     */
    public List<IAsyncTask> buildAsList() {
        IAsyncTask<T>[] copy = build();
        return Arrays.asList(copy);
    }

    /**
     * 获取任务数量
     */
    public int length() {
        return count;
    }

    /**
     * 获取容量
     */
    public int capacity() {
        return tasks.length;
    }

    /**
     * 确保容量
     * @param minimumCapacity
     */
    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > 0) {
            ensureCapacityInternal(minimumCapacity);
        }
    }

    /**
     * 确保容量(内部)
     * @param minimumCapacity
     */
    private void ensureCapacityInternal(int minimumCapacity) {
        // 最小容量大于当前容量时进行扩容
        if (minimumCapacity - tasks.length > 0) {
            expandCapacity(minimumCapacity);
        }
    }

    /**
     * 扩容
     * @param minimumCapacity 最小容量值
     */
    private void expandCapacity(int minimumCapacity) {
        // 新容量为当前容量的2倍
        int newCapacity = tasks.length * 2;

        // 扩容之后的新容量若小于最小容量，则新容量为最小容量
        if (newCapacity < minimumCapacity) {
            newCapacity = minimumCapacity;
        }

        if (newCapacity < 0) {
            // overflow(长度溢出)
            if (minimumCapacity < 0) {
                throw new OutOfMemoryError();
            }
            newCapacity = Integer.MAX_VALUE;
        }

        // 任务迁移到新容器
        tasks = Arrays.copyOf(tasks, newCapacity);
    }

}