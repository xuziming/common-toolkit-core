package com.simon.credit.toolkit.lang;

import java.util.Comparator;

import com.simon.credit.toolkit.sort.JDKComparableTimSort;
import com.simon.credit.toolkit.sort.JDKMergeSort;
import com.simon.credit.toolkit.sort.JDKQuicksort;
import com.simon.credit.toolkit.sort.JDKTimSort;

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
	
	/**
     * Old merge sort implementation can be selected (for
     * compatibility with broken comparators) using a system property.
     * Cannot be a static boolean in the enclosing class due to
     * circular dependencies. To be removed in a future release.
     */
	static final class LegacyMergeSort {
		private static final boolean userRequested = java.security.AccessController.doPrivileged(
			new sun.security.action.GetBooleanAction("java.util.Arrays.useLegacyMergeSort")).booleanValue();
	}

	/**
	 * 自然排序
	 */
	static final class NaturalOrder implements Comparator<Object> {
		@SuppressWarnings("unchecked")
		public int compare(Object first, Object second) {
			return ((Comparable<Object>) first).compareTo(second);
		}

		static final NaturalOrder INSTANCE = new NaturalOrder();
	}

	/**
	 * 数组排序
	 * @param array
	 */
	public static void sort(Object[] array) {
		if (LegacyMergeSort.userRequested) {// false
			JDKMergeSort.legacyMergeSort(array);
		} else {
			JDKComparableTimSort.sort(array, 0, array.length, null, 0, 0);
		}
	}

	/**
	 * 数组排序
	 * @param array 待排序数组
	 * @param comparator 比较器
	 */
	public static <T> void sort(T[] array, Comparator<? super T> comparator) {
		if (comparator == null) {
			comparator = NaturalOrder.INSTANCE;
		}
		if (LegacyMergeSort.userRequested) {
			JDKMergeSort.legacyMergeSort(array, comparator);
		} else {
			JDKTimSort.sort(array, 0, array.length, comparator, null, 0, 0);
		}
	}

	/**
	 * 整型数组排序
	 * @param array 待排序数组
	 */
	public static void sort(int[] array) {
		JDKQuicksort.sort(array, 0, array.length - 1, null, 0, 0);
	}

	/**
	 * 长整型数组排序
	 * @param array 待排序的长整型数组
	 */
	public static void sort(long[] array) {
		JDKQuicksort.sort(array, 0, array.length - 1, null, 0, 0);
	}

}
