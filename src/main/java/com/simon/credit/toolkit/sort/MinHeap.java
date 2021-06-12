package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 小顶堆
 * @author XUZIMING 2019-11-19
 */
public final class MinHeap {

	public static void main(String[] args) {
		int[] num = new int[]{3, 7, 2, 1, 21, 20, 2};
		MinHeap.minHeapSort(num);
		System.out.println(Arrays.toString(num));
	}

	/**
	 * 小堆排序(降序排序)
	 * @param array 构建小顶堆之后对应的数据存储
	 */
	public static final void minHeapSort(int[] array) {
		// 1、将无序序列构造成一个堆，升序选择大顶堆，降序选择小顶堆
		buildMinHeap(array);

		for (int count = array.length - 1; count > 0; count--) {
			// 2、将堆顶元素与末尾元素交换，把最小元素“沉”到数组末端；
			swap(array, 0, count);
			// 3、重新调整结构，使其满足堆定义，然后继续交换顶堆元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序
			heapify(array, 0, count);
		}
	}

	/**
	 * 将无序序列构造成一个堆，升序选择大顶堆，降序选择小顶堆
	 * @param array 待排序数组
	 * @return
	 */
	public static final int[] buildMinHeap(int[] array) {
		// 堆的构建过程就是从最后一个非叶子结点开始从下往上调整
		for (int i = array.length / 2 - 1; i >= 0; i--) {
			heapify(array, i, array.length);
		}
		return array;
	}

	/**
	 * 从array[i]向下进行堆调整
	 *
	 * @param array
	 * @param i
	 * @param heapSize
	 */
	public static void heapify(int[] array, int i, int heapSize) {
		int  leftChild = 2 * i + 1;
		int rightChild = 2 * i + 2;

		int min = i;// 初始将i位置的值标记为最小值
		if (leftChild < heapSize && array[leftChild] < array[min]) {
			min = leftChild;
		}
		if (rightChild < heapSize && array[rightChild] < array[min]) {
			min = rightChild;
		}

		if (min != i) {
			swap(array, i, min);// 交换
			heapify(array, min, heapSize);// 堆结构发生了变化，递归向下进行堆调整
		}
	}

	private static final void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

}