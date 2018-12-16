package com.simon.credit.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常处理工具
 * @author XUZIMING 2018-12-16
 */
public class ExceptionToolkits {

	public static final IllegalStateException illegalStateException(Throwable t) {
		return new IllegalStateException(t);
	}

	public static final IllegalStateException illegalStateException(String message) {
		return new IllegalStateException(message);
	}

	public static final IllegalStateException illegalStateException(String message, Throwable t) {
		return new IllegalStateException(message, t);
	}

	public static final IllegalArgumentException illegalArgumentException(String message) {
		return new IllegalArgumentException(message);
	}

	public static final UnsupportedOperationException unsupportedMethodException() {
		return new UnsupportedOperationException("unsupport this method");
	}

	/**
	 * 获取最原始的抛出异常
	 * @param t 捕捉到的异常抛出对象
	 * @return
	 */
	public static final Throwable foundRealThrowable(Throwable t) {
		Throwable cause = t.getCause();
		if (cause == null) return t;
		return foundRealThrowable(cause);
	}

	/**
	 * 格式化异常
	 * @param t
	 * @return
	 */
	public static final String formatThrowable(Throwable t) {
		if (t == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	public static final String formatThrowableForHtml(Throwable t) {
		String ex = formatThrowable(t);
		return ex.replaceAll("\n\t", " ");
	}

}
