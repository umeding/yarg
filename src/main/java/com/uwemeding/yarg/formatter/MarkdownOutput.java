/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.formatter;

import com.uwemeding.yarg.OutputFormatContext;
import com.uwemeding.yarg.YargException;
import com.uwemeding.yarg.bindings.Application;
import java.io.File;

/**
 * Markdown output.
 *
 * @author uwe
 */
public class MarkdownOutput implements OutputFormatContext {

	@Override
	public void create(File outputDir, Application app) throws YargException {
		System.out.println("Markdown output");
	}

}
