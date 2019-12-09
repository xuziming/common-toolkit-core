package com.simon.credit.toolkit.lang.wrapper;

import java.io.Serializable;

public class CharacterWrapper implements Serializable, Comparable<Character> {
	private static final long serialVersionUID = 5623841541587062297L;

	public static final char MIN_LOW_SURROGATE = '\uDC00';

	public static final char MIN_HIGH_SURROGATE = '\uD800';

	public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

	public static final char MAX_HIGH_SURROGATE = '\uDBFF';

	public static final char MAX_LOW_SURROGATE = '\uDFFF';

	private final Character delegate;

	public CharacterWrapper(Character delegate) {
        this.delegate = delegate;
    }

	public static void toSurrogates(int codePoint, char[] dst, int index) {
		// We write elements "backwards" to guarantee all-or-nothing
		dst[index + 1] = lowSurrogate(codePoint);
		dst[index] = highSurrogate(codePoint);
	}

	public static char lowSurrogate(int codePoint) {
		return (char) ((codePoint & 0x3ff) + MIN_LOW_SURROGATE);
	}

	public static char highSurrogate(int codePoint) {
		return (char) ((codePoint >>> 10) + (MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
	}

	// throws ArrayIndexOutOfBoundsException if index out of bounds
	public static int codePointAtImpl(char[] charArray, int index, int limit) {
		char ch1 = charArray[index];
		if (isHighSurrogate(ch1) && ++index < limit) {
			char ch2 = charArray[index];
			if (isLowSurrogate(ch2)) {
				return toCodePoint(ch1, ch2);
			}
		}
		return ch1;
	}

	public static boolean isHighSurrogate(char ch) {
		// Help VM constant-fold; MAX_HIGH_SURROGATE + 1 == MIN_LOW_SURROGATE
		return ch >= MIN_HIGH_SURROGATE && ch < (MAX_HIGH_SURROGATE + 1);
	}

	public static boolean isLowSurrogate(char ch) {
		return ch >= MIN_LOW_SURROGATE && ch < (MAX_LOW_SURROGATE + 1);
	}

	public static int toCodePoint(char high, char low) {
		return ((high << 10) + low) + (MIN_SUPPLEMENTARY_CODE_POINT - (MIN_HIGH_SURROGATE << 10) - MIN_LOW_SURROGATE);
	}

	// throws ArrayIndexOutOfBoundsException if index-1 out of bounds
	public static int codePointBeforeImpl(char[] a, int index, int start) {
		char c2 = a[--index];
		if (isLowSurrogate(c2) && index > start) {
			char c1 = a[--index];
			if (isHighSurrogate(c1)) {
				return toCodePoint(c1, c2);
			}
		}
		return c2;
	}

	public static int codePointCountImpl(char[] a, int offset, int count) {
		int endIndex = offset + count;
		int n = count;
		for (int i = offset; i < endIndex;) {
			if (isHighSurrogate(a[i++]) && i < endIndex && isLowSurrogate(a[i])) {
				n--;
				i++;
			}
		}
		return n;
	}

	public static int offsetByCodePointsImpl(char[] a, int start, int count, int index, int codePointOffset) {
		int x = index;
		if (codePointOffset >= 0) {
			int limit = start + count;
			int i;
			for (i = 0; x < limit && i < codePointOffset; i++) {
				if (isHighSurrogate(a[x++]) && x < limit && isLowSurrogate(a[x])) {
					x++;
				}
			}
			if (i < codePointOffset) {
				throw new IndexOutOfBoundsException();
			}
		} else {
			int i;
			for (i = codePointOffset; x > start && i < 0; i++) {
				if (isLowSurrogate(a[--x]) && x > start && isHighSurrogate(a[x - 1])) {
					x--;
				}
			}
			if (i < 0) {
				throw new IndexOutOfBoundsException();
			}
		}
		return x;
	}

	@Override
	public int compareTo(Character anotherCharacter) {
		return compare(delegate.charValue(), anotherCharacter.charValue());
	}

	public static int compare(char x, char y) {
        return x - y;
    }

}