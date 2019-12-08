package com.simon.credit.toolkit.lang;

import sun.misc.FloatingDecimal;
import java.util.Arrays;

import com.simon.credit.toolkit.lang.wrapper.CharacterWrapper;
import com.simon.credit.toolkit.lang.wrapper.IntegerWrapper;
import com.simon.credit.toolkit.lang.wrapper.LongWrapper;
import com.simon.credit.toolkit.lang.wrapper.StringWrapper;

@SuppressWarnings("restriction")
abstract class MyAbstractStringBuilder implements Appendable, CharSequence {

	/** The value is used for character storage. */
	char[] value;

	/** The count is the number of characters used. */
	int count;

	/** This no-arg constructor is necessary for serialization of subclasses. */
	MyAbstractStringBuilder() {}

	/** Creates an AbstractStringBuilder of the specified capacity. */
	MyAbstractStringBuilder(int capacity) {
		value = new char[capacity];
	}

	@Override
	public int length() {
		return count;
	}

	public int capacity() {
		return value.length;
	}

	public void ensureCapacity(int minimumCapacity) {
		if (minimumCapacity > 0)
			ensureCapacityInternal(minimumCapacity);
	}

	private void ensureCapacityInternal(int minimumCapacity) {
		// overflow-conscious code
		if (minimumCapacity - value.length > 0) {
			expandCapacity(minimumCapacity);
		}
	}

	void expandCapacity(int minimumCapacity) {
		int newCapacity = value.length * 2 + 2;
		if (newCapacity - minimumCapacity < 0) {
			newCapacity = minimumCapacity;
		}
		if (newCapacity < 0) {
			if (minimumCapacity < 0) {// overflow
				throw new OutOfMemoryError();
			}
			newCapacity = Integer.MAX_VALUE;
		}
		value = Arrays.copyOf(value, newCapacity);
	}

	public void trimToSize() {
		if (count < value.length) {
			value = Arrays.copyOf(value, count);
		}
	}

	public void setLength(int newLength) {
		if (newLength < 0) {
			throw new StringIndexOutOfBoundsException(newLength);
		}
		ensureCapacityInternal(newLength);

		if (count < newLength) {
			Arrays.fill(value, count, newLength, '\0');
		}

		count = newLength;
	}

	@Override
	public char charAt(int index) {
		if ((index < 0) || (index >= count)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return value[index];
	}

	public int codePointAt(int index) {
		if ((index < 0) || (index >= count)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return CharacterWrapper.codePointAtImpl(value, index, count);
	}

	public int codePointBefore(int index) {
		int i = index - 1;
		if ((i < 0) || (i >= count)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return CharacterWrapper.codePointBeforeImpl(value, index, 0);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
			throw new IndexOutOfBoundsException();
		}
		return CharacterWrapper.codePointCountImpl(value, beginIndex, endIndex - beginIndex);
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		if (index < 0 || index > count) {
			throw new IndexOutOfBoundsException();
		}
		return CharacterWrapper.offsetByCodePointsImpl(value, 0, count, index, codePointOffset);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		if (srcBegin < 0) {
			throw new StringIndexOutOfBoundsException(srcBegin);
		}
		if ((srcEnd < 0) || (srcEnd > count)) {
			throw new StringIndexOutOfBoundsException(srcEnd);
		}
		if (srcBegin > srcEnd) {
			throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
		}
		System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
	}

	public void setCharAt(int index, char ch) {
		if ((index < 0) || (index >= count)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		value[index] = ch;
	}

	public MyAbstractStringBuilder append(Object obj) {
		return append(String.valueOf(obj));
	}

	public MyAbstractStringBuilder append(String str) {
		if (str == null) {
			return appendNull();
		}
		int len = str.length();
		ensureCapacityInternal(count + len);
		str.getChars(0, len, value, count);
		count += len;
		return this;
	}

	// Documentation in subclasses because of synchro difference
	public MyAbstractStringBuilder append(MyStringBuffer buffer) {
		if (buffer == null) {
			return appendNull();
		}
		int len = buffer.length();
		ensureCapacityInternal(count + len);
		buffer.getChars(0, len, value, count);
		count += len;
		return this;
	}

	MyAbstractStringBuilder append(MyAbstractStringBuilder myAbstractStringBuilder) {
		if (myAbstractStringBuilder == null) {
			return appendNull();
		}
		int len = myAbstractStringBuilder.length();
		ensureCapacityInternal(count + len);
		myAbstractStringBuilder.getChars(0, len, value, count);
		count += len;
		return this;
	}

	// Documentation in subclasses because of synchro difference
	@Override
	public MyAbstractStringBuilder append(CharSequence s) {
		if (s == null) {
			return appendNull();
		}
		if (s instanceof String) {
			return this.append((String) s);
		}
		if (s instanceof MyAbstractStringBuilder) {
			return this.append((MyAbstractStringBuilder) s);
		}

		return this.append(s, 0, s.length());
	}

	private MyAbstractStringBuilder appendNull() {
		int c = count;
		ensureCapacityInternal(c + 4);
		final char[] value = this.value;
		value[c++] = 'n';
		value[c++] = 'u';
		value[c++] = 'l';
		value[c++] = 'l';
		count = c;
		return this;
	}

	@Override
	public MyAbstractStringBuilder append(CharSequence s, int start, int end) {
		if (s == null) {
			s = "null";
		}
		if ((start < 0) || (start > end) || (end > s.length())) {
			throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + s.length());
		}
		int len = end - start;
		ensureCapacityInternal(count + len);
		for (int i = start, j = count; i < end; i++, j++) {
			value[j] = s.charAt(i);
		}
		count += len;
		return this;
	}

	public MyAbstractStringBuilder append(char[] str) {
		int len = str.length;
		ensureCapacityInternal(count + len);
		System.arraycopy(str, 0, value, count, len);
		count += len;
		return this;
	}

	public MyAbstractStringBuilder append(char str[], int offset, int len) {
		if (len > 0) {// let arraycopy report AIOOBE for len < 0
			ensureCapacityInternal(count + len);
		}
		System.arraycopy(str, offset, value, count, len);
		count += len;
		return this;
	}

	public MyAbstractStringBuilder append(boolean b) {
		if (b) {
			ensureCapacityInternal(count + 4);
			value[count++] = 't';
			value[count++] = 'r';
			value[count++] = 'u';
			value[count++] = 'e';
		} else {
			ensureCapacityInternal(count + 5);
			value[count++] = 'f';
			value[count++] = 'a';
			value[count++] = 'l';
			value[count++] = 's';
			value[count++] = 'e';
		}
		return this;
	}

	@Override
	public MyAbstractStringBuilder append(char c) {
		ensureCapacityInternal(count + 1);
		value[count++] = c;
		return this;
	}

	public MyAbstractStringBuilder append(int i) {
		if (i == Integer.MIN_VALUE) {
			append("-2147483648");
			return this;
		}
		int appendedLength = (i < 0) ? IntegerWrapper.stringSize(-i) + 1 : IntegerWrapper.stringSize(i);
		int spaceNeeded = count + appendedLength;
		ensureCapacityInternal(spaceNeeded);
		IntegerWrapper.getChars(i, spaceNeeded, value);
		count = spaceNeeded;
		return this;
	}

	public MyAbstractStringBuilder append(long l) {
		if (l == Long.MIN_VALUE) {
			append("-9223372036854775808");
			return this;
		}
		int appendedLength = (l < 0) ? LongWrapper.stringSize(-l) + 1 : LongWrapper.stringSize(l);
		int spaceNeeded = count + appendedLength;
		ensureCapacityInternal(spaceNeeded);
		LongWrapper.getChars(l, spaceNeeded, value);
		count = spaceNeeded;
		return this;
	}

	public MyAbstractStringBuilder append(float f) {
		FloatingDecimal.appendTo(f, this);
		return this;
	}

	public MyAbstractStringBuilder append(double d) {
		FloatingDecimal.appendTo(d, this);
		return this;
	}

	public MyAbstractStringBuilder delete(int start, int end) {
		if (start < 0) {
			throw new StringIndexOutOfBoundsException(start);
		}
		if (end > count) {
			end = count;
		}
		if (start > end) {
			throw new StringIndexOutOfBoundsException();
		}
		int len = end - start;
		if (len > 0) {
			System.arraycopy(value, start + len, value, start, count - end);
			count -= len;
		}
		return this;
	}

	public MyAbstractStringBuilder appendCodePoint(int codePoint) {
		final int count = this.count;

		if (Character.isBmpCodePoint(codePoint)) {
			ensureCapacityInternal(count + 1);
			value[count] = (char) codePoint;
			this.count = count + 1;
		} else if (Character.isValidCodePoint(codePoint)) {
			ensureCapacityInternal(count + 2);
			CharacterWrapper.toSurrogates(codePoint, value, count);
			this.count = count + 2;
		} else {
			throw new IllegalArgumentException();
		}
		return this;
	}

	public MyAbstractStringBuilder deleteCharAt(int index) {
		if ((index < 0) || (index >= count)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		System.arraycopy(value, index + 1, value, index, count - index - 1);
		count--;
		return this;
	}

	public MyAbstractStringBuilder replace(int start, int end, String str) {
		if (start < 0) {
			throw new StringIndexOutOfBoundsException(start);
		}
		if (start > count) {
			throw new StringIndexOutOfBoundsException("start > length()");
		}
		if (start > end) {
			throw new StringIndexOutOfBoundsException("start > end");
		}

		if (end > count) {
			end = count;
		}
		int len = str.length();
		int newCount = count + len - (end - start);
		ensureCapacityInternal(newCount);

		System.arraycopy(value, end, value, start + len, count - end);
		new StringWrapper(str).getChars(value, start);
		count = newCount;
		return this;
	}

	public String substring(int start) {
		return substring(start, count);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return substring(start, end);
	}

	public String substring(int start, int end) {
		if (start < 0) {
			throw new StringIndexOutOfBoundsException(start);
		}
		if (end > count) {
			throw new StringIndexOutOfBoundsException(end);
		}
		if (start > end) {
			throw new StringIndexOutOfBoundsException(end - start);
		}
		return new String(value, start, end - start);
	}

	public MyAbstractStringBuilder insert(int index, char[] str, int offset, int len) {
		if ((index < 0) || (index > length())) {
			throw new StringIndexOutOfBoundsException(index);
		}
		if ((offset < 0) || (len < 0) || (offset > str.length - len)) {
			throw new StringIndexOutOfBoundsException("offset " + offset + ", len " + len + ", str.length " + str.length);
		}
		ensureCapacityInternal(count + len);
		System.arraycopy(value, index, value, index + len, count - index);
		System.arraycopy(str, offset, value, index, len);
		count += len;
		return this;
	}

	public MyAbstractStringBuilder insert(int offset, Object obj) {
		return insert(offset, String.valueOf(obj));
	}

	public MyAbstractStringBuilder insert(int offset, String str) {
		if ((offset < 0) || (offset > length())) {
			throw new StringIndexOutOfBoundsException(offset);
		}
		if (str == null) {
			str = "null";
		}
		int len = str.length();
		ensureCapacityInternal(count + len);
		System.arraycopy(value, offset, value, offset + len, count - offset);
		new StringWrapper(str).getChars(value, offset);
		count += len;
		return this;
	}

	public MyAbstractStringBuilder insert(int offset, char[] str) {
		if ((offset < 0) || (offset > length())) {
			throw new StringIndexOutOfBoundsException(offset);
		}
		int len = str.length;
		ensureCapacityInternal(count + len);
		System.arraycopy(value, offset, value, offset + len, count - offset);
		System.arraycopy(str, 0, value, offset, len);
		count += len;
		return this;
	}

	public MyAbstractStringBuilder insert(int dstOffset, CharSequence s) {
		if (s == null) {
			s = "null";
		}
		if (s instanceof String) {
			return this.insert(dstOffset, (String) s);
		}
		return this.insert(dstOffset, s, 0, s.length());
	}

	public MyAbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
		if (s == null) {
			s = "null";
		}
		if ((dstOffset < 0) || (dstOffset > this.length())) {
			throw new IndexOutOfBoundsException("dstOffset " + dstOffset);
		}
		if ((start < 0) || (end < 0) || (start > end) || (end > s.length())) {
			throw new IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + s.length());
		}
		int len = end - start;
		ensureCapacityInternal(count + len);
		System.arraycopy(value, dstOffset, value, dstOffset + len, count - dstOffset);
		for (int i = start; i < end; i++) {
			value[dstOffset++] = s.charAt(i);
		}
		count += len;
		return this;
	}

	public MyAbstractStringBuilder insert(int offset, boolean b) {
		return insert(offset, String.valueOf(b));
	}

	public MyAbstractStringBuilder insert(int offset, char c) {
		ensureCapacityInternal(count + 1);
		System.arraycopy(value, offset, value, offset + 1, count - offset);
		value[offset] = c;
		count += 1;
		return this;
	}

	public MyAbstractStringBuilder insert(int offset, int i) {
		return insert(offset, String.valueOf(i));
	}

	public MyAbstractStringBuilder insert(int offset, long l) {
		return insert(offset, String.valueOf(l));
	}

	public MyAbstractStringBuilder insert(int offset, float f) {
		return insert(offset, String.valueOf(f));
	}

	public MyAbstractStringBuilder insert(int offset, double d) {
		return insert(offset, String.valueOf(d));
	}

	public int indexOf(String str) {
		return indexOf(str, 0);
	}

	public int indexOf(String str, int fromIndex) {
		return StringWrapper.indexOf(value, 0, count, str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return lastIndexOf(str, count);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return StringWrapper.lastIndexOf(value, 0, count, str, fromIndex);
	}

	public MyAbstractStringBuilder reverse() {
		boolean hasSurrogates = false;
		int n = count - 1;
		for (int j = (n - 1) >> 1; j >= 0; j--) {
			int k = n - j;
			char cj = value[j];
			char ck = value[k];
			value[j] = ck;
			value[k] = cj;
			if (Character.isSurrogate(cj) || Character.isSurrogate(ck)) {
				hasSurrogates = true;
			}
		}
		if (hasSurrogates) {
			reverseAllValidSurrogatePairs();
		}
		return this;
	}

	/** Outlined helper method for reverse() */
	private void reverseAllValidSurrogatePairs() {
		for (int i = 0; i < count - 1; i++) {
			char c2 = value[i];
			if (Character.isLowSurrogate(c2)) {
				char c1 = value[i + 1];
				if (Character.isHighSurrogate(c1)) {
					value[i++] = c1;
					value[i] = c2;
				}
			}
		}
	}

	@Override
	public abstract String toString();

	final char[] getValue() {
		return value;
	}

}