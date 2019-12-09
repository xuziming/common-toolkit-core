package com.simon.credit.toolkit.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;

public final class MyStringBuffer extends MyAbstractStringBuilder implements Serializable, CharSequence {
	static final long serialVersionUID = 3388685877147921107L;

	private transient char[] toStringCache;

	public MyStringBuffer() {
		super(16);// 默认16
	}

	public MyStringBuffer(int capacity) {
		super(capacity);
	}

	public MyStringBuffer(String str) {
		super(str.length() + 16);// 默认str长+16
		append(str);
	}

	public MyStringBuffer(CharSequence charSequence) {
		this(charSequence.length() + 16);
		append(charSequence);
	}

	@Override
	public synchronized int length() {
		return super.length();
	}

	@Override
	public synchronized int capacity() {
		return super.capacity();
	}

	@Override
	public synchronized void ensureCapacity(int minimumCapacity) {
		if (minimumCapacity > value.length) {
			expandCapacity(minimumCapacity);
		}
	}

	@Override
	public synchronized void trimToSize() {
		super.trimToSize();
	}

	@Override
	public synchronized void setLength(int newLength) {
		toStringCache = null;
		super.setLength(newLength);
	}

	@Override
	public synchronized char charAt(int index) {
		if ((index < 0) || (index >= count))
			throw new StringIndexOutOfBoundsException(index);
		return value[index];
	}

	@Override
	public synchronized int codePointAt(int index) {
		return super.codePointAt(index);
	}

	@Override
	public synchronized int codePointBefore(int index) {
		return super.codePointBefore(index);
	}

	@Override
	public synchronized int codePointCount(int beginIndex, int endIndex) {
		return super.codePointCount(beginIndex, endIndex);
	}

	@Override
	public synchronized int offsetByCodePoints(int index, int codePointOffset) {
		return super.offsetByCodePoints(index, codePointOffset);
	}

	@Override
	public synchronized void getChars(int srcBegin, int srcEnd, char[] dstCharArray, int dstBegin) {
		super.getChars(srcBegin, srcEnd, dstCharArray, dstBegin);
	}

	@Override
	public synchronized void setCharAt(int index, char ch) {
		if ((index < 0) || (index >= count)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		toStringCache = null;
		value[index] = ch;
	}

	@Override
	public synchronized MyStringBuffer append(Object obj) {
		toStringCache = null;
		return append(String.valueOf(obj));
	}

	@Override
	public synchronized MyStringBuffer append(String str) {
		toStringCache = null;
		super.append(str);
		return this;
	}

	public synchronized MyStringBuffer append(MyStringBuffer buffer) {
		toStringCache = null;
		super.append(buffer);
		return this;
	}

	@Override
	synchronized MyStringBuffer append(MyAbstractStringBuilder myAbstractStringBuilder) {
		toStringCache = null;
		super.append(myAbstractStringBuilder);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(CharSequence charSequence) {
		toStringCache = null;
		super.append(charSequence);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(CharSequence charSequence, int start, int end) {
		toStringCache = null;
		super.append(charSequence, start, end);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(char[] charArray) {
		toStringCache = null;
		super.append(charArray);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(char[] charArray, int offset, int len) {
		toStringCache = null;
		super.append(charArray, offset, len);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(boolean booleanValue) {
		toStringCache = null;
		super.append(booleanValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(char charValue) {
		toStringCache = null;
		super.append(charValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(int intValue) {
		toStringCache = null;
		super.append(intValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer appendCodePoint(int codePoint) {
		toStringCache = null;
		super.appendCodePoint(codePoint);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(long longValue) {
		toStringCache = null;
		super.append(longValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(float floatValue) {
		toStringCache = null;
		super.append(floatValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer append(double doubleValue) {
		toStringCache = null;
		super.append(doubleValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer delete(int start, int end) {
		toStringCache = null;
		super.delete(start, end);
		return this;
	}

	@Override
	public synchronized MyStringBuffer deleteCharAt(int index) {
		toStringCache = null;
		super.deleteCharAt(index);
		return this;
	}

	@Override
	public synchronized MyStringBuffer replace(int start, int end, String str) {
		toStringCache = null;
		super.replace(start, end, str);
		return this;
	}

	@Override
	public synchronized String substring(int start) {
		return substring(start, count);
	}

	@Override
	public synchronized CharSequence subSequence(int start, int end) {
		return super.substring(start, end);
	}

	@Override
	public synchronized String substring(int start, int end) {
		return super.substring(start, end);
	}

	@Override
	public synchronized MyStringBuffer insert(int index, char[] charArray, int offset, int len) {
		toStringCache = null;
		super.insert(index, charArray, offset, len);
		return this;
	}

	@Override
	public synchronized MyStringBuffer insert(int offset, Object obj) {
		toStringCache = null;
		super.insert(offset, String.valueOf(obj));
		return this;
	}

	@Override
	public synchronized MyStringBuffer insert(int offset, String str) {
		toStringCache = null;
		super.insert(offset, str);
		return this;
	}

	@Override
	public synchronized MyStringBuffer insert(int offset, char[] charArray) {
		toStringCache = null;
		super.insert(offset, charArray);
		return this;
	}

	@Override
	public MyStringBuffer insert(int dstOffset, CharSequence charSequence) {
		super.insert(dstOffset, charSequence);
		return this;
	}

	@Override
	public synchronized MyStringBuffer insert(int dstOffset, CharSequence charSequence, int start, int end) {
		toStringCache = null;
		super.insert(dstOffset, charSequence, start, end);
		return this;
	}

	@Override
	public MyStringBuffer insert(int offset, boolean booleanValue) {
		super.insert(offset, booleanValue);
		return this;
	}

	@Override
	public synchronized MyStringBuffer insert(int offset, char charValue) {
		toStringCache = null;
		super.insert(offset, charValue);
		return this;
	}

	@Override
	public MyStringBuffer insert(int offset, int intValue) {
		super.insert(offset, intValue);
		return this;
	}

	@Override
	public MyStringBuffer insert(int offset, long longValue) {
		super.insert(offset, longValue);
		return this;
	}

	@Override
	public MyStringBuffer insert(int offset, float floatValue) {
		super.insert(offset, floatValue);
		return this;
	}

	@Override
	public MyStringBuffer insert(int offset, double doubleValue) {
		super.insert(offset, doubleValue);
		return this;
	}

	@Override
	public int indexOf(String str) {
		return super.indexOf(str);
	}

	@Override
	public synchronized int indexOf(String str, int fromIndex) {
		return super.indexOf(str, fromIndex);
	}

	@Override
	public int lastIndexOf(String str) {
		return lastIndexOf(str, count);
	}

	@Override
	public synchronized int lastIndexOf(String str, int fromIndex) {
		return super.lastIndexOf(str, fromIndex);
	}

	@Override
	public synchronized MyStringBuffer reverse() {
		toStringCache = null;
		super.reverse();
		return this;
	}

	@Override
	public synchronized String toString() {
		if (toStringCache == null) {
			toStringCache = Arrays.copyOfRange(value, 0, count);
		}
		return new String(toStringCache);
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("value" , char[].class), 
		new ObjectStreamField("count" , Integer.TYPE),
		new ObjectStreamField("shared", Boolean.TYPE), 
	};

	private synchronized void writeObject(ObjectOutputStream oos) throws java.io.IOException {
		ObjectOutputStream.PutField fields = oos.putFields();
		fields.put("value" , value);
		fields.put("count" , count);
		fields.put("shared", false);
		oos.writeFields();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField fields = ois.readFields();
		value = (char[]) fields.get("value", null);
		count = fields.get("count", 0);
	}

}