package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 归并排序
 * @author XUZIMING 2019-11-18
 */
public class MergeSort {

	public static void main(String[] args) {
		int[] arr = { 8, 4, 5, 7, 1, 3, 6, 2 };
		mergeSort(arr);
		System.out.println("归并排序之后 = " + Arrays.toString(arr));
	}

	public static void mergeSort(int[] arr) {
		mergeSort(arr, 0, arr.length - 1, new int[arr.length]);
	}

	/**
	 * 分割+合并
	 * @param arr
	 * @param left
	 * @param right
	 * @param temp
	 */
	public static void mergeSort(int[] arr, int left, int right, int[] temp) {
		if (left < right) {
			int mid = (left + right) / 2;// 中间索引
			// 向左递归进行分解
			mergeSort(arr, left, mid, temp);
			// 向右递归进行分解
			mergeSort(arr, mid + 1, right, temp);
			// 合并
			merge(arr, left, mid, right, temp);
		}
	}

	/**
	 * 归并排序
	 * @param arr   待排序的数组
	 * @param left  左边有序序列的初始索引
	 * @param mid   中间索引
	 * @param right 右边索引
	 * @param temp  用作中转的数组
	 */
	public static void merge(int[] arr, int left, int mid, int right, int[] temp) {
		int i = left;// 初始化i,左边有序序列的初始索引
		int j = mid + 1;// 初始化j,右边有序序列的初始索引
		int t = 0;// 指向temp数组的当前索引

		// (一)
		// 先把左右两边(有序)的数据按照规则填充到temp数组
		// 直到左右两边的有序序列，有一边处理完毕为止
		while (i <= mid && j <= right) {// 继续
			// 如果左边的有序序列的当前元素，小于等于右边有序序列的当前元素
			// 则将左边的当前元素拷贝到temp数组
			if (arr[i] <= arr[j]) {
				temp[t++] = arr[i++];
			} else {// 反之，将右边的有序序列的当前元素填充到temp数组
				temp[t++] = arr[j++];
			}
		}

		// (二)
		// 将有剩余数据的一边的数据依次全部填充到temp数组
		while (i <= mid) {
			temp[t++] = arr[i++];
		}

		while (j <= right) {
			temp[t++] = arr[j++];
		}

		// (三)
		// 将temp数组的元素拷贝到arr
		// 注意：并不是每次都拷贝所有的元素
		t = 0;
		int tempLeft = left;
		// 第一次合并 tempLeft=0,right=1; tempLeft=2,right=3; tempLeft=0,right=3
		// 最后一次合并 tempLeft=0,right=7
		while (tempLeft <= right) {
			arr[tempLeft++] = temp[t++];
		}
	}

}
