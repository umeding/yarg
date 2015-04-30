/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

/**
 * A YARG exception.
 *
 * @author uwe
 */
public class YargException extends RuntimeException {

	public YargException() {
	}

	public YargException(String message) {
		super(message);
	}

	public YargException(String message, Throwable cause) {
		super(message, cause);
	}

	public YargException(Throwable cause) {
		super(cause);
	}

	public YargException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
