/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.mojo;

import com.uwemeding.yarg.Yarg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Parses a YARG ReST description and generates the appropriate source code.
 * <p>
 * @author uwe
 */
@Mojo(name = "yarg", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class YargMojo extends AbstractMojo {

	/**
	 * The current Maven project
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	/**
	 * The set of compile source roots whose contents are not generated as part
	 * of the build, i.e. those that usually reside somewhere below
	 * "${basedir}/src" in the project structure. Files in these source roots
	 * are owned by the user and must not be overwritten with generated files.
	 */
	private Collection<File> nonGeneratedSourceRoots;

	/**
	 * Package into which the YARG program classes will be put.
	 */
	@Parameter(property = "packageName")
	private String packageName;

	/**
	 * Directory where the YARG source files (<code>*.xml</code>) are located.
	 */
	@Parameter(defaultValue = "${basedir}/src/main/yarg", property = "sourceDir")
	private File sourceDirectory;

	/**
	 * Output formatter
	 */
	@Parameter(defaultValue = "java", property = "formatter")
	private String formatter;

	/**
	 * Directory where the YARG created files will be stored.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/yarg", property = "outputDirectory", required = false)
	private File outputDirectory;

	/**
	 * Java source directory
	 */
	@Parameter(defaultValue = "${basedir}/src/main/java", property = "javaSourceDirectory", required = false)
	private File javaSourceDirectory;

	/**
	 * Indicate if we want to output to the java source dir
	 */
	@Parameter(defaultValue = "false", property = "outputToJavaSource", required = false)
	private String outputToJavaSource;
	/**
	 * The granularity in milliseconds of the last modification date for testing
	 * whether a source needs recompilation.
	 */
	@Parameter(defaultValue = "0", property = "staleMillis")
	private int staleMillis;

	/**
	 * A set of Ant-like inclusion patterns used to select files from the source
	 * directory for processing. By default, the patterns
	 * <code>**&#47;*.xml</code> and <code>**&#47;*.XML</code> are used to
	 * select YARG files.
	 */
	@Parameter
	private String[] includes;

	/**
	 * A set of Ant-like exclusion patterns used to prevent certain files from
	 * being processed. By default, this set is empty such that no files are
	 * excluded.
	 */
	@Parameter
	private String[] excludes;

	/**
	 * {@inheritDoc}
	 * <p>
	 * @return the included files
	 */
	protected String[] getIncludes() {
		if (this.includes != null) {
			return this.includes;
		} else {
			return new String[]{"**/*.xml", "**/*.XML"};
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @return the files to be excluded
	 */
	protected String[] getExcludes() {
		return this.excludes;
	}

	/**
	 * {@inheritDoc }
	 * <p>
	 * @return the package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * {@inheritDoc }
	 * <p>
	 * @return the source directory
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
	}

	/**
	 * {@inheritDoc }
	 * <p>
	 * @return the output directory
	 */
	public File getOutputDirectory() {
		return getOutputToJavaSourceValue() ? javaSourceDirectory : outputDirectory;
	}

	/**
	 * {@inheritDoc }
	 * <p>
	 * @return the formatter preference
	 */
	public String getFormatter() {
		return formatter;
	}

	public File getJavaSourceDirectory() {
		return javaSourceDirectory;
	}

	public String getOutputToJavaSource() {
		return outputToJavaSource;
	}

	public boolean getOutputToJavaSourceValue() {
		return Boolean.parseBoolean(outputToJavaSource);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @return millisecond difference that triggers a recompile
	 */
	protected int getStaleMillis() {
		return this.staleMillis;
	}

	/**
	 * Scans the configured source directory for grammar files which need
	 * processing.
	 * <p>
	 * @return An array of grammar infos describing the found grammar files or
	 *         <code>null</code> if the source directory does not exist.
	 * @throws MojoExecutionException If the source directory could not be
	 *                                scanned.
	 */
	private YargInfo[] scanForGrammars()
			throws MojoExecutionException {
		if (!getSourceDirectory().isDirectory()) {
			return null;
		}

		YargInfo[] grammarInfos;

		getLog().debug("Scanning for descriptions: " + getSourceDirectory());
		try {
			YargDirectoryScanner scanner = new YargDirectoryScanner();
			scanner.setSourceDirectory(getSourceDirectory());
			scanner.setIncludes(getIncludes());
			scanner.setExcludes(getExcludes());
			scanner.setOutputDirectory(getOutputDirectory());
			scanner.setStaleMillis(getStaleMillis());
			scanner.scan();
			grammarInfos = scanner.getIncludedGrammars();
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to scan for grammars: " + getSourceDirectory(), e);
		}
		getLog().debug("Found grammars: " + Arrays.asList(grammarInfos));

		return grammarInfos;
	}

	/**
	 * Determines those compile source roots of the project that do not reside
	 * below the project's build directories. These compile source roots are
	 * assumed to contain hand-crafted sources that must not be overwritten with
	 * generated files. In most cases, this is simply
	 * "${project.build.sourceDirectory}".
	 * <p>
	 * @throws MojoExecutionException If the compile source roots could not be
	 *                                determined.
	 */
	private void determineNonGeneratedSourceRoots() throws MojoExecutionException {
		this.nonGeneratedSourceRoots = new LinkedHashSet<>();
		try {
			String targetPrefix
					= new File(this.project.getBuild().getDirectory()).getCanonicalPath() + File.separator;
			Collection sourceRoots = this.project.getCompileSourceRoots();
			for (Iterator it = sourceRoots.iterator(); it.hasNext();) {
				File sourceRoot = new File(it.next().toString());
				if (!sourceRoot.isAbsolute()) {
					sourceRoot = new File(this.project.getBasedir(), sourceRoot.getPath());
				}
				String sourcePath = sourceRoot.getCanonicalPath();
				if (!sourcePath.startsWith(targetPrefix)) {
					this.nonGeneratedSourceRoots.add(sourceRoot);
					getLog().debug("Non-generated compile source root: " + sourceRoot);
				} else {
					getLog().debug("Generated compile source root: " + sourceRoot);
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to determine non-generated source roots", e);
		}
	}

	/**
	 * Determines whether the specified directory denotes a compile source root
	 * of the current project.
	 * <p>
	 * @param directory The directory to check, must not be <code>null</code>.
	 * @return <code>true</code> if the specified directory is a compile source
	 *         root of the project, <code>false</code> otherwise.
	 */
	protected boolean isSourceRoot(File directory) {
		return this.nonGeneratedSourceRoots.contains(directory);
	}

	/**
	 * Execute the YARG compile.
	 * <p>
	 * @throws org.apache.maven.plugin.MojoExecutionException if the compile
	 *                                                        failed
	 */
	@Override
	public void execute() throws MojoExecutionException {
		getLog().info("Running YARG for "+getFormatter());

		YargInfo[] grammarInfos = scanForGrammars();

		if (grammarInfos == null) {
			getLog().info("Skipping non-existing source directory: " + getSourceDirectory());
			return;
		} else if (grammarInfos.length <= 0) {
			getLog().info("Skipping - all parsers are up to date");
		} else {
			determineNonGeneratedSourceRoots();

			for (YargInfo grammarInfo : grammarInfos) {
				processRESTDescriptions(grammarInfo);
			}

			getLog().info("Processed " + grammarInfos.length + " description" + (grammarInfos.length != 1 ? "s" : ""));
		}

	}

	/**
	 * Generate the command line arguments.
	 * <p>
	 * @param info the YARG file info
	 * @return the command line arguments
	 */
	private String[] generateArgs(YargInfo info) {
		List<String> argsList = new ArrayList<>();
		File outputdir = getOutputDirectory();
		if (outputdir != null) {
			argsList.add("--outputdir=" + outputdir.getAbsolutePath());
		}
		if (packageName != null) {
			argsList.add("--package=" + packageName);
		}
		if (formatter != null) {
			argsList.add("--format=" + formatter);
		} else {
			argsList.add("--format=java");
		}
		argsList.add(info.getInputFile().getAbsoluteFile().toString());
		return argsList.toArray(new String[0]);
	}

	/**
	 * Process the YARG command by the YARG main program.
	 * <p>
	 * @param info the YARG file info
	 * @throws MojoExecutionException if the compile failed for some reason
	 */
	private void processRESTDescriptions(YargInfo info) throws MojoExecutionException {
		getLog().info("Processing: " + info.getInputFile());

		try {
			String[] args = generateArgs(info);
			Yarg.main(args);
		} catch (Exception e) {
			throw new MojoExecutionException("Yarg execution problem", e);
		}
	}

}
