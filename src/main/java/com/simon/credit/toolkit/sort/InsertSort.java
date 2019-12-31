package com.simon.credit.toolkit.sort;

/**
 * 插入排序
 * @author XUZIMING 2019-11-28
 */
public final class InsertSort {

	public static void sort(int[] array) {
		int insertVal = 0;
		int insertIndex = 0;

		for (int i = 1; i < array.length; i++) {
			// 定义待插入的数
			insertVal = array[i];
			insertIndex = i - 1;// 即array[i]的前面这个数的下标

			// 给insertValue找到插入的位置
			// 说明
			// 1. insertIndex >= 0 保证在给insertValue 找插入位置，不越界
			// 2. insertValue < array[insertIndex] 待插入的数，还没有找到插入位置
			// 3. 就需要将 array[insertIndex] 后移
			while (insertIndex >= 0 && insertVal < array[insertIndex]) {
				array[insertIndex + 1] = array[insertIndex];// arr[insertIndex]
				insertIndex--;
			}

			// 当退出while循环时，说明插入的位置找到, insertIndex + 1
			if (insertIndex + 1 != i) {// 判断是否需要赋值
				array[insertIndex + 1] = insertVal;
			}
		}
	}

}