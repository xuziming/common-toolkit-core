package com.simon.credit.toolkit.codec.binary;

public class DecoderException extends Exception {
	private static final long serialVersionUID = -6730576705109523346L;

	public DecoderException() {
		super();
	}

	public DecoderException(final String message) {
		super(message);
	}

	public DecoderException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DecoderException(final Throwable cause) {
		super(cause);
	}

}
