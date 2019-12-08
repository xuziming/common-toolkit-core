package com.simon.credit.toolkit.lang.wrapper;

import java.io.Serializable;

public class StringWrapper implements Serializable, Comparable<String>, CharSequence {
	private static final long serialVersionUID = 7084331764952988996L;

	private String delegate;

	public StringWrapper(String delegate) {
		this.delegate = delegate;
	}

	public static int lastIndexOf(char[] source, int sourceOffset, int sourceCount, String target, int fromIndex) {
		return lastIndexOf(source, sourceOffset, sourceCount, target.toCharArray(), 0, target.toCharArray().length, fromIndex);
	}

	static int lastIndexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
		int rightIndex = sourceCount - targetCount;
		if (fromIndex < 0) {
			return -1;
		}
		if (fromIndex > rightIndex) {
			fromIndex = rightIndex;
		}
		/* Empty string always matches. */
		if (targetCount == 0) {
			return fromIndex;
		}

		int strLastIndex = targetOffset + targetCount - 1;
		char strLastChar = target[strLastIndex];
		int min = sourceOffset + targetCount - 1;
		int i = min + fromIndex;

		startSearchForLastChar: while (true) {
			while (i >= min && source[i] != strLastChar) {
				i--;
			}
			if (i < min) {
				return -1;
			}
			int j = i - 1;
			int start = j - (targetCount - 1);
			int k = strLastIndex - 1;

			while (j > start) {
				if (source[j--] != target[k--]) {
					i--;
					continue startSearchForLastChar;
				}
			}
			return start - sourceOffset + 1;
		}
	}

	public void getChars(char dst[], int dstBegin) {
		System.arraycopy(delegate.toCharArray(), 0, dst, dstBegin, delegate.toCharArray().length);
	}

	public static int indexOf(char[] source, int sourceOffset, int sourceCount, String target, int fromIndex) {
		return indexOf(source, sourceOffset, sourceCount, target.toCharArray(), 0, target.toCharArray().length, fromIndex);
	}

	static int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
					;

				if (j == end) {
					/* Found whole string. */
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}

	@Override
	public int length() {
		return delegate.length();
	}

	@Override
	public char charAt(int index) {
		return delegate.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return delegate.subSequence(start, end);
	}

	@Override
	public int compareTo(String anotherString) {
		return delegate.compareTo(anotherString);
	}

}