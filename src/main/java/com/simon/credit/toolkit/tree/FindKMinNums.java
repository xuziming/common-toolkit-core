package com.simon.credit.toolkit.tree;

import com.simon.credit.toolkit.sort.MaxHeap;

import java.util.Arrays;

/**
 * 找出无序数组中最小的k个数（top k问题）
 * @author xuziming 2021-06-08
 */
public class FindKMinNums {

    /**
     * 维护一个有k个数的大顶堆，代表目前选出的最小的k个数
     *
     * @param array 实际场景中，数组里的数据需要从文件中读取
     * @param k
     * @return
     */
    public static int[] getMinKNumsByMaxHeap(int[] array, int k) {
        if (k < 1 || k > array.length) {
            return array;
        }

        int[] kSizeHeap = new int[k];
        // 初始时一次性从文件中读取k个数据
        for (int i = 0; i < k; i++) {
            kSizeHeap[i] = array[i];
        }

        // 建堆，时间复杂度O(k)
        MaxHeap.buildMaxHeap(kSizeHeap);

        // 从文件中逐个读取剩余数据
        for (int i = k; i < array.length; i++) {
            if (array[i] < kSizeHeap[0]) {
                // 将堆顶元素替换为更小的数据
                kSizeHeap[0] = array[i];

                // 从堆顶开始向下进行调整，时间复杂度O(logk)
                MaxHeap.heapify(kSizeHeap, 0, k);
            }
        }

        return kSizeHeap;
    }

    public static void main(String[] args) {
        int[] arr = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        System.out.println(Arrays.toString(getMinKNumsByMaxHeap(arr, 7)));
    }

}