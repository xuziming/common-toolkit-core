package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 排序算法性能测试
 * @author XUZIMING 2019-11-28
 */
public class SortAlgorithmTest {

	/**
	 * 排序算法性能测试
	 * @param sortAlgorithm 排序算法
	 */
	public static void performanceTest(SortAlgorithm sortAlgorithm) {
		// 创建要给8000000个的随机的数组
		int[] array = new int[8000000];
		for (int i = 0; i < 8000000; i++) {
			array[i] = (int) (Math.random() * 8000000);// 生成一个[0, 8000000)的随机数
		}

		long start = System.currentTimeMillis();
		System.out.println("排序前的时间戳是: " + start);

		sortAlgorithm.sort(array);

		long end = System.currentTimeMillis();
		System.out.println("排序后的时间戳是: " + end);
		System.out.println("排序前后时间差是: " + (end - start) + "毫秒");
	}

	/**
	 * 校验排序算法的正确性
	 * @param sortAlgorithm 排序算法
	 */
	public static void validateCorrectness(SortAlgorithm sortAlgorithm) {
		// 创建要给8个的随机的数组
		int[] array = new int[8];
		for (int i = 0; i < 8; i++) {
			array[i] = (int) (Math.random() * 8000000);// 生成一个[0, 8000000)的随机数
		}

		sortAlgorithm.sort(array);
		System.out.println("数组排序后: " + Arrays.toString(array));
	}

}