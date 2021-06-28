package com.simon.credit.toolkit;

import com.simon.credit.toolkit.lang.ArrayToolkits;

import java.util.Arrays;

public class ArrayTest {
    private static int RESIZE_STAMP_BITS = 16;
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

    public static void main(String[] args) {
        System.out.println(ArrayToolkits.indexOf(new String[]{"123", "456"}, "456"));
        System.out.println(ArrayToolkits.contains(new String[]{"123", "456"}, "123"));
        System.out.println(ArrayToolkits.notContains(new String[]{"123", "456"}, "456"));

        long[] array = new long[]{123, 456, 222, 101, 911, 996, 700};
        Arrays.sort(array);
        ArrayToolkits.sort(array);
        for (Object i : array) {
            System.out.println(i);
        }
        System.out.println(MAX_RESIZERS);
    }

}