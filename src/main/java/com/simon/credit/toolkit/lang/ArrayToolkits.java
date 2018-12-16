package com.simon.credit.toolkit.lang;

/**
 * 数组工具类
 * @author XUZIMING 2018-08-19
 */
public class ArrayToolkits {

	private static final int INDEX_NOT_FOUND = -1;

	public static <T> boolean notContains(final T[] array, final T target) {
		return !contains(array, target);
	}

	public static <T> boolean contains(final T[] array, final T target) {
		return indexOf(array, target) != INDEX_NOT_FOUND;
	}

	public static <T> int indexOf(final T[] array, final T target) {
		return indexOf(array, target, 0);
	}

	public static <T> int indexOf(final T[] array, final T target, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (target == null) {
			for (int i = startIndex; i < array.length; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else if (array.getClass().getComponentType().isInstance(target)) {
			for (int i = startIndex; i < array.length; i++) {
				if (target.equals(array[i])) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static final boolean contains(final Object target, final Object... objs) {
		if (objs == null || objs.length == 0) {
			return false;
		}

		if (target == null) {
			for (int i = 0; i < objs.length; i++) {
				if (objs[i] == null) {
					return true;
				}
			}
		} else if (objs.getClass().getComponentType().isInstance(target)) {
			for (int i = 0; i < objs.length; i++) {
				if (target.equals(objs[i])) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 获取数组指定位置的值,越界则返回def
	 * @param array
	 * @param index
	 * @param def
	 * @return
	 */
	public static final <T> T get(T[] array, int index, T def) {
		int arrayLength = array == null ? 0 : array.length;
		return get(array, arrayLength, index, def);
	}

	/**
	 * 获取数组指定位置的值,越界则返回def
	 * @param array
	 * @param arrayLength
	 * @param index
	 * @param def
	 * @return
	 */
	public static final <T> T get(T[] array, int arrayLength, int index, T def) {
		if (index >= 0 && index < arrayLength) {
			return array[index];
		}
		return def;
	}

}
