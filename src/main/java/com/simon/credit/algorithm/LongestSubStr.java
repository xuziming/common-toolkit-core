package com.simon.credit.algorithm;

import java.util.HashSet;
import java.util.Set;

/**
 * 最长无重复字符子串长度
 * 示例1：
 * s = 'abcabcbb' 最长的无重复子串为'abc'，长度为3
 * 示例2:
 * s = 'aabbccdd' 最长的无重复子串为'ab' ，长度为2
 * 示例3:
 * s = 'abccbae'  最长无重复子串为'cbae'，长度为4
 * 示例4:
 * s = 'bbbbb'    最长的无重复子串为'b' ，长度为1
 */
public class LongestSubStr {

    public static void main(String[] args) {
        String s = "abcabcbb";
        System.out.println(longestSubStrLen(s));

        String s2 = "aabbccdd";
        System.out.println(longestSubStrLen(s2));

        String s3 = "abccbae";
        System.out.println(longestSubStrLen(s3));

        String s4 = "bbbb";
        System.out.println(longestSubStrLen(s4));
    }

    public static int longestSubStrLen(String str) {
        char[] chars = str.toCharArray();

        Set<Character> set = new HashSet<>();
        int len = 0;
        int maxLen = 0;

        for (char c : chars) {
            if (!set.contains(c)) {
                set.add(c);
                len++;
                maxLen = Math.max(len, maxLen);
            } else {
                set.clear();
                set.add(c);
                len = 1;
            }
        }

        return maxLen;
    }

}