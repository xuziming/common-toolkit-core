package com.simon.credit.toolkit;

import java.util.Map;
import java.util.TreeMap;

/**
 * 求数组中的最长连续序列，见《程序员代码面试指南》P248
 */
public class ArrayLongestConsecutiveTest {

    public static void main(String[] args) {
        int[] arr = {100, 4, 200, 1, 3, 2};
        int longestConsecutive = longestConsecutive(arr);
        System.out.println(longestConsecutive);
    }

    /**
     * 求数组中的最长连续序列
     * @param array
     * @return
     */
    public static final int longestConsecutive(int[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }

        int max = 1;// 有元素的情况下，最小长度为1
        Map<Integer, Integer> map = new TreeMap<>();

        for (int i = 0; i < array.length; i++) {
            System.out.println("\n\n=== loop: " + array[i] + ", " + map);
            if (map.containsKey(array[i])) {
                continue;// 重复元素不处理
            }

            // 1、未记录的元素，第一次进行记录时长度为1
            map.put(array[i], 1);

            // 2、判断序列前一个数:array[i] - 1
            if (map.containsKey(array[i] - 1)) {
                max = Math.max(max, merge(map, array[i] - 1, array[i]));
            }
            // 3、判断序列后一个数:array[i] + 1
            if (map.containsKey(array[i] + 1)) {
                max = Math.max(max, merge(map, array[i], array[i] + 1));
            }
        }

        return max;
    }

    private static int merge(Map<Integer, Integer> map, int less, int more) {
        System.out.println("=== less: " + less + ", more: " + more);
        int left   = less - map.get(less) + 1;// 获取序列最左边界的数值
        int right  = more + map.get(more) - 1;// 获取序列最右边界的数值
        int length = right - left + 1;// 重新计算连续序列左右边界的长度

        map.put(left , length);// 记录左边界与长度
        map.put(right, length);// 记录右边界与长度

        System.out.println(map);
        return length;
    }

}