/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import com.uwemeding.yarg.bindings.Application;
import java.io.File;
import java.io.FileInputStream;

/**
 * Main program.
 *
 * @author uwe
 */
public class Yarg {

	private static final GetOpt options;
	public static boolean DEBUG = true;

	static {
		options = new GetOpt()
				.add("help", "h", false, "Some Help")
				.add("debug", "d", false, "Debug (currently default)")
				.add("format", "f", true, "Output format (java | markdown)")
				.add("outputdir", "o", true, "Output directory");
	}

	private static void printHelp() {
		printHelp(false);
	}

	private static void printHelp(boolean errorExit) {
		System.out.println("Usage:");
		System.out.println();
		System.out.println("$ java -jar yarg.jar [OPTIONS]");
		System.out.println();
		System.out.println("Options:");
		for (GetOpt.Definition def : options.definitions()) {
			System.out.printf("    %-30s %s%n", def.toString(), def.getHelpText());
		}
		System.exit(errorExit ? 1 : 0);
	}

	private void execute(String... av) throws Exception {
		File outputdir = new File("./");
		OutputFormatContext context = null;
		for (GetOpt.Option opt : options.parseOptions(av)) {
			switch (opt.getLongName()) {

				case "help":
					printHelp();
					return;

				case "outputdir":
					// Setup the output directory
					outputdir = new File(opt.getValue());
					if (!outputdir.exists()) {
						outputdir.mkdirs();
					}
					if (!outputdir.isDirectory()) {
						throw new YargException(opt.getValue() + ": not a directory");
					}
					break;

				case "format":
					// find the desired output formatter
					context = OutputFormatFactory.getInstance(opt.getValue());
					break;
			}
		}
		if (context == null) {
			throw new YargException("must declare one formatter");
		}

		// allow DTD access
		System.setProperty("javax.xml.accessExternalDTD", "all");

		// now loop through the input files
		for (int i = options.getOptind(); i < av.length; i++) {
			try (FileInputStream fp = new FileInputStream(av[i])) {
				Application app = new RestDescBuilder().fromXML(fp);

				context.prepare(app);
				context.create(outputdir, app);
			}
		}
//		System.out.println("--> done.");
	}

	public static void main(String... av) throws Exception {
		new Yarg().execute(av);
	}

}
