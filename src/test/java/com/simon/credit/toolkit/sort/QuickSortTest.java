package com.simon.credit.toolkit.sort;

import com.simon.credit.toolkit.sort.QuickSort;

/**
 * 快速排序测试
 * @author XUZIMING 2019-11-28
 */
public class QuickSortTest {

	public static void main(String[] args) {
// 		int[] array = { -9, 78, 0, 23, -567, 70, -1, 900, 4561 };
//		QuickSort.sort(array, 0, array.length - 1);
//		System.out.println("arr=" + Arrays.toString(array));

		SortAlgorithmTest.performanceTest(new SortAlgorithm() {
			@Override
			public void sort(int[] array) {
				QuickSort.sort(array);// 快速排序
			}
		});
	}

}
