package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 快速排序
 * @author XUZIMING 2019-11-18
 */
public final class QuickSort {

	public static void main(String[] args) {
 		int[] array = { -9, 78, 0, 23, -567, 70, -1, 900, 4561 };
 		// 快速排序
		QuickSort.sort(array, 0, array.length - 1);
		System.out.println(Arrays.toString(array));
	}

	public static final void sort(int[] array) {
		sort(array, 0, array.length - 1);
	}

	public static final void sort(int[] array, int left, int right) {
		int  leftIndex = left; // 左下标
		int rightIndex = right;// 右下标
		int pivot = array[(left + right) / 2];// pivot: 中轴值
		int temp = 0; // 临时变量，作为交换时使用

		// while循环的目的: 将比pivot值小的放到左边, 比pivot值大的放到右边
		while (leftIndex < rightIndex) {
			// 在pivot的左边一直找, 直至找到大于或等于pivot的值, 才退出
			while (array[leftIndex] < pivot) {
				leftIndex++;
			}

			// 在pivot的右边一直找, 直至找到小于或等于pivot的值, 才退出
			while (array[rightIndex] > pivot) {
				rightIndex--;
			}

			// 如果leftIndex >= rightIndex, 说明pivot左右两边的值已经按照：
			// 左边全部是小于等于pivot的值，右边全部是大于等于pivot的值排序
			if (leftIndex >= rightIndex) {
				break;
			}

			// 交换
			temp = array[leftIndex];
			array[leftIndex] = array[rightIndex];
			array[rightIndex] = temp;

			// 如果交换完后, 发现这个array[leftIndex]==pivot, 则rightIndex--, 前(左)移一步
			if (array[leftIndex] == pivot) {
				rightIndex--;
			}

			// 如果交换完后, 发现这个array[rightIndex]==pivot, 则leftIndex++, 后(右)移一步
			if (array[rightIndex] == pivot) {
				leftIndex++;
			}
		}

		// 如果leftIndex==rightIndex, 必须leftIndex++, rightIndex--, 否则为出现栈溢出
		if (leftIndex == rightIndex) {
			leftIndex++;
			rightIndex--;
		}

		// 向左递归
		if (left < rightIndex) {
			sort(array, left, rightIndex);
		}

		// 向右递归
		if (right > leftIndex) {
			sort(array, leftIndex, right);
		}
	}

}