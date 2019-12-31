package com.simon.credit.toolkit.sort;

/**
 * 归并排序
 * @author XUZIMING 2019-11-18
 */
public final class MergeSort {

	public static final void sort(int[] array) {
		mergeSort(array, 0, array.length - 1, new int[array.length]);
	}

	/**
	 * 分割+合并
	 * @param array
	 * @param left
	 * @param right
	 * @param temp
	 */
	private static final void mergeSort(int[] array, int left, int right, int[] temp) {
		if (left < right) {
			int middle = (left + right) / 2;// 中间索引
			// 向左递归进行分解
			mergeSort(array, left, middle, temp);
			// 向右递归进行分解
			mergeSort(array, middle + 1, right, temp);
			// 合并
			merge(array, left, middle, right, temp);
		}
	}

	/**
	 * 归并排序
	 * @param array 待排序的数组
	 * @param left  左边有序序列的初始索引
	 * @param mid   中间索引
	 * @param right 右边索引
	 * @param temp  用作中转的数组
	 */
	public static void merge(int[] array, int left, int mid, int right, int[] temp) {
		int leftIndex  = left;// 初始化leftIndex,左边有序序列的初始索引
		int rightIndex = mid + 1;// 初始化rightIndex,右边有序序列的初始索引
		int tempIndex  = 0;// 指向temp数组的当前索引

		// (一)
		// 先把左右两边(有序)的数据按照规则填充到temp数组
		// 直到左右两边的有序序列，有一边处理完毕为止
		while (leftIndex <= mid && rightIndex <= right) {// 继续
			// 如果左边的有序序列的当前元素，小于等于右边有序序列的当前元素
			// 则将左边的当前元素拷贝到temp数组
			if (array[leftIndex] <= array[rightIndex]) {
				temp[tempIndex++] = array[leftIndex++];
			} else {// 反之，将右边的有序序列的当前元素填充到temp数组
				temp[tempIndex++] = array[rightIndex++];
			}
		}

		// (二)
		// 将有剩余数据的一边的数据依次全部填充到temp数组
		while (leftIndex <= mid) {
			temp[tempIndex++] = array[leftIndex++];
		}

		while (rightIndex <= right) {
			temp[tempIndex++] = array[rightIndex++];
		}

		// (三)
		// 将temp数组的元素拷贝到array
		// 注意：并不是每次都拷贝所有的元素
		tempIndex = 0;
		int tempLeft = left;
		// 第一次合并 tempLeft=0,right=1; tempLeft=2,right=3; tempLeft=0,right=3
		// 最后一次合并 tempLeft=0,right=7
		while (tempLeft <= right) {
			array[tempLeft++] = temp[tempIndex++];
		}
	}

}