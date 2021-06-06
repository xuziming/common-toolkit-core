package com.simon.credit.toolkit.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java使用bitmap求两个数组的交集
 * 一般来说int代表一个数字，但是如果利用每一个位 ，则可以表示32个数字 ，在数据量极大的情况下可以显著的减轻内存的负担。
 * 我们就以int为例构造一个bitmap，并使用其来解决一个简单的问题：求两个数组的交集。
 */
public class BitMap {

    /**
     * 1        , 2        , 4         , 8          ,
     * 16       , 32       , 64        , 128        ,
     * 256      , 512      , 1024      , 2048       ,
     * 4096     , 8192     , 16384     , 32768      ,
     * 65536    , 131072   , 262144    , 524288     ,
     * 1048576  , 2097152  , 4194304   , 8388608    ,
     * 16777216 , 33554432 , 67108864  , 134217728  ,
     * 268435456, 536870912, 1073741824, -2147483648
     */
    private int[] sign = {
            0x00000001, 0x00000002, 0x00000004, 0x00000008,
            0x00000010, 0x00000020, 0x00000040, 0x00000080,
            0x00000100, 0x00000200, 0x00000400, 0x00000800,
            0x00001000, 0x00002000, 0x00004000, 0x00008000,
            0x00010000, 0x00020000, 0x00040000, 0x00080000,
            0x00100000, 0x00200000, 0x00400000, 0x00800000,
            0x01000000, 0x02000000, 0x04000000, 0x08000000,
            0x10000000, 0x20000000, 0x40000000, 0x80000000
    };

    private int[] arr;

    private int capacity;

    public BitMap(int capacity) {
        validate(capacity);
        this.capacity = capacity;
        this.arr = new int[(capacity >> 5) + 1];
    }

    public void put(int k) {
        if (k > capacity) {
            throw new RuntimeException("k is greater than capacity");
        }
        validate(k);
        int index = k >> 5;// 当前数字应该存放的bucket索引
        arr[index] = arr[index] | sign[k & 31];
    }

    private void validate(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException(" capacity must be greater than zero");
        }
    }

    public int[] getMixed(BitMap bitMap) {
        int length = Math.min(bitMap.arr.length, this.arr.length);
        int[] other = new int[length];
        int[] me = new int[length];

        System.arraycopy(bitMap.arr, 0, other, 0, length);
        System.arraycopy(this.arr, 0, me, 0, length);

        // 借用集合的无固定大小来构建最后数组
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            int k = other[i] & me[i];
            for (int j = 1; j <= 32; j++) {
                if (((k >> j) & 1) == 1) {
                    result.add((i << 5) + j);
                }
            }
        }

        if (result.size() == 0) {
            return null;
        } else {
            int[] rs = new int[result.size()];
            for (int i = 0; i < result.size(); i++) {
                rs[i] = result.get(i);
            }
            return rs;
        }
    }

    // 写一个main方法试验下
    public static void main(String[] args) {
        BitMap bitMap = new BitMap(1000) {
            {
                put(248);
                put(5);
                put(9);
                put(12);
                put(6);
                put(13);
                put(963);
            }
        };
        BitMap bitMap1 = new BitMap(1000) {
            {
                put(248);
                put(15);
                put(13);
                put(963);
                put(5);
                put(6);
                put(9);
            }
        };
        int[] mixed = bitMap.getMixed(bitMap1);
        // 得到有序结果
        // [5, 6, 9, 13, 248, 963]
        System.out.println(Arrays.toString(mixed));
    }

}