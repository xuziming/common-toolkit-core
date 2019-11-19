package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 堆排序
 * @author XUZIMING 2019-11-19
 */
public class HeapSort {

	public static void main(String[] args) {
		// 要求将数组进行升序排序
		int[] arr = { 4, 6, 8, 5, 9, -11, 666, -5555, 93, 1589, 55, 7 };

		maxHeapSort(arr);
		System.out.println("大堆排序后(升序)：" + Arrays.toString(arr));

		minHeapSort(arr);
		System.out.println("小堆排序后(降序)：" + Arrays.toString(arr));
	}

	/**
	 * 大堆排序(升序排序)
	 * @param arr 待排序的数组
	 */
	public static void maxHeapSort(int[] arr) {
		int temp = 0;
		System.out.println("大堆排序!!");

//		// 分步完成
//		adjustMaxHeap(arr, 1, arr.length);
//		System.out.println("第1次：" + Arrays.toString(arr));// 4,9,8,5,6
//
//		adjustMaxHeap(arr, 0, arr.length);
//		System.out.println("第2次：" + Arrays.toString(arr));// 9,6,8,5,4

		// 完成我们的最终代码
		/**
		 * 1).将无序序列构造成一个堆，根据升序降序需求选择大顶堆或小顶堆
		 */
		for (int i = arr.length / 2 - 1; i >= 0; i--) {
			adjustMaxHeap(arr, i, arr.length);
		}

		/**
		 * 2).将堆顶元素与末尾元素交换，将最大元素“沉”到数组末端；
		 * 3).重新调整结构，使其满足堆定义，然后继续交换顶堆元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序。
		 */
		for (int j = arr.length - 1; j > 0; j--) {
			// 交换(最大值沉淀)
			temp   = arr[j];
			arr[j] = arr[0];
			arr[0] = temp;

			adjustMaxHeap(arr, 0, j);
		}
	}

	/**
	 * 小堆排序(降序排序)
	 * @param arr 待排序的数组
	 */
	public static void minHeapSort(int[] arr) {
		int temp = 0;
		System.out.println("小堆排序!!");

		// 完成我们的最终代码
		/**
		 * 1).将无序序列构造成一个堆，根据升序降序需求选择大顶堆或小顶堆
		 */
		for (int i = arr.length / 2 - 1; i >= 0; i--) {
			adjustMinHeap(arr, i, arr.length);
		}

		/**
		 * 2).将堆顶元素与末尾元素交换，将最小元素“沉”到数组末端；
		 * 3).重新调整结构，使其满足堆定义，然后继续交换顶堆元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序。
		 */
		for (int j = arr.length - 1; j > 0; j--) {
			// 交换(最小值沉淀)
			temp   = arr[j];
			arr[j] = arr[0];
			arr[0] = temp;
			adjustMinHeap(arr, 0, j);
		}
	}

	/**
	 * 将一个数组(二叉树)，调整为一个大顶堆
	 * @param arr
	 * @param i
	 * @param length
	 */
	public static void adjustMaxHeap(int[] arr, int i, int length) {
		int temp = arr[i];// 先取出当前元素的值，保存在临时变量
		// 开始调整
		// 说明
		// 1. k=i*2+1, k是i节点的左子节点
		for (int k = i * 2 + 1; k < length; k = k * 2 + 1) {
			if (k + 1 < length && arr[k] < arr[k + 1]) {// 说明左子节点的值小于右子节点的值
				k++;// k指向右子节点(取大值)
			}

			if (arr[k] > temp) {// 如果子节点大于父节点
				arr[i] = arr[k];
				i = k;// !!! i指向k,继续循环比较
			} else {
				break;// ！
			}
		}

		// 当for循环结束后，我们已经将以i为父节点的树的最大值，放在了最顶(局部)
		arr[i] = temp;
	}

	/**
	 * 将一个数组(二叉树)，调整为一个小顶堆
	 * @param arr
	 * @param i
	 * @param length
	 */
	public static void adjustMinHeap(int[] arr, int i, int length) {
		int temp = arr[i];// 先取出当前元素的值，保存在临时变量
		// 开始调整
		// 说明
		// 1. k=i*2+1, k是i节点的左子节点
		for (int k = i * 2 + 1; k < length; k = k * 2 + 1) {
			if (k + 1 < length && arr[k] > arr[k + 1]) {// 说明左子节点的值大于右子节点的值
				k++;// k指向右子节点(取小值)
			}

			if (arr[k] < temp) {// 如果子节点小于父节点
				arr[i] = arr[k];
				i = k;// !!! i指向k,继续循环比较
			} else {
				break;// ！
			}
		}

		// 当for循环结束后，我们已经将以i为父节点的树的最小值，放在了最顶(局部)
		arr[i] = temp;
	}

}
