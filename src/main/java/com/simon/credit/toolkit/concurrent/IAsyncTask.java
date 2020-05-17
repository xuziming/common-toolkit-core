package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.Callable;

/**
 * 异步任务接口
 * @param <T>
 * @author simon 2020-05-17
 */
public interface IAsyncTask<T> extends Callable<T> {
    // 空接口
}