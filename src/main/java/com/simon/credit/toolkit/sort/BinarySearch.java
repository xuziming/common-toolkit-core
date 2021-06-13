package com.simon.credit.toolkit.sort;

/**
 * 二分查找算法
 * 1.二分查找又称折半查找，它是一种效率较高的查找方法。
 * 2.二分查找要求：（1）必须采用顺序存储结构 （2）.必须按关键字大小有序排列
 * 3.原理：将数组分为三部分，依次是中值（所谓的中值就是数组中间位置的那个值）前，中值，中值后；
 *   将要查找的值和数组的中值进行比较，若小于中值则在中值前 面找，若大于中值则在中值后面找，等于中值时直接返回。
 *   然后依次是一个递归过程，将前半部分或者后半部分继续分解为三部分。
 * 4.实现：二分查找的实现用递归和循环两种方式
 * @author xuziming 2021-06-13
 */
public class BinarySearch {

    /**
     * 循环实现二分查找算法
     * @param array 已排好序的数组
     * @param data  需要查找的数
     * @return -1 无法查到数据
     */
    public static int binarySearch(int[] array, int data) {
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int middle = (low + high) / 2;
            if (data == array[middle]) {// 等于中间值，直接返回
                return middle;
            } else if (data < array[middle]) {// 比中间值小，往左找
                high = middle - 1;
            } else if (data > array[middle]) {// 比中间值大，往右找
                low = middle + 1;
            }
        }
        return -1;
    }

    /**
     * 递归实现二分查找
     * @param array 已排好序的数组
     * @param data  需要查找的数
     * @param low   最左边查询下标
     * @param high  最右边查询下标
     * @return -1 无法查到数据
     */
    public static int binarySearch(int[] array, int data, int low, int high) {
        int middle = (low + high) / 2;
        if (data < array[low] || data > array[high] || low > high) {
            return -1;
        }

        if (data == array[middle]) {// 等于中间值，直接返回
            return middle;
        } else if (data < array[middle]) {// 比中间值小，往左找
            return binarySearch(array, data, low, middle - 1);
        } else if (data > array[middle]) {// 比中间值大，往右找
            return binarySearch(array, data, middle + 1, high);
        }

        return -1;
    }

    public static void main(String[] args) {
        int[] array = {6, 12, 33, 87, 90, 97, 108, 561};
        System.out.println("循环查找：" + binarySearch(array, 90));
        System.out.println("递归查找: " + binarySearch(array, 90, 0, array.length - 1));
    }

}