package com.simon.credit.toolkit;

import java.util.Arrays;

import com.simon.credit.toolkit.sort.HeapSort;

public class HeapSortTest {

	public static void main(String[] args) {
		// 待排序数组
		int[] arr = { 4, 6, 8, 5, 9, -11, 666, -22, 93, 189, 55, 7 };

		HeapSort.maxHeapSort(arr);
		System.out.println("大堆排序后(升序)：" + Arrays.toString(arr));

		HeapSort.minHeapSort(arr);
		System.out.println("小堆排序后(降序)：" + Arrays.toString(arr));
	}

}
