package com.simon.credit.toolkit.sort;

import com.simon.credit.toolkit.common.CommonToolkits;

import java.util.Date;

/**
 * 希尔排序
 * @author XUZIMING 2019-11-28
 */
public final class ShellSort {

	public static void main(String[] args) {
		// 创建一个8000000个随机数据的数组
		int[] arr = new int[8000000];
		for (int i = 0; i < 8000000; i++) {
			arr[i] = (int) (Math.random() * 80000000); // 生成一个[0,800000) 的一个数
		}

		System.out.println("排序前时间=" + CommonToolkits.formatDate(new Date())); // 输出时间
		//shellSort(arr); // 速度慢 超过10秒排 8000000个数据
		sort(arr); // 速度提升很多，4秒左右，排8000000个数据
		System.out.println("排序后时间=" + CommonToolkits.formatDate(new Date())); // 输出时间
	}

	/**
	 * 使用逐步推导的方式来编写希尔排序
	 * 希尔排序时， 对有序序列在插入时采用交换法,
	 * 思路(算法) ===> 代码
	 * <pre>该算法存在效率问题，已废弃</pre>
	 */
	@Deprecated
	public static final void shellSort(int[] array) {
		int temp = 0;
		// int count = 0;
		// 根据前面的逐步分析，使用循环处理
		for (int gap = array.length / 2; gap > 0; gap /= 2) {
			for (int i = gap; i < array.length; i++) {
				// 遍历各组中所有的元素(共gap组，每组有个元素), 步长gap
				for (int j = i - gap; j >= 0; j -= gap) {
					// 如果当前元素大于加上步长后的那个元素，说明交换
					if (array[j] > array[j + gap]) {
						temp = array[j];
						array[j] = array[j + gap];
						array[j + gap] = temp;
					}
				}
			}
		}
	}

//	private static final void originShellSort(int[] array) {
//		int temp = 0;
//		// 希尔排序的第1轮排序
//		// 因为第1轮排序，是将10个数据分成了 5组
//		for (int i = 5; i < array.length; i++) {
//			// 遍历各组中所有的元素(共5组，每组有2个元素), 步长5
//			for (int j = i - 5; j >= 0; j -= 5) {
//				// 如果当前元素大于加上步长后的那个元素，说明交换
//				if (array[j] > array[j + 5]) {
//					temp = array[j];
//					array[j] = array[j + 5];
//					array[j + 5] = temp;
//				}
//			}
//		}
//
//		System.out.println("希尔排序1轮后=" + Arrays.toString(array));//
//
//		// 希尔排序的第2轮排序
//		// 因为第2轮排序，是将10个数据分成了 5/2 = 2组
//		for (int i = 2; i < array.length; i++) {
//			// 遍历各组中所有的元素(共5组，每组有2个元素), 步长5
//			for (int j = i - 2; j >= 0; j -= 2) {
//				// 如果当前元素大于加上步长后的那个元素，说明交换
//				if (array[j] > array[j + 2]) {
//					temp = array[j];
//					array[j] = array[j + 2];
//					array[j + 2] = temp;
//				}
//			}
//		}
//
//		System.out.println("希尔排序2轮后=" + Arrays.toString(array));//
//
//		// 希尔排序的第3轮排序
//		// 因为第3轮排序，是将10个数据分成了 2/2 = 1组
//		for (int i = 1; i < array.length; i++) {
//			// 遍历各组中所有的元素(共5组，每组有2个元素), 步长5
//			for (int j = i - 1; j >= 0; j -= 1) {
//				// 如果当前元素大于加上步长后的那个元素，说明交换
//				if (array[j] > array[j + 1]) {
//					temp = array[j];
//					array[j] = array[j + 1];
//					array[j + 1] = temp;
//				}
//			}
//		}
//
//		System.out.println("希尔排序3轮后=" + Arrays.toString(array));//
//	}

	/**
	 * 对交换式的希尔排序进行优化->移位法
	 * @param array
	 */
	public static final void sort(int[] array) {
		// 增量gap, 并逐步的缩小增量
		for (int gap = array.length / 2; gap > 0; gap /= 2) {
			// 从第gap个元素，逐个对其所在的组进行直接插入排序
			for (int i = gap; i < array.length; i++) {
				int j = i;
				int temp = array[j];
				if (array[j] < array[j - gap]) {
					while (j - gap >= 0 && temp < array[j - gap]) {
						// 移动
						array[j] = array[j - gap];
						j -= gap;
					}
					// 当退出while后，就给temp找到插入的位置
					array[j] = temp;
				}
			}
		}
	}

}