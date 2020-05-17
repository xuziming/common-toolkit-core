package com.simon.credit.toolkit.concurrent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AsyncTaskBuilderTest {

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AsyncTaskBuilder builder = new AsyncTaskBuilder();

        for (int i = 0; i < 10000; i++) {
            builder.append(() -> now());
        }

        // 构建任务列表
        List<IAsyncTask<String>> asyncTaskList = builder.buildAsList();

        // 同步处理任务列表
        List<String> results = new AsyncTaskHandler<String>().syncHandle(asyncTaskList);

        for (Object result : results) {
            // 打印任务处理结果
            System.out.println("echo: " + result);
        }
    }

}