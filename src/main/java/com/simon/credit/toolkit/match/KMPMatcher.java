package com.simon.credit.toolkit.match;

import java.util.Arrays;

/**
 * KMP模式匹配
 */
public class KMPMatcher {

	public static final int indexOf(String srcStr, String subStr) {
		int[] next = kmpNext(subStr);// [0, 0, 0, 0, 1, 2, 0]
		return indexOf(srcStr, subStr, next);
	}

	public static final int indexOf(String srcStr, String subStr, int[] next) {
		// 遍历
		for (int i = 0, j = 0; i < srcStr.length(); i++) {
			// 需要处理srcStr.charAt(i) != subStr.charAt(j)，去调整j的大小
			// KMP算法核心, 可以验证
			while (j > 0 && srcStr.charAt(i) != subStr.charAt(j)) {
				j = next[j - 1];
			}

			if (srcStr.charAt(i) == subStr.charAt(j)) {
				j++;
			}

			if (j == subStr.length()) {
				return i - j + 1;
			}
		}

		return -1;
	}

	private static final int[] kmpNext(String dest) {
		// 创建一个next数组保存部分匹配值
		int[] next = new int[dest.length()];
		next[0] = 0;// 如果字符串长度为1，部分匹配值就是0

		for (int i = 1, j = 0; i < dest.length(); i++) {
			// 当dest.charAt(i) != dest.charAt(j),我们需要从next[j-1]获取新的j
			// 直到我们发现有dest.charAt(i) == dest.charAt(j)成立才退出
			while (j > 0 && dest.charAt(i) != dest.charAt(j)) {
				j = next[j - 1];
			}

			// 当dest.chatAt(i) == dest.charAt(j)满足时，部分匹配值就是+1
			if (dest.charAt(i) == dest.charAt(j)) {
				j++;
			}
			next[i] = j;
		}

		return next;
	}

	public static void main(String[] args) {
		String srcStr = "尚硅谷 尚硅谷你尚硅 尚硅谷你尚硅谷你尚硅你好";
		String subStr = "尚硅谷你尚硅你";
		int[] next = kmpNext(subStr);
		System.out.println("next = " + Arrays.toString(next));
		int index = indexOf(srcStr, subStr, next);
		System.out.println("index = " + index);

		String str1 = "BBC ABCDAB ABCDABCDABDE";
		String str2 = "ABCDABD";
		int[] nextArray = kmpNext(str2);// [0, 0, 0, 0, 1, 2, 0]
		System.out.println("nextArray = " + Arrays.toString(nextArray));
		int idx = indexOf(str1, str2, nextArray);
		System.out.println("idx = " + idx);
	}

}