package com.simon.credit.toolkit.match;

/**
 * 暴力匹配
 * <p>
 * 时间复杂度: O(m*n), m = pattern.length, n = source.length
 */
public class ViolentMatcher {

	public static final int indexOf(String srcStr, String subStr) {
		char[] srcChs = srcStr.toCharArray();
		char[] subChs = subStr.toCharArray();

		int srcStrLen = srcChs.length;
		int subStrLen = subChs.length;

		int i = 0;// i索引指向srcChs
		int j = 0;// j索引指向subChs

		while (i < srcStrLen && j < subStrLen) {
			if (srcChs[i] == subChs[j]) {
				// 如果当前字符匹配成功,则将两者各自增1,继续比较后面的字符
				i++;
				j++;
			} else {
				// 如果当前字符匹配不成功,则i回溯到此次匹配最开始的位置+1处,也就是i = i - j + 1
				// (因为i,j是同步增长的), j = 0;
				i = i - j + 1;
				j = 0;
			}
		}

		// 匹配成功,则返回模式字符串在原字符串中首次出现的位置;否则返回-1
		if (j == subStrLen) {
			return i - j;
		} else {
			return -1;
		}
	}

	public static void main(String[] args) {
		String srcStr = "尚硅谷 尚硅谷你尚硅 尚硅谷你尚硅谷你尚硅你好";
		String subStr = "尚硅谷你尚硅你";
		int index = indexOf(srcStr, subStr);
		System.out.println("index = " + index);
	}

}