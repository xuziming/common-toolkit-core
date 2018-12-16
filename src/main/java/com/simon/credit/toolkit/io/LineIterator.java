package com.simon.credit.toolkit.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 文本行迭代器
 * @author XUZIMING 2018-12-16
 */
public class LineIterator implements Iterator<Object> {

	/** 缓冲reader */
	private final BufferedReader bufferedReader;

	/** 缓存当前行 */
	private String cachedLine;

	/** 是否已读完所有行 */
	private boolean hadFinished = false;

	public LineIterator(final Reader reader) throws IllegalArgumentException {
		if (reader == null) {
			throw new IllegalArgumentException("reader不能为null");
		}
		if (reader instanceof BufferedReader) {
			bufferedReader = (BufferedReader) reader;
		} else {
			bufferedReader = new BufferedReader(reader);
		}
	}

	public boolean hasNext() {
		if (cachedLine != null) {
			return true;
		} else if (hadFinished) {
			return false;
		} else {
			try {
				while (true) {
					String line = bufferedReader.readLine();
					if (line == null) {
						hadFinished = true;
						return false;
					} else if (isValidLine(line)) {
						cachedLine = line;
						return true;
					}
				}
			} catch (IOException ioe) {
				close();
				throw new IllegalStateException(ioe.toString());
			}
		}
	}

	protected boolean isValidLine(String line) {
		return true;
	}

	public Object next() {
		return nextLine();
	}

	public String nextLine() {
		if (!hasNext()) {
			throw new NoSuchElementException("没有更多可读行");
		}
		String currentLine = cachedLine;
		cachedLine = null;
		return currentLine;
	}

	public void close() {
		hadFinished = true;
		IOToolkits.close(bufferedReader);
		cachedLine = null;
	}

	public void remove() {
		throw new UnsupportedOperationException("不支持在行迭代器进行删除");
	}

	public static void closeQuietly(LineIterator iterator) {
		if (iterator != null) {
			iterator.close();
		}
	}

}
