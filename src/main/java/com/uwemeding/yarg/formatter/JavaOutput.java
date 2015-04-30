/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.formatter;

import com.uwemeding.yarg.OutputFormatContext;
import com.uwemeding.yarg.YargException;
import com.uwemeding.yarg.bindings.Application;
import java.io.File;

/**
 * Java output formatter.
 *
 * @author uwe
 */
public class JavaOutput implements OutputFormatContext {

	@Override
	public void create(File outputDir, Application app) throws YargException {
		System.out.println("Java output");
	}

}
