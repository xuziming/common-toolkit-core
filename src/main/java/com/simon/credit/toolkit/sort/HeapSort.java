package com.simon.credit.toolkit.sort;

/**
 * 堆排序
 * @author XUZIMING 2019-11-19
 */
public final class HeapSort {

	/**
	 * 大堆排序(升序排序)
	 * @param array 待排序的数组
	 */
	public static final void maxHeapSort(int[] array) {
		int temp = 0;

//		// 分步完成
//		adjustMaxHeap(arr, 1, arr.length);
//		System.out.println("第1次：" + Arrays.toString(arr));// 4,9,8,5,6
//
//		adjustMaxHeap(arr, 0, arr.length);
//		System.out.println("第2次：" + Arrays.toString(arr));// 9,6,8,5,4

		// 完成我们的最终代码
		/**
		 * 1).将无序序列构造成一个堆，根据升序降序需求选择大顶堆或小顶堆
		 */
		for (int i = array.length / 2 - 1; i >= 0; i--) {
			adjustMaxHeap(array, i, array.length);
		}

		/**
		 * 2).将堆顶元素与末尾元素交换，将最大元素“沉”到数组末端；
		 * 3).重新调整结构，使其满足堆定义，然后继续交换顶堆元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序。
		 */
		for (int j = array.length - 1; j > 0; j--) {
			// 交换(最大值沉淀)
			temp   = array[j];
			array[j] = array[0];
			array[0] = temp;

			adjustMaxHeap(array, 0, j);
		}
	}

	/**
	 * 小堆排序(降序排序)
	 * @param array 待排序的数组
	 */
	public static final void minHeapSort(int[] array) {
		int temp = 0;

		// 完成我们的最终代码
		/**
		 * 1).将无序序列构造成一个堆，根据升序降序需求选择大顶堆或小顶堆
		 */
		for (int i = array.length / 2 - 1; i >= 0; i--) {
			adjustMinHeap(array, i, array.length);
		}

		/**
		 * 2).将堆顶元素与末尾元素交换，将最小元素“沉”到数组末端；
		 * 3).重新调整结构，使其满足堆定义，然后继续交换顶堆元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序。
		 */
		for (int j = array.length - 1; j > 0; j--) {
			// 交换(最小值沉淀)
			temp   = array[j];
			array[j] = array[0];
			array[0] = temp;
			adjustMinHeap(array, 0, j);
		}
	}

	/**
	 * 将一个数组(二叉树)，调整为一个大顶堆
	 * @param array
	 * @param i
	 * @param length
	 */
	public static void adjustMaxHeap(int[] array, int i, int length) {
		int temp = array[i];// 先取出当前元素的值，保存在临时变量
		// 开始调整
		// 说明
		// 1. k=i*2+1, k是i节点的左子节点
		for (int k = i * 2 + 1; k < length; k = k * 2 + 1) {
			if (k + 1 < length && array[k] < array[k + 1]) {// 说明左子节点的值小于右子节点的值
				k++;// k指向右子节点(取大值)
			}

			if (array[k] > temp) {// 如果子节点大于父节点
				array[i] = array[k];
				i = k;// !!! i指向k,继续循环比较
			} else {
				break;// ！
			}
		}

		// 当for循环结束后，我们已经将以i为父节点的树的最大值，放在了最顶(局部)
		array[i] = temp;
	}

	/**
	 * 将一个数组(二叉树)，调整为一个小顶堆
	 * @param array
	 * @param i
	 * @param length
	 */
	public static void adjustMinHeap(int[] array, int i, int length) {
		int temp = array[i];// 先取出当前元素的值，保存在临时变量
		// 开始调整
		// 说明
		// 1. k=i*2+1, k是i节点的左子节点
		for (int k = i * 2 + 1; k < length; k = k * 2 + 1) {
			if (k + 1 < length && array[k] > array[k + 1]) {// 说明左子节点的值大于右子节点的值
				k++;// k指向右子节点(取小值)
			}

			if (array[k] < temp) {// 如果子节点小于父节点
				array[i] = array[k];
				i = k;// !!! i指向k,继续循环比较
			} else {
				break;// ！
			}
		}

		// 当for循环结束后，我们已经将以i为父节点的树的最小值，放在了最顶(局部)
		array[i] = temp;
	}

}
