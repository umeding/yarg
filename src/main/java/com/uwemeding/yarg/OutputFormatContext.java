/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import com.uwemeding.yarg.bindings.Application;
import java.io.File;

/**
 * Output format context.
 * <p>
 * @author uwe
 */
public interface OutputFormatContext {

	/**
	 * Prepare the application for output processing. Missing fields will be set
	 * to their defaults as required.
	 * <p>
	 * @param app
	 * @throws YargException
	 */
	void prepare(Application app) throws YargException;

	/**
	 * Create an output in the requested format.
	 * <p>
	 * @param outputDir the output folder
	 * @param app       the application
	 * @throws YargException
	 */
	void create(File outputDir, Application app) throws YargException;

}
