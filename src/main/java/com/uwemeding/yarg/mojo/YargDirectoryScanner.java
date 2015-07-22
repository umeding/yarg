/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Scans source directories for YARG grammar files.
 * <p>
 * @author uwe
 */
public class YargDirectoryScanner {

	/**
	 * The directory scanner used to scan the source directory for files.
	 */
	private final DirectoryScanner scanner;

	/**
	 * The absolute path to the output directory used to detect stale target
	 * files by timestamp checking, may be <code>null</code> if no stale
	 * detection should be performed.
	 */
	private File outputDirectory;

	// TODO: Once the parameter "packageName" from the yarg mojo has been deleted, remove this field, too.
	/**
	 * The package name for the generated parser, may be <code>null</code> to
	 * use the package declaration from the grammar file.
	 */
	private String parserPackage;

	/**
	 * The granularity in milliseconds of the last modification date for testing
	 * whether a grammar file needs recompilation because its corresponding
	 * target file is stale.
	 */
	private int staleMillis;

	/**
	 * A set of grammar infos describing the included grammar files, must never
	 * be <code>null</code>.
	 */
	private final List<YargInfo> includedGrammars;

	/**
	 * Creates a new grammar directory scanner.
	 */
	public YargDirectoryScanner() {
		this.scanner = new DirectoryScanner();
		this.scanner.setFollowSymlinks(true);
		this.includedGrammars = new ArrayList<>();
	}

	/**
	 * Sets the absolute path to the source directory to scan for grammar files.
	 * This directory must exist or the scanner will report an error.
	 * <p>
	 * @param directory The absolute path to the source directory to scan, must
	 *                  not be <code>null</code>.
	 */
	public void setSourceDirectory(File directory) {
		if (!directory.isAbsolute()) {
			throw new IllegalArgumentException("source directory is not absolute: " + directory);
		}
		this.scanner.setBasedir(directory);
	}

	/**
	 * Sets the package name for the generated parser.
	 * <p>
	 * @param packageName The package name for the generated parser, may be
	 *                    <code>null</code> to use the package declaration from
	 *                    the grammar file.
	 */
	public void setParserPackage(String packageName) {
		this.parserPackage = packageName;
	}

	/**
	 * Sets the Ant-like inclusion patterns.
	 * <p>
	 * @param includes The set of Ant-like inclusion patterns, may be
	 *                 <code>null</code> to include all files.
	 */
	public void setIncludes(String[] includes) {
		this.scanner.setIncludes(includes);
	}

	/**
	 * Sets the Ant-like exclusion patterns.
	 * <p>
	 * @param excludes The set of Ant-like exclusion patterns, may be
	 *                 <code>null</code> to exclude no files.
	 */
	public void setExcludes(String[] excludes) {
		this.scanner.setExcludes(excludes);
		this.scanner.addDefaultExcludes();
	}

	/**
	 * Sets the absolute path to the output directory used to detect stale
	 * target files.
	 * <p>
	 * @param directory The absolute path to the output directory used to detect
	 *                  stale target files by timestamp checking, may be
	 *                  <code>null</code> if no stale detection should be
	 *                  performed.
	 */
	public void setOutputDirectory(File directory) {
		if (directory != null && !directory.isAbsolute()) {
			throw new IllegalArgumentException("output directory is not absolute: " + directory);
		}
		this.outputDirectory = directory;
	}

	/**
	 * Sets the granularity in milliseconds of the last modification date for
	 * stale file detection.
	 * <p>
	 * @param milliseconds The granularity in milliseconds of the last
	 *                     modification date for testing whether a grammar file
	 *                     needs recompilation because its corresponding target
	 *                     file is stale.
	 */
	public void setStaleMillis(int milliseconds) {
		this.staleMillis = milliseconds;
	}

	/**
	 * Scans the source directory for grammar files that match at least one
	 * inclusion pattern but no exclusion pattern, optionally performing
	 * timestamp checking to exclude grammars whose corresponding parser files
	 * are up to date.
	 * <p>
	 * @throws IOException If a grammar file could not be analyzed for metadata.
	 */
	public void scan() throws IOException {
		this.includedGrammars.clear();
		this.scanner.scan();

		String[] includedFiles = this.scanner.getIncludedFiles();
		for (String includedFile : includedFiles) {
			YargInfo grammarInfo = new YargInfo(this.scanner.getBasedir(), includedFile, this.parserPackage);
			this.includedGrammars.add(grammarInfo);
		}
	}

	/**
	 * Gets the grammar files that were included by the scanner during the last
	 * invocation of {@link #scan()}.
	 * <p>
	 * @return An array of grammar infos describing the included grammar files,
	 *         will be empty if no files were included but is never
	 *         <code>null</code>.
	 */
	public YargInfo[] getIncludedGrammars() {
		return this.includedGrammars.toArray(new YargInfo[0]);
	}

}
