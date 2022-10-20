package com.net128.oss.web.lib.jpa.csv;

public class ValidationException extends RuntimeException {
	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
