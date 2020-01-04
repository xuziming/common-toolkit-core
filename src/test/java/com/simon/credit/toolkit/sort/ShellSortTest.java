package com.simon.credit.toolkit.sort;

import com.simon.credit.toolkit.sort.ShellSort;

public class ShellSortTest {

	public static void main(String[] args) {
//		int[] array = { 8, 9, 1, 7, 2, 3, 5, 4, 6, 0 };
//		ShellSort.sort(array);
//		System.out.println(Arrays.toString(array));

		SortAlgorithmTest.performanceTest(new SortAlgorithm() {
			@Override
			public void sort(int[] array) {
				// ShellSort.shellSort(array);// 交换式(效率低,已废弃)
				ShellSort.sort(array);// 移位式
			}
		});
	}

}