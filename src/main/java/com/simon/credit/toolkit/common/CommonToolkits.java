package com.simon.credit.toolkit.common;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 通用工具类
 * @author XUZIMING 2016-10-11
 */
public class CommonToolkits {

	public static final String UTF8 = "UTF-8";
	public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static ThreadLocal<Map<String, DateFormat>> threadLocal = new ThreadLocal<Map<String, DateFormat>>();

	/**======================================================================================*/
	/**================================= 字符/数组等非空判断与操作=================================*/
	/**======================================================================================*/

	public static final boolean isEmpty(Object obj) {
		if (obj instanceof String) {
			return obj == null || isEmptyContainNull(obj.toString());
		}

		if (obj instanceof Object[]) {
			return isEmpty((Object[]) obj);
		}

		if (obj instanceof Collection) {
			return isEmpty((Collection<?>) obj);
		}

		if (obj instanceof Map) {
			return isEmpty((Map<?, ?>) obj);
		}

		return obj == null;
	}

	public static final boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	public static final boolean isEmpty(String input) {
		return input == null || input.trim().isEmpty();
	}

	public static final boolean isEmptyContainNull(String input) {
		return input == null || input.trim().isEmpty() || input.trim().equalsIgnoreCase("null");
	}

	public static final boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static final boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	public static final boolean isNotEmpty(Collection<?> c) {
		return !isEmpty(c);
	}

	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static final boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static final String trim(final String input) {
		return input == null ? null : input.trim();
	}

	public static final String trim(String input, String emptyDefault) {
		return isEmpty(input) ? emptyDefault : input.trim();
	}

	public static final String trimToEmpty(String input) {
		return isEmptyContainNull(input) ? "" : input.trim();
	}

	public static final String trimToNull(String input) {
		return isEmptyContainNull(input) ? null : input.trim();
	}

	public static final boolean isNoneEmpty(List<Object> objs) {
		return !isAnyEmpty(objs);
	}

	public static final boolean isAnyEmpty(List<Object> objs) {
		if (isEmpty(objs)) {
			return true;
		}
		return isAnyEmpty(objs.toArray());
	}

	/**
	 * 判断目标对象列表的元素全部不为空值(即: 没有任何一个为空值)
	 * <pre>空值包括: null、空字符串(即: "")、若干空格组成的字符串(如: "  ")</pre>
	 * @param objs
	 * @return
	 */
	public static final boolean isNoneEmpty(Object[] objs) {
		if (objs == null || objs.length == 0) {
			return false;
		}
		for (Object obj : objs) {
			if (isEmpty(obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断目标对象列表的元素是否存在空值
	 * <pre>空值包括: null、空字符串(即: "")、若干空格组成的字符串(如: "  ")</pre>
	 * @param objs
	 * @return
	 */
	public static final boolean isAnyEmpty(Object... objs) {
		if (objs == null || objs.length == 0) {
			return true;
		}
		for (Object obj : objs) {
			if (isEmpty(obj)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断目标对象列表的元素是否全部为空值
	 * <pre>空值包括: null、空字符串(即: "")、若干空格组成的字符串(如: "  ")</pre>
	 * @param objs
	 * @return
	 */
	public static final boolean isAllEmpty(Object... objs) {
		if (objs == null || objs.length == 0) {
			return true;
		}
		for (Object obj : objs) {
			if (!isEmpty(obj)) {
				return false;
			}
		}
		return true;
	}

	public static final boolean endsWith(final CharSequence str, final CharSequence suffix) {
        return endsWith(str, suffix, false);
    }

	public static final boolean endsWithAnyIgnoreCase(CharSequence string, CharSequence... searchStrings) {
		if (string == null || string.length() == 0 || searchStrings == null || searchStrings.length == 0) {
            return false;
        }
        for (final CharSequence searchString : searchStrings) {
            if (endsWithIgnoreCase(string, searchString)) {
                return true;
            }
        }
        return false;
	}

	private static final boolean endsWithIgnoreCase(final CharSequence str, final CharSequence suffix) {
        return endsWith(str, suffix, true);
    }

	private static final boolean endsWith(final CharSequence str, final CharSequence suffix, final boolean ignoreCase) {
        if (str == null || suffix == null) {
            return str == null && suffix == null;
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return regionMatches(str, ignoreCase, strOffset, suffix, 0, suffix.length());
    }

	private static final boolean regionMatches(final CharSequence cs, final boolean ignoreCase, 
		final int thisStart, final CharSequence substring, final int start, final int length) {

		if (cs instanceof String && substring instanceof String) {
			return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
		}

		int index1 = thisStart;
		int index2 = start;
		int tmpLen = length;

		while (tmpLen-- > 0) {
			char c1 = cs.charAt(index1++);
			char c2 = substring.charAt(index2++);

			if (c1 == c2) continue;

			if (!ignoreCase) return false;

			// The same check as in String.regionMatches():
			if (Character.toUpperCase(c1) != Character.toUpperCase(c2) && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
				return false;
			}
		}

		return true;
	}

	/**======================================================================================*/
	/**==================================== 字符相等或不等判断 ===================================*/
	/**======================================================================================*/

	/**
	 * 判断两个字符串是否相同(区别于: ==)
	 * @param input1 字符串1
	 * @param input2 字符串2
	 * @return
	 */
	public static final boolean equals(String input1, String input2) {
		if (input1 == input2) {
			return true;
		}
		if (input1 == null || input2 == null) {
			return false;
		}

		input1 = input1.trim();
		input2 = input2.trim();

		if (input1 instanceof String && input2 instanceof String) {
			return input1.equals(input2);
		}
        return regionMatches(input1, false, 0, input2, 0, Math.max(input1.length(), input2.length()));
	}

	/**
	 * 判断两个字符串是否不同(区别于: !=)
	 * @param input1 字符串1
	 * @param input2 字符串2
	 * @return
	 */
	public static final boolean notEquals(String input1, String input2) {
		return !equals(input1, input2);
	}

	/**
	 * 判断两个字符串是否相同(不区分大小写)
	 * @param input1 字符串1
	 * @param input2 字符串2
	 * @return
	 */
	public static final boolean equalsIgnoreCase(String input1, String input2) {
		if (input1 == null || input2 == null) {
			return input1 == input2;
		}
		if (input1 == input2) {
			return true;
		}

		input1 = input1.trim();
		input2 = input2.trim();

		if (input1.length() != input2.length()) {
			return false;
		}
		return regionMatches(input1, true, 0, input2, 0, input1.length());
	}

	/**
	 * 判断两个字符串是否不同(不区分大小写)
	 * @param input1 字符串1
	 * @param input2 字符串2
	 * @return
	 */
	public static final boolean notEqualsIgnoreCase(String input1, String input2) {
		return !equalsIgnoreCase(input1, input2);
	}

	/**======================================================================================*/
	/**=============================== 布尔字符串true或false相关判断==============================*/
	/**======================================================================================*/

	/**
	 * 判断布尔字符串是否为true
	 * <pre>
	 * CommonUtils.isTrue("true")  = true
	 * CommonUtils.isTrue("false") = false
	 * CommonUtils.isTrue(null)    = false
	 * </pre>
	 * 
	 * @param input 输入值
	 * @return
	 */
	public static final boolean isTrue(String input) {
		Boolean bool = Boolean.valueOf(input == null ? null : input.toLowerCase());
		return Boolean.TRUE.equals(bool);
	}

	/**
	 * 判断布尔字符串是否为false
	 * <pre>
	 * CommonUtils.isFalse("true")  = false
	 * CommonUtils.isFalse("false") = true
	 * CommonUtils.isFalse(null)    = false
	 * </pre>
	 * 
	 * @param input 输入值
	 * @return
	 */
	public static final boolean isFalse(String input) {
		if (isEmptyContainNull(input)) {
			return false;
		}
		Boolean bool = Boolean.valueOf(input == null ? null : input.toLowerCase());
		return Boolean.FALSE.equals(bool);
	}

	public static final boolean isTrue(Boolean bool) {
		return Boolean.TRUE.equals(bool);
	}

	public static final boolean isFalse(Boolean bool) {
		return Boolean.FALSE.equals(bool);
	}

	/**======================================================================================*/
	/**========================================空集处理========================================*/
	/**======================================================================================*/

	public static final <T> List<T> emptyList() {
		return new ArrayList<T>();
	}

	public static final <T> List<T> emptyList(List<T> list) {
		if (list == null) {
			return emptyList();
		} else {
			return list;
		}
	}

	public static final <K, V> Map<K, V> emptyMap() {
		return new HashMap<K, V>();
	}

	public static final <K, V> Map<K, V> emptyMap(Map<K, V> map) {
		if (map == null) {
			return emptyMap();
		} else {
			return map;
		}
	}

	public static final <K, V> Map<K, V> stableMap(int size) {
		return new HashMap<K, V>(size, 1.0f);
	}

	/**======================================================================================*/
	/**======================================基本类型转换  ======================================*/
	/**======================================================================================*/

	public static final short parseShort(Object data) {
		if (data == null) {
			return 0;
		}

		try {
			if (data instanceof Short) {
				return (Short) data;
			} else {
				return Short.valueOf(trim(String.valueOf(data)));
			}
		} catch (Exception e) {
			return 0;
		}
	}

	public static final int parseInt(Object data) {
		return parseInt(data, 0);
	}

	public static final int parseInt(Object data, int def) {
		if (data == null) {
			return def;
		}

		try {
			if (data instanceof Integer) {
				return (Integer) data;
			} else {
				return Integer.valueOf(trim(String.valueOf(data)));
			}
		} catch (Exception e) {
			return def;
		}
	}

	public static final long parseLong(Object data) {
		return parseLong(data, 0);
	}

	public static final long parseLong(Object data, long def) {
		if (data == null) {
			return def;
		}

		try {
			if (data instanceof Long) {
				return (Long) data;
			} else {
				return Long.valueOf(trim(String.valueOf(data)));
			}
		} catch (Exception e) {
			return def;
		}
	}

	public static final double parseDouble(Object data) {
		return parseDouble(data, (double) 0);
	}

	public static final double parseDouble(Object data, double def) {
		if (data != null) {
			try {
				double value = def;
				if (data != null) {
					if (data instanceof BigDecimal) {
						value = ((BigDecimal) data).doubleValue();
					} else if (data instanceof Double) {
						value = ((Double) data).doubleValue();
					} else {
						value = Double.valueOf(trim(String.valueOf(data)));
					}
				}
				return value == 0 ? 0 : roundHalfUp(value, 2);
			} catch (Exception e) {
			}
		}
		return def;
	}

	public static final DateFormat getDateFormat(String pattern) {
		return getDateFormat(pattern, null);
	}

	public static DateFormat getDateFormat(String pattern, Locale locale) {
		if (isEmptyContainNull(pattern)) {
			throw new IllegalArgumentException("date format pattern cann't be empty!");
		}

		Map<String, DateFormat> dateFormatMap = threadLocal.get();
		if (dateFormatMap == null) {
			dateFormatMap = new HashMap<String, DateFormat>();
			threadLocal.set(dateFormatMap);
		}

		DateFormat dateFormat = dateFormatMap.get(pattern);
		if (dateFormat == null) {
			if (locale == null) {
				dateFormat = new SimpleDateFormat(pattern);
			} else {
				dateFormat = new SimpleDateFormat(pattern, locale);
			}
		}

		return dateFormat;
	}

	public static final String formatDate(final Date date) {
		return formatDate(date, DEFAULT_DATE_FORMAT);
	}

	public static final String formatDate(final Date date, final String pattern) {
		if (date == null || isEmptyContainNull(pattern)) {
			return "";
		}

		// 此种实现方式在高并发环境下存在性能问题
		// final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		// return sdf.format(date);

		// 使用Apache commons 里的FastDateFormat，号称既快又线程安全的DateFormat
		// return DateFormatUtils.format(date, pattern);

		// 使用ThreadLocal, 将共享变量变为独享，线程独享肯定比方法独享在并发环境中能减少不少创建对象的开销。若对性能要求比较高，推荐此方法
		return getDateFormat(pattern).format(date);
	}

	private static final Map<Integer, String> DATE_FORMAT_PATTERN_MAP = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 6468294574893504185L;
		{
			/** key:格式化模式字符串长度, value:格式化模式 */
			put(8 , "yyyy-M-d");
			put(10, "yyyy-MM-dd");
			put(19, "yyyy-MM-dd HH:mm:ss");
			put(20, "yyyy-MM-dd HH:mm:ss");
			put(23, "yyyy-MM-dd HH:mm:ss.SSS");
		}
	};

	public static final Date parseDate(String date, String... patterns) {
		if (isEmptyContainNull(date) || isEmpty(patterns)) {
			throw new IllegalArgumentException("date and patterns must not be null");
		}

		for (String pattern : patterns) {
			try {
				return new SimpleDateFormat(pattern).parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		throw new RuntimeException("unable to parse the date: " + date);
	}

	public static final Date parseDate(Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Calendar) {
			return ((Calendar) value).getTime();
		}

		if (value instanceof Date) {
			return (Date) value;
		}

		long longValue = -1;

		if (value instanceof Number) {
			longValue = ((Number) value).longValue();
			return new Date(longValue);
		}

		if (value instanceof String) {
			String strVal = trimToEmpty(String.valueOf(value));

			if (strVal.length() == 0) {
				return null;
			}

			DateFormat dateFormat = null;

			if (strVal.indexOf('-') != -1) {
				String pattern = DATE_FORMAT_PATTERN_MAP.get(strVal.length());
				dateFormat = getDateFormat(pattern);
			} else if (strVal.length() == "yyyyMMddHHmmss".length()) {
				dateFormat = getDateFormat("yyyyMMddHHmmss");
			} else {
				dateFormat = getDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
			}

			try {
				return dateFormat.parse(strVal);
			} catch (ParseException e) {
				// ignore
			}

			// 判断是否整型(Short、Integer、Long)字符串
			if (Pattern.compile("^[+-]?[0-9]+$").matcher(strVal).find()) {
				longValue = Long.parseLong(strVal);
			}
		}

		if (longValue < 0) {
			throw new IllegalArgumentException("can not cast to Date, value : " + value);
		}

		return new Date(longValue);
	}

	public static double timeDiff(Date src, Date dest, TimeUnit timeUnit) {
		long diff = dest.getTime() - src.getTime();
		if (diff < 0) {
			diff = -diff;
		}
		System.out.println(diff);

		switch (timeUnit) {
			case DAYS    : return divide(diff, 86400000.0).doubleValue();
			case HOURS   : return divide(diff, 3600000.0).doubleValue();
			case MINUTES : return divide(diff, 60000.0).doubleValue();
			case SECONDS : return divide(diff, 1000.0).doubleValue();
		}

		return diff;
	}

	private static final BigDecimal divide(long diff, double divisor) {
		return new BigDecimal(String.valueOf(diff)).divide(new BigDecimal(String.valueOf(divisor)), 2, BigDecimal.ROUND_HALF_UP);
	}

	public static final BigDecimal roundHalfUp(BigDecimal value, int scale) {
		if (value == null) {
			return null;
		}
		return value.setScale(scale, BigDecimal.ROUND_HALF_EVEN);
	}

	/**
	 * 对双精度浮点数进行四舍五入
	 * @param value double类型数字(非空)
	 * @param scale 小数点后保留的小数位数
	 * @return
	 */
	public static final double roundHalfUp(double value, int scale) {
		BigDecimal decimal = new BigDecimal(value);
		return roundHalfUp(decimal, scale).doubleValue();
	}

	public static final byte[] toBinary(String content) {
		try {
			return content.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unsupport charset: " + UTF8);
		}
	}

	public static final String toString(byte[] data) {
		return toString(data, UTF8);
	}

	public static final String toString(byte[] data, String characterEncoding) {
		if (data == null || data.length == 0) {
			return null;
		}
		try {
			return new String(data, characterEncoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unsupport charset: " + characterEncoding);
		}
	}

	/**======================================================================================*/
	/**====================================集合/数组/Map操作====================================*/
	/**======================================================================================*/

	public static final int size(List<?> list) {
		return list == null ? 0 : list.size();
	}

	public static final int size(Collection<?> c) {
		return c == null ? 0 : c.size();
	}

	public static final <K, V> void put(Map<K, V> map, K key, V value) {
		if (map != null && key != null && value != null) {
			map.put(key, value);
		}
	}

	public static final <T> T getValue(Map<String, T> dataMap, String key) {
		T value = dataMap.get(key);
		return value == null ? null : (T) value;
	}

	/**======================================================================================*/
	/**======================================反射/实例化 =======================================*/
	/**======================================================================================*/

	/**
	 * 实例化对象,注意该对象必须有无参构造函数
	 * 
	 * @param klass
	 * @return
	 */
	public static final <T> T newInstance(Class<T> klass) {
		try {
			return (T) klass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("instance class[" + klass + "] with ex:", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static final <T> T newInstance(String className) {
		try {
			return (T) newInstance(Class.forName(className));
		} catch (Exception e) {
			throw new IllegalArgumentException("instance class[" + className + "] with ex:", e);
		}
	}

	public static final Class<?> classForName(String className) {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			throw new IllegalArgumentException("classForName(" + className + ")  with ex:", e);
		}
	}

	/**======================================================================================*/
	/**====================================== URL编/解码 ======================================*/
	/**======================================================================================*/

	public static final String urlDecodeUTF8(String input) {
		if (input == null) return null;
		try {
			return URLDecoder.decode(input, UTF8);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static final String urlEncodeUTF8(String input) {
		if (input == null) return null;
		try {
			return URLEncoder.encode(input, UTF8);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**======================================================================================*/
	/**==================================classpath下文件读取 ===================================*/
	/**======================================================================================*/

	public static final InputStream getInputStreamFromClassPath(String filename) {
		if (isEmptyContainNull(filename)) {
			return null;
		}
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
	}

	/**======================================================================================*/
	/**=======================================文件路径解析======================================*/
	/**======================================================================================*/

	/**
	 * 将文件路径转换为Java可识别的路径
	 * @param path 文件路径
	 * @return
	 */
	public static final String castToJavaFilePath(String path) {
		String FILE_SEPERATOR = "/";

		if (path == null) return null;

		// 反斜杠
		path = replace(path, "\\", FILE_SEPERATOR);
		path = replace(path, "\\\\", FILE_SEPERATOR);

		// 斜杠
		path = replace(path, "//", FILE_SEPERATOR);
		path = replace(path, "////", FILE_SEPERATOR);
		path = replace(path, "//////", FILE_SEPERATOR);
		path = replace(path, "////////", FILE_SEPERATOR);

		path = replace(path, "/", FILE_SEPERATOR);
		path = replace(path, "//", FILE_SEPERATOR);
		path = replace(path, "///", FILE_SEPERATOR);
		path = replace(path, "////", FILE_SEPERATOR);

		path = replace(path, "${FILE_SEPERATOR}", FILE_SEPERATOR);

		return path;
	}

	public static final String replace(final String text, final String searchString, final String replacement) {
		return replace(text, searchString, replacement, -1);
	}

	public static final String replace(final String text, final String searchString, final String replacement, int max) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == -1) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	/**
	 * java去除字符串中的空格、回车、换行符、制表符
	 * @param input 输入值
	 * @return
	 */
	public static final String deleteWhitespace(String input) {
		if (isEmpty(input)) {
			return input;
		}
		final int size = input.length();
		final char[] chs = new char[size];
		int count = 0;
		for (int i = 0; i < size; i++) {
			if (!Character.isWhitespace(input.charAt(i))) {
				chs[count++] = input.charAt(i);
			}
		}
		if (count == size) {
			return input;
		}
		return new String(chs, 0, count);
	}

	/**
	 * 递归删除以remove结尾的字符串
	 * @param content 源字符串
	 * @param remove 待删除的尾部字符串
	 * @return
	 */
	public static final String recurseRemoveEnd(String content, String remove) {
		if (endsWith(content, remove)) {
			return recurseRemoveEnd(removeEnd(content, remove), remove);
		}
		return content;
	}

	public static final String removeEnd(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.endsWith(remove)) {
			return str.substring(0, str.length() - remove.length());
		}
		return str;
	}

	public static final String remove(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		return replace(str, remove, "", -1);
	}

	public static final boolean contains(final CharSequence seq, final CharSequence searchSeq) {
		if (seq == null || searchSeq == null) {
			return false;
		}

		return seq.toString().indexOf(searchSeq.toString(), 0) >= 0;
	}

	public static String join(final Iterable<?> iterable, final String separator) {
		if (iterable == null) {
			return null;
		}
		return join(iterable.iterator(), separator);
	}

	public static String join(final Iterator<?> iterator, final String separator) {
		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return null;
		}
		if (!iterator.hasNext()) {
			return "";
		}
		final Object first = iterator.next();
		if (!iterator.hasNext()) {
			final String result = Objects.toString(first);
			return result;
		}

		// two or more elements
		final StringBuilder builder = new StringBuilder(256);
		if (first != null) {
			builder.append(first);
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				builder.append(separator);
			}
			final Object obj = iterator.next();
			if (obj != null) {
				builder.append(obj);
			}
		}

		return builder.toString();
	}

}