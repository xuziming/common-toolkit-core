package com.simon.credit.toolkit;

import java.util.Arrays;

import com.simon.credit.toolkit.sort.QuickSort;

public class QuickSortTest {

	public static void main(String[] args) {
		// int[] array = { -9, 78, 0, 23, -567, 70, -1, 900, 4561 };
		int[] array = { 100, 5, -10, -1, -2, 98, -4, -5, -6, -7, -8 };
		QuickSort.sort(array, 0, array.length - 1);
		System.out.println("arr=" + Arrays.toString(array));

		// 测试快排的执行速度
		// 创建要给80000个的随机的数组
//		int[] array = new int[8000000];
//		for (int i = 0; i < 8000000; i++) {
//			array[i] = (int) (Math.random() * 8000000); // 生成一个[0, 8000000) 数
//		}
//
//		System.out.println("排序前");
//		Date data1 = new Date();
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String date1Str = simpleDateFormat.format(data1);
//		System.out.println("排序前的时间是=" + date1Str);
//
//		quickSort(array, 0, array.length - 1);
//
//		Date data2 = new Date();
//		String date2Str = simpleDateFormat.format(data2);
//		System.out.println("排序前的时间是=" + date2Str);
//		// System.out.println("array=" + Arrays.toString(array));
	}

}
