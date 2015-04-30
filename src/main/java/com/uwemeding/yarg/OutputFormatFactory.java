/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import com.uwemeding.yarg.formatter.JavaOutput;
import com.uwemeding.yarg.formatter.MarkdownOutput;

/**
 * Locate an output formatter.
 *
 * @author uwe
 */
public class OutputFormatFactory {

	public static OutputFormatContext getInstance(String name) {
		switch (name) {
			case "java":
				return new JavaOutput();
				
			case "markdown":
				return new MarkdownOutput();
				
			default:
				throw new YargException(name + ": output format not supported");
		}
	}
}
