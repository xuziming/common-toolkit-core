package com.simon.credit.toolkit.concurrent;

import java.util.concurrent.ExecutionException;

public class MyExecutionException extends ExecutionException {
	private static final long serialVersionUID = -578932460305933159L;

	protected MyExecutionException() {}

	protected MyExecutionException(String message) {
		super(message);
	}

	public MyExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyExecutionException(Throwable cause) {
		super(cause);
	}

}