/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import com.uwemeding.yarg.bindings.Application;
import java.io.File;

/**
 * Output format context.
 *
 * @author uwe
 */
public interface OutputFormatContext {

	/**
	 * Create an output in the requested format.
	 *
	 * @param outputDir the output folder
	 * @param app the application
	 * @throws YargException
	 */
	void create(File outputDir, Application app) throws YargException;

}
