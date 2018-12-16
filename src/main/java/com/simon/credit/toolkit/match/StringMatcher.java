package com.simon.credit.toolkit.match;

/**
 * 字符串匹配器
 * @author XUZIMING 2018-07-07
 */
public interface StringMatcher {

	/**
	 * 从原字符串中查找模式字符串的位置,如果模式字符串存在,则返回模式字符串第一次出现的位置,否则返回-1
	 * 
	 * @param str 原字符串
	 * @param subStr 子字符串
	 * @return 若子字符串存在, 则返回子串第一次出现的下标位置; 若通篇不存在, 则返回-1 
	 */
	int indexOf(String str, String subStr);

}