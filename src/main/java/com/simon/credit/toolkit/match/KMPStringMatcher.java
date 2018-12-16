package com.simon.credit.toolkit.match;

/**
 * KMP模式匹配
 */
public class KMPStringMatcher implements StringMatcher {

	/**
	 * 获取KMP算法中pattern字符串对应的next数组
	 * @param p 模式字符串对应的字符数组
	 * @return
	 */
	protected int[] getNext(char[] p) {
		// 已知next[j] = k,利用递归的思想求出next[j+1]的值
		// 若已知next[j] = k,如何求出next[j+1]呢?具体算法如下:
		// 1. 若p[j] = p[k], 则next[j+1] = next[k] + 1;
		// 2. 若p[j] != p[k], 则令k=next[k],若此时p[j]==p[k],则next[j+1]=k+1,
		// 若不相等,则继续递归前缀索引,令 k=next[k],继续判断,直至k=-1(即k=next[0])或者p[j]=p[k]为止
		int pLen = p.length;
		int[] next = new int[pLen];
		int k = -1;
		int j = 0;
		next[0] = -1;// next数组中next[0]为-1
		while (j < pLen - 1) {
			if (k == -1 || p[j] == p[k]) {
				k++;
				j++;
				next[j] = k;
			} else {
				k = next[k];
			}
		}
		return next;
	}

	@Override
	public int indexOf(String str, String subStr) {
		int i = 0, j = 0;
		char[] src = str.toCharArray();
		char[] ptn = subStr.toCharArray();
		int sLen = src.length;
		int pLen = ptn.length;
		int[] next = getNext(ptn);
		while (i < sLen && j < pLen) {
			// 若j = -1,或者当前字符匹配成功(src[i] = ptn[j]),都让i++,j++
			if (j == -1 || src[i] == ptn[j]) {
				i++;
				j++;
			} else {
				// 若j!=-1且当前字符匹配失败,则令i不变,j=next[j],即让pattern模式串右移j-next[j]个单位
				j = next[j];
			}
		}
		if (j == pLen)
			return i - j;
		return -1;
	}

}