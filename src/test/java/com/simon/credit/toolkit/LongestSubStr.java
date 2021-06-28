package com.simon.credit.toolkit;

import java.util.HashSet;
import java.util.Set;

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