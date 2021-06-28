package com.simon.credit.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 给定一个整数数组和一个目标整数，该目标整数满足数组中两元素之和，返回数组中两个数字的下标索引，以数组形式表示结果。
 * 例如, 给定数组: int[] nums = [2, 7, 11, 15], 目标整数int target = 9,
 * 因为 nums[0] + nums[1] = 2 + 7 = 9,返回包含两数索引的数组[0, 1]。
 */
public class TwoSum {

    public static void main(String[] args) {
        //            0  1  2   3   4  5  6  7
        int[] nums = {2, 7, 11, 15, 3, 3, 1, 4};
        List<List<Integer>> results = fourSum(nums, 21);
        for (List<Integer> result : results) {
            for (Integer index : result) {
                System.out.print(index + "\t");
            }
            System.out.println();
        }
    }

    public static List<List<Integer>> twoSum(int[] nums, int target) {
        return twoSum(nums, 0, target);
    }

    public static List<List<Integer>> twoSum(int[] nums, int startIndex, int target) {
        List<List<Integer>> tuples = new ArrayList<>();

        if (nums.length < 2) {
            return tuples;
        }

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = startIndex; i < nums.length; i++) {
            int num = target - nums[i];
            if (map.containsKey(num)) {
                List<Integer> tuple = tuple(map.get(num), i);
                tuples.add(tuple);
            }
            map.put(nums[i], i);
        }

        return tuples;
    }

    private static final List<Integer> tuple(int... nums) {
        List<Integer> tuple = new ArrayList<>(4);
        tuple.add(nums[0]);
        tuple.add(nums[1]);
        return tuple;
    }

    /**
     * 计算数组nums中所有和为target的三元组
     *
     * @param nums
     * @param target
     * @return
     */
    public static List<List<Integer>> threeSum(int[] nums, int target) {
        return threeSum(nums, 0, target);
    }

    /**
     * 计算数组nums中所有和为target的三元组
     *
     * @param nums
     * @param target
     * @return
     */
    public static List<List<Integer>> threeSum(int[] nums, int startIndex, int target) {
        List<List<Integer>> triples = new ArrayList<>();

        // 穷举threeSum的第一个数
        for (int i = startIndex; i < nums.length; i++) {
            // twoSum = target - nums[i]
            List<List<Integer>> tuples = twoSum(nums, i + 1, target - nums[i]);

            // 遍历满足条件的二元组，再加上nums[i]构成目标三元组
            for (List<Integer> tuple : tuples) {
                tuple.add(i);// 加入第三个数的下标
                triples.add(tuple);
            }

            // 跳过第一个数字重复的情况，否则会出现重复结果
            while (i < nums.length - 1 && nums[i] == nums[i + 1]) {
                i++;
            }
        }

        return triples;
    }

    /**
     * 计算数组nums中所有和为target的四元组
     *
     * @param nums
     * @param target
     * @return
     */
    public static List<List<Integer>> fourSum(int[] nums, int target) {
        List<List<Integer>> results = new ArrayList<>();

        // 穷举threeSum的第一个数
        for (int i = 0; i < nums.length; i++) {
            // twoSum = target - nums[i]
            List<List<Integer>> triples = threeSum(nums, i + 1, target - nums[i]);

            // 遍历满足条件的三元组，再加上nums[i]构成目标四元组
            for (List<Integer> triple : triples) {
                triple.add(i);// 加入第三个数的下标
                results.add(triple);
            }

            // 跳过第一个数字重复的情况，否则会出现重复结果
            while (i < nums.length - 1 && nums[i] == nums[i + 1]) {
                i++;
            }
        }

        return results;
    }

}