package com.simon.credit.toolkit.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class MyStringBuilder extends MyAbstractStringBuilder implements Serializable, CharSequence {
	private static final long serialVersionUID = 2980623407515793812L;

	public MyStringBuilder() {
		super(16);// 默认16
	}

	public MyStringBuilder(int capacity) {
		super(capacity);
	}

	public MyStringBuilder(String str) {
		super(str.length() + 16);// 默认str长+16
		append(str);
	}

	public MyStringBuilder(CharSequence charSequence) {
		this(charSequence.length() + 16);
		append(charSequence);
	}

	@Override
	public MyStringBuilder append(Object obj) {
		return append(String.valueOf(obj));
	}

	@Override
	public MyStringBuilder append(String str) {
		super.append(str);
		return this;
	}

	public MyStringBuilder append(StringBuffer buffer) {
		super.append(buffer);
		return this;
	}

	@Override
	public MyStringBuilder append(CharSequence charSequence) {
		super.append(charSequence);
		return this;
	}

	@Override
	public MyStringBuilder append(CharSequence charSequence, int start, int end) {
		super.append(charSequence, start, end);
		return this;
	}

	@Override
	public MyStringBuilder append(char[] str) {
		super.append(str);
		return this;
	}

	@Override
	public MyStringBuilder append(char[] charArray, int offset, int len) {
		super.append(charArray, offset, len);
		return this;
	}

	@Override
	public MyStringBuilder append(boolean booleanValue) {
		super.append(booleanValue);
		return this;
	}

	@Override
	public MyStringBuilder append(char charValue) {
		super.append(charValue);
		return this;
	}

	@Override
	public MyStringBuilder append(int intValue) {
		super.append(intValue);
		return this;
	}

	@Override
	public MyStringBuilder append(long longValue) {
		super.append(longValue);
		return this;
	}

	@Override
	public MyStringBuilder append(float floatValue) {
		super.append(floatValue);
		return this;
	}

	@Override
	public MyStringBuilder append(double doubleValue) {
		super.append(doubleValue);
		return this;
	}

	@Override
	public MyStringBuilder appendCodePoint(int codePoint) {
		super.appendCodePoint(codePoint);
		return this;
	}

	@Override
	public MyStringBuilder delete(int start, int end) {
		super.delete(start, end);
		return this;
	}

	@Override
	public MyStringBuilder deleteCharAt(int index) {
		super.deleteCharAt(index);
		return this;
	}

	@Override
	public MyStringBuilder replace(int start, int end, String str) {
		super.replace(start, end, str);
		return this;
	}

	@Override
	public MyStringBuilder insert(int index, char[] charArray, int offset, int len) {
		super.insert(index, charArray, offset, len);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, Object obj) {
		super.insert(offset, obj);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, String str) {
		super.insert(offset, str);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, char[] str) {
		super.insert(offset, str);
		return this;
	}

	@Override
	public MyStringBuilder insert(int dstOffset, CharSequence charSequence) {
		super.insert(dstOffset, charSequence);
		return this;
	}

	@Override
	public MyStringBuilder insert(int dstOffset, CharSequence charSequence, int start, int end) {
		super.insert(dstOffset, charSequence, start, end);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, boolean booleanValue) {
		super.insert(offset, booleanValue);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, char charValue) {
		super.insert(offset, charValue);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, int intValue) {
		super.insert(offset, intValue);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, long longValue) {
		super.insert(offset, longValue);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, float floatValue) {
		super.insert(offset, floatValue);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, double doubleValue) {
		super.insert(offset, doubleValue);
		return this;
	}

	@Override
	public int indexOf(String str) {
		return super.indexOf(str);
	}

	@Override
	public int indexOf(String str, int fromIndex) {
		return super.indexOf(str, fromIndex);
	}

	@Override
	public int lastIndexOf(String str) {
		return super.lastIndexOf(str);
	}

	@Override
	public int lastIndexOf(String str, int fromIndex) {
		return super.lastIndexOf(str, fromIndex);
	}

	@Override
	public MyStringBuilder reverse() {
		super.reverse();
		return this;
	}

	@Override
	public String toString() {
		// Create a copy, don't share the array
		return new String(value, 0, count);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeInt(count);
		oos.writeObject(value);
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		count = ois.readInt();
		value = (char[]) ois.readObject();
	}

}