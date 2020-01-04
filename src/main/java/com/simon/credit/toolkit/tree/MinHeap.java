package com.simon.credit.toolkit.tree;

/**
 * 最小堆
 * <pre>
 * see: https://blog.csdn.net/bbewx/article/details/24127779
 * 【二叉堆的定义】
 * 二叉堆是完全二叉树或者是近似完全二叉树。
 * 二叉堆满足二个特性：
 * 1．父结点的键值总是大于或等于（小于或等于）任何一个子节点的键值。
 * 2．每个结点的左子树和右子树都是一个二叉堆（都是最大堆或最小堆）。
 * 当父结点的键值总是大于或等于任何一个子节点的键值时为最大堆。当父结点的键值总是小于或等于任何一个子节点的键值时为最小堆。
 * **由于其它几种堆（二项式堆，斐波纳契堆等）用的较少，一般将二叉堆就简称为堆。
 * 
 * 【堆的存储】
 * 一般都用数组来表示堆，i结点的父结点下标就为(i – 1) / 2。它的左右子结点下标分别为2 * i + 1和2 * i + 2。如第0个结点左右子结点下标分别为1和2。
 * </pre>
 * @author XUZIMING 2019-11-19
 */
public class MinHeap implements HeapOpt {

	public static void main(String[] args) {
		int[] array = { 10, 40, 30 };

		HeapOpt heapOpt = new MinHeap();
		int[] arrayForAdd = heapOpt.add(array, array.length, 15);
		print(arrayForAdd);

		int[] arrayForDelete = heapOpt.delete(arrayForAdd, arrayForAdd.length);
		print(arrayForDelete);
	}

	/**
	 * 打印数组元素
	 * @param array
	 */
	private static void print(int[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}

	@Override
	public int[] add(int[] array, int length, int num) {
		int[] newArray = new int[length + 1];
		for (int i = 0; i < length; i++) {
			newArray[i] = array[i];
		}
		newArray[length] = num;
		fixupMinHeap(newArray, length + 1);
		return newArray;
	}

	/**
	 * 修正最小堆
	 * @param array
	 * @param length
	 */
	private void fixupMinHeap(int[] array, int length) {
		int i, j, temp;
		temp = array[length - 1];
		i = length - 1;
		j = (i - 1) / 2;
		while (j >= 0 && i != 0) {
			if (array[j] < array[i]) {
				break;
			}
			array[i] = array[j];
			i = j;
			j = (i - 1) / 2;
		}
		array[i] = temp;
	}

	/**
	 * 对fixupMinHeap进行简化
	 */
	@SuppressWarnings("unused")
	private void fixupMinHeapSimplify(int[] array, int i) {
		int k = i - 1;
		for (int j = (k - 1) / 2; j >= 0 && k != 0 && array[j] > array[k]; k = j, j = (k - 1) / 2) {
			swap(array, k, j);
		}
	}

	/**
	 * 将数组array的下标为k和j的两个元素进行交换
	 * @param array
	 * @param i
	 * @param j
	 */
	private void swap(int[] array, int i, int j) {
		array[i] = array[i] + array[j];
		array[j] = array[i] - array[j];
		array[i] = array[i] - array[j];
	}

	/**
	 * 最小堆删除元素
	 * <pre>删除总是发生在根</pre>
	 * @param array
	 * @param length
	 * @return
	 */
	@Override
	public int[] delete(int[] array, int length) {
		swap(array, 0, length - 1);
		return fixdownMixHeap(array, 0, length - 1);
	}

	/**
	 * 把以i为根节点的二叉堆调整成小顶堆
	 * @param array
	 * @param i
	 * @param n
	 * @return
	 */
	private int[] fixdownMixHeap(int[] array, int i, int n) {
		int temp = array[i];
		int j = 2 * i + 1;
		int[] newArray = new int[n];
		while (j < n) {
			if (j + 1 < n && array[j] > array[j + 1]) {
				j++;
			}
			if (array[j] >= temp) {
				break;
			}
			array[i] = array[j];
			i = j;
			j = 2 * i + 1;
		}
		array[i] = temp;
		for (j = 0; j < n; j++) {
			newArray[j] = array[j];
		}
		return newArray;
	}

}