package com.simon.credit.toolkit.sort;

public class BitMapTest {

    private int[] bitArray;

    public BitMapTest(long size) {
        bitArray = new int[(int) (size / 32 + 1)];
    }

    /**
     * 将比特位设置为1
     * <p>
     *  例：将第28个比特位设置为1
     *  只需将1左移(31-28)位数，然后与原来的值进行或运算。
     * </p>
     * @param num
     */
    public void set1(int num) {
        // 确定数组 index
        int arrayIndex = num >> 5;
        // 确定bit index
        int bitIndex = num & 31;
        // 设置0
        bitArray[arrayIndex] |= 1 << bitIndex;
    }

    /**
     * 将比特位设置为0
     * <p>
     *  例：将第28个比特位设置为0
     *  只需将1左移(31-28)位数，并进行非运算，然后与原来的值进行与运算。
     * </p>
     * @param num
     */
    public void set0(int num) {
        // 确定数组 index
        int arrayIndex = num >> 5;
        // 确定bit index
        int bitIndex = num & 31;
        // 设置0
        bitArray[arrayIndex] &= ~(1 << bitIndex);
    }

    /**
     * 判断某一元素是否存在
     * <p>
     *  例：判断28位比特位是否有元素存在
     *  只需将1左移(31-28)位数，然后与原来的值进行与运算。
     *  只要与运算结果中有1，即表示元素存在。所以可以用运行结果是不为0作为元素是否存在依据。
     * </p>
     * @param num
     * @return
     */
    public boolean isExist(int num) {
        //确定数组 index
        int arrayIndex = num >> 5;
        //确定bit index
        int bitIndex = num & 31;

        //判断是否存在
        return (bitArray[arrayIndex] & ((1 << bitIndex))) != 0 ? true : false;
    }

    /**
     * 将整型数字转换为二进制字符串，总共32位，不舍弃前面的0
     * @param number 整型数字
     * @return 二进制字符串
     */
    private static String get32BitBinString(int number) {
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sBuilder.append(number & 1);
            number = number >>> 1;
        }
        return sBuilder.reverse().toString();
    }

    public static void main(String[] args) {
        int[] array = new int[]{1, 2, 35, 22, 56, 334, 245, 2234, 54};

        BitMapTest instance = new BitMapTest(2234 - 1);

        for (int element : array) {
            instance.set1(element);
        }

        System.out.println(instance.isExist(88));
    }

}