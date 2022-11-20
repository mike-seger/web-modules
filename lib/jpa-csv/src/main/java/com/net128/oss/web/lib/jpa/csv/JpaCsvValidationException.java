package com.net128.oss.web.lib.jpa.csv;

public class JpaCsvValidationException extends RuntimeException {
	public JpaCsvValidationException(String message) {
		super(message);
	}

	public JpaCsvValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
