package com.simon.credit.toolkit.sort;

public class HeapSortTest {

	public static void main(String[] args) {
//		// 待排序数组
//		int[] array = { 4, 6, 8, 5, 9, -11, 666, -22, 93, 189, 55, 7 };
//
//		HeapSort.maxHeapSort(array);
//		System.out.println("大堆排序后(升序)：" + Arrays.toString(array));
//
//		HeapSort.minHeapSort(array);
//		System.out.println("小堆排序后(降序)：" + Arrays.toString(array));

		SortAlgorithmTest.performanceTest(new SortAlgorithm() {
			@Override
			public void sort(int[] array) {
				HeapSort.maxHeapSort(array);// 大堆排序(升序)
			}
		});

//		SortAlgorithmPerformanceTest.doTest(new SortAlgorithm() {
//			@Override
//			public void sort(int[] array) {
//				HeapSort.minHeapSort(array);// 小堆排序(降序)
//			}
//		});
	}

}
