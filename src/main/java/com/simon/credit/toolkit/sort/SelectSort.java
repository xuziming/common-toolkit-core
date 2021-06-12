package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 选择排序
 * @author xuziming 2019-11-28
 */
public class SelectSort {

    public static void main(String[] args) {
        int arr[] = {3, 9, -1, 10, 20};

        System.out.println("排序前: " + Arrays.toString(arr));

        // 测试选择排序
        selectSort(arr);

        System.out.println("排序后: " + Arrays.toString(arr));
    }

    public static void selectSort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int min = arr[i];
            int minIndex = i;
            // 遍历
            for (int j = i + 1; j < arr.length; j++) {
                if (min > arr[j]) { // 说明min不是真的最小值
                    min = arr[j]; // 重置min
                    minIndex = j; // 重置minIndex
                }
            }
            // 判断是否需要交换
            if (minIndex != i) {
                arr[minIndex] = arr[i];
                arr[i] = min;
            }
        }
    }

}