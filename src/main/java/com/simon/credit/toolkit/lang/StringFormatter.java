package com.simon.credit.toolkit.lang;

/**
 * 替换Java字符串中${}或{}等占位符的解析工具类
 * @author XUZIMING 2017-11-01
 */
public class StringFormatter {

	/**
	 * 参照SLF日志格式拼接字符串
	 * @param str 待替换字符串
	 * @param args 占位符对应的参数(或参数数组), 参数位置与占位符一一对应
	 * @return
	 */
	public static final String format(String str, Object... args) {
		return parse("{", "}", str, args);
	}

	/**
	 * 参照XML占位符形式拼接字符串
	 * @param str 待替换字符串
	 * @param args 占位符对应的参数(或参数数组), 参数位置与占位符一一对应
	 * @return
	 */
	private static final String format$(String str, Object... args) {
		return parse("${", "}", str, args);
	}

	/**
	 * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
	 * @param openToken 占位符开始标记
	 * @param closeToken 占位符结束标记
	 * @param str 待替换的字符串
	 * @param args 占位符对应的参数(或参数数组)
	 * @return
	 */
	private static final String parse(String openToken, String closeToken, String str, Object... args) {
		if (args == null || args.length <= 0) {
			return str;
		}

		if (str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("null")) {
			return "";
		}

		char[] src = str.toCharArray();
		int argsIndex = 0;
		int offset = 0;

		// search open token
		int start = str.indexOf(openToken, offset);
		if (start == -1) {
			return str;
		}

		final StringBuilder builder = new StringBuilder();
		StringBuilder expression = null;
		while (start > -1) {
			if (start > 0 && src[start - 1] == '\\') {
				// this open token is escaped. remove the backslash and continue.
				builder.append(src, offset, start - offset - 1).append(openToken);
				offset = start + openToken.length();
			} else {
				// found open token. let's search close token.
				if (expression == null) {
					expression = new StringBuilder();
				} else {
					expression.setLength(0);
				}
				builder.append(src, offset, start - offset);
				offset = start + openToken.length();
				int end = str.indexOf(closeToken, offset);
				while (end > -1) {
					if (end > offset && src[end - 1] == '\\') {
						// this close token is escaped. remove the backslash and continue.
						expression.append(src, offset, end - offset - 1).append(closeToken);
						offset = end + closeToken.length();
						end = str.indexOf(closeToken, offset);
					} else {
						expression.append(src, offset, end - offset);
						offset = end + closeToken.length();
						break;
					}
				}
				if (end == -1) {
					// close token was not found.
					builder.append(src, start, src.length - start);
					offset = src.length;
				} else {
					String value = "";
					if (argsIndex <= args.length - 1) {
						if (args[argsIndex] != null) {
							value = args[argsIndex].toString();
						}
					} else {
						value = expression.toString();
					}

					builder.append(value);
					offset = end + closeToken.length();
					argsIndex++;
				}
			}
			start = str.indexOf(openToken, offset);
		}

		if (offset < src.length) {
			builder.append(src, offset, src.length - offset);
		}

		return builder.toString();
	}

	public static void main(String... args) {
		// {}被转义, 不会被替换
		System.out.println(parse("{", "}", "我名:\\{}, 结果:{}, 可信度:{}%", true, 100));

		System.out.println(format$("我名:${name}, 结果:${result}, 可信度:${}%", "雷锋", true, 100));

		System.out.println(format("我名:{ }, 结果:{ }, 可信度:{}%", "雷锋", false, 0));

		/* ~~~~~~~~~ 输出结果如下：~~~~~~~~~ */
		// 我名:{},  结果: true, 可信度:%100
		// 我名:雷锋, 结果: true, 可信度:%100
		// 我名:雷锋, 结果: true, 可信度:%100
	}

}
