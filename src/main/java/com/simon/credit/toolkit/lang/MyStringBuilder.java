package com.simon.credit.toolkit.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class MyStringBuilder extends MyAbstractStringBuilder implements Serializable, CharSequence {
	private static final long serialVersionUID = 2980623407515793812L;

	public MyStringBuilder() {
		super(16);
	}

	public MyStringBuilder(int capacity) {
		super(capacity);
	}

	public MyStringBuilder(String str) {
		super(str.length() + 16);
		append(str);
	}

	public MyStringBuilder(CharSequence seq) {
		this(seq.length() + 16);
		append(seq);
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

	public MyStringBuilder append(StringBuffer sb) {
		super.append(sb);
		return this;
	}

	@Override
	public MyStringBuilder append(CharSequence s) {
		super.append(s);
		return this;
	}

	@Override
	public MyStringBuilder append(CharSequence s, int start, int end) {
		super.append(s, start, end);
		return this;
	}

	@Override
	public MyStringBuilder append(char[] str) {
		super.append(str);
		return this;
	}

	@Override
	public MyStringBuilder append(char[] str, int offset, int len) {
		super.append(str, offset, len);
		return this;
	}

	@Override
	public MyStringBuilder append(boolean b) {
		super.append(b);
		return this;
	}

	@Override
	public MyStringBuilder append(char c) {
		super.append(c);
		return this;
	}

	@Override
	public MyStringBuilder append(int i) {
		super.append(i);
		return this;
	}

	@Override
	public MyStringBuilder append(long lng) {
		super.append(lng);
		return this;
	}

	@Override
	public MyStringBuilder append(float f) {
		super.append(f);
		return this;
	}

	@Override
	public MyStringBuilder append(double d) {
		super.append(d);
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
	public MyStringBuilder insert(int index, char[] str, int offset, int len) {
		super.insert(index, str, offset, len);
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
	public MyStringBuilder insert(int dstOffset, CharSequence s) {
		super.insert(dstOffset, s);
		return this;
	}

	@Override
	public MyStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
		super.insert(dstOffset, s, start, end);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, boolean b) {
		super.insert(offset, b);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, char c) {
		super.insert(offset, c);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, int i) {
		super.insert(offset, i);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, long l) {
		super.insert(offset, l);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, float f) {
		super.insert(offset, f);
		return this;
	}

	@Override
	public MyStringBuilder insert(int offset, double d) {
		super.insert(offset, d);
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

	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeInt(count);
		s.writeObject(value);
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		count = s.readInt();
		value = (char[]) s.readObject();
	}

}