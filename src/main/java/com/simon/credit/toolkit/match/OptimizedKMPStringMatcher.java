package com.simon.credit.toolkit.match;

/**
 * 优化的KMP算法(对next数组的获取进行优化)
 */
public class OptimizedKMPStringMatcher extends KMPStringMatcher {

	@Override
	protected int[] getNext(char[] p) {
		// 已知next[j] = k,利用递归的思想求出next[j+1]的值
		// 若已知next[j] = k,如何求出next[j+1]呢?具体算法如下:
		// 1. 若p[j] = p[k], 则next[j+1] = next[k] + 1;
		// 2. 若p[j] != p[k], 则令k=next[k],如果此时p[j]==p[k],则next[j+1]=k+1,
		// 若不相等,则继续递归前缀索引,令 k=next[k],继续判断,直至k=-1(即k=next[0])或者p[j]=p[k]为止
		int pLen = p.length;
		int[] next = new int[pLen];
		int k = -1;
		int j = 0;
		next[0] = -1; // next数组中next[0]为-1
		while (j < pLen - 1) {
			if (k == -1 || p[j] == p[k]) {
				k++;
				j++;
				// 修改next数组求法
				if (p[j] != p[k]) {
					next[j] = k;// KMPStringMatcher中只有这一行
				} else {
					// 不能出现p[j] = p[next[j]],故若出现这种情况则继续递归,如 k = next[k],
					// k = next[[next[k]]
					next[j] = next[k];
				}
			} else {
				k = next[k];
			}
		}
		return next;
	}

}