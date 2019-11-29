package com.simon.credit.toolkit.sort;

import com.simon.credit.toolkit.sort.InsertSort;

public class InsertSortTest {

	public static void main(String[] args) {
//		int[] array = { 101, 34, 119, 1 };
//		InsertSort.sort(array);
//		System.out.println(Arrays.toString(array));

//		SortAlgorithmTest.validateCorrectness(new SortAlgorithm() {
//			@Override
//			public void sort(int[] array) {
//				InsertSort.sort(array);
//			}
//		});

		SortAlgorithmTest.performanceTest(new SortAlgorithm() {
			@Override
			public void sort(int[] array) {
				InsertSort.sort(array);
			}
		});
	}

}
