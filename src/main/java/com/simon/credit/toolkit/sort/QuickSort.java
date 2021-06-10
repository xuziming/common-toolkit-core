package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 快速排序
 * @author XUZIMING 2019-11-18
 */
public final class QuickSort {

	public static void main(String[] args) {
 		int[] array = { 6, 1, 2, 7, 9, 3, 4, 5, 10, 8 };
		QuickSort.quickSort(array, 0, array.length - 1);
		System.out.println(Arrays.toString(array));
	}

	public static final void sort(int[] array) {
		quickSort(array, 0, array.length - 1);
	}

	/**
	 * 已废弃：写法比较复杂，不利于记忆
	 * @param array
	 * @param left
	 * @param right
	 */
	@Deprecated
	public static final void sort(int[] array, int left, int right) {
		int  leftIndex = left; // 左下标
		int rightIndex = right;// 右下标
		int pivot = array[(left + right) / 2];// pivot: 中轴值

		// while循环的目的: 将比pivot值小的放到左边, 比pivot值大的放到右边
		while (leftIndex < rightIndex) {
			// 在pivot的左边(从左向右)一直找, 直至找到大于或等于pivot的值, 才退出
			while (array[leftIndex] < pivot) {
				leftIndex++;
			}

			// 在pivot的右边(从右向左)一直找, 直至找到小于或等于pivot的值, 才退出
			while (array[rightIndex] > pivot) {
				rightIndex--;
			}

			// 如果leftIndex >= rightIndex, 说明pivot左右两边的值已经按照：
			// 左边全部是小于等于pivot的值，右边全部是大于等于pivot的值排序
			if (leftIndex >= rightIndex) {
				break;
			}

			// 交换
			swap(array, leftIndex, rightIndex);

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

	public static final void quickSort(int[] array, int low, int high) {
		if (low > high) {
			return;
		}

		int i = low;
		int j = high;
		int benchmark = array[low];// 基准

		while (i < j) {
			// 先看右边，依次往左递减
			while (array[j] >= benchmark && i < j) {
				j--;
			}
			// 再看左边，依次往右递增
			while (array[i] <= benchmark && i < j) {
				i++;
			}
			// 如果满足条件则交换
			if (i < j) {
				swap(array, i, j);
			}
		}

		// 最后将基准与(i或j)位置的数字交换
		swap(array, low, i);// 此时i等于j

		// 递归快排左边数字
		quickSort(array, low, j - 1);

		// 递归快排右边数字
		quickSort(array, j + 1, high);
	}

	private static final void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

}