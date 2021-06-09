package com.simon.credit.toolkit.sort;

import java.util.Arrays;

/**
 * 大顶堆
 * @author XUZIMING 2019-11-19
 */
public final class MaxHeap {

	public static void main(String[] args) {
		int[] num = new int[]{3, 7, 2, 1, 21, 20};
		MaxHeap.maxHeapSort(num);
		System.out.println(Arrays.toString(num));
	}

	/**
	 * 大堆排序(升序排序)
	 * @param array 构建大顶堆之后对应的数据存储
	 */
	public static final void maxHeapSort(int[] array) {
		/**
		 * 1、将无序序列构造成一个堆，升序选择大顶堆，降序选择小顶堆
		 */
		buildMaxHeap(array);

		/**
		 * 2、将堆顶元素与末尾元素交换，把最大元素“沉”到数组末端；
		 * 3、重新调整结构，使其满足堆定义，然后继续交换顶堆元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序
		 */
		for (int count = array.length - 1; count > 0; count--) {
			swap(array, 0, count);// 交换(最大值沉淀到数组末端)
			// adjustMaxHeap(array, 0, count);
			heapify(array, 0, count);
		}
	}

	/**
	 * 将无序序列构造成一个堆，升序选择大顶堆，降序选择小顶堆
	 * @param array 待排序数组
	 * @return
	 */
	public static final int[] buildMaxHeap(int[] array) {
		// 大顶堆的构建过程就是从最后一个非叶子结点开始从下往上调整
		for (int i = array.length / 2 - 1; i >= 0; i--) {
			// adjustMaxHeap(array, i, array.length);
			heapify(array, i, array.length);
		}
		return array;
	}

    /**
     * 将一个数组，调整为一个大顶堆
     * 废弃原因：代码稍微有点复杂；使用heapify方法代替
     * @param array        待排序序列
     * @param notLeafIndex 非叶子节点下标，默认从最后一个非叶子结点位置(数组长度/2-1)，然后往前(下标减一)继续寻找下一个非叶子节点
     * @param count       调整长度(整个大顶堆调整过程中，调整长度是逐渐递减的)
     */
    @Deprecated
    public static void adjustMaxHeap(int[] array, int notLeafIndex, int count) {
		int rootIndex = notLeafIndex;
		while (rootIndex < count) {// 或者rootIndex <= count - 1
            int  leftIndex = 2 * rootIndex + 1;
            int rightIndex = 2 * rootIndex + 2;

            int maxIndex = rootIndex;// 最大值对应的下标，默认为根对应的下标

            if (leftIndex < count && array[leftIndex] > array[maxIndex]) {
                maxIndex = leftIndex;
            }
            if (rightIndex < count && array[rightIndex] > array[maxIndex]) {
                maxIndex = rightIndex;
            }

            if (maxIndex == rootIndex) {// 左、右子节点的值没有比根节点更大的，则结束调整
                break;
            }

            // 将当前节点与(左子树或者右子树)进行交换
            swap(array, rootIndex, maxIndex);

            // 交换完后检查(左子树或者右子树)是否满足大顶堆的性质，不满足则再次循环进行子树结构调整
            rootIndex = maxIndex;
        }
    }

	/**
	 * 从array[i]向下进行堆调整
	 *
	 * @param array
	 * @param i
	 * @param heapSize
	 */
	public static void heapify(int[] array, int i, int heapSize) {
		int  leftChild = 2 * i + 1;
		int rightChild = 2 * i + 2;

		int max = i;// 初始将i位置的值标记为最大值
		if (leftChild < heapSize && array[leftChild] > array[max]) {
			max = leftChild;
		}
		if (rightChild < heapSize && array[rightChild] > array[max]) {
			max = rightChild;
		}

		if (max != i) {
			swap(array, i, max);// 交换
			heapify(array, max, heapSize);// 堆结构发生了变化，递归向下进行堆调整
		}
	}

	private static final void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

}