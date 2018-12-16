package com.simon.credit.exception;

/**
 * 单例异常
 * @author XUZIMING 2018-06-24
 */
public class SingletonException extends RuntimeException {
	private static final long serialVersionUID = -30273207816591221L;

	public SingletonException(Class<?> clazz) {
		super("not allow instantiate again: " + clazz.getName());
	}

}
