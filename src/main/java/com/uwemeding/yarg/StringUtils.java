/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 * @author uwe
 */
public class StringUtils {

	public final static String EMPTY_STRING = "";

	/**
	 * Replaces all whitespaces from a string with space, removes all redundant
	 * whitespaces.
	 * <p>
	 * @param string string to collapse
	 * @return the collapsed string
	 */
	public static String collapseWhitespace(String string) {
		if (string == null) {
			return (null);
		}

		string = string.trim();
		boolean haveWS = false;
		StringBuilder sb = new StringBuilder();
		char[] ca = string.toCharArray();
		for (char c : ca) {
			if (Character.isWhitespace(c)) {
				if (!haveWS) {
					sb.append(' ');
				}
				haveWS = true;
			} else {
				haveWS = false;
				sb.append(c);
			}
		}
		return (sb.toString());
	}

	/**
	 * Remove the leading spaces from a chunk of text.
	 * <p>
	 * @param text the text
	 * @return the space-less text
	 */
	public static String removeLeadingSpaces(String text) {
		try (LineNumberReader fp = new LineNumberReader(new StringReader(text))) {

			fp.mark(text.length() + 1);

			// count spaces
			int nspaces = text.length();
			String line;
			while ((line = fp.readLine()) != null) {
				char[] cs = line.toCharArray();
				for (int i = 0; i < cs.length; i++) {
					if (!Character.isWhitespace(cs[i])) {
						nspaces = Math.min(nspaces, i);
					}
				}
			}

			fp.reset();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			while ((line = fp.readLine()) != null) {
				if (line.length() > nspaces) {
					pw.println(line.substring(nspaces));
				}
			}
			return sw.toString();
		} catch (IOException e) {
			throw new YargException(e);
		}
	}

	/**
	 * Extract a short description from the text. Usually this is the first
	 * sentence.
	 * <p>
	 * @param text the incoming text
	 * @return the first sentence
	 */
	public static String firstSentence(String text) {
		char[] cs = collapseWhitespace(text).toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cs.length; i++) {
			if (i > 0 && cs[i] == '.') {
				sb.append(".");
				break;
			} else {
				sb.append(cs[i]);
			}
		}
		return sb.toString();
	}

	/**
	 * Encode a text for db usage etc.
	 * <p>
	 * @param text the text to be encoded
	 * @return the encoded text
	 */
	public static String encode(String text) {
		if (text == null) {
			return (null);
		}
		try {
			return (URLEncoder.encode(text, "UTF-8"));
		} catch (Exception e) {
			return (text);
		}
	}

	/**
	 * Decode a string for db usage etc.
	 * <p>
	 * @param text is hte text
	 * @return the decoded text
	 */
	public static String decode(String text) {
		if (text == null) {
			return (null);
		}
		try {
			return (URLDecoder.decode(text, "UTF-8"));
		} catch (Exception e) {
			return (text);
		}
	}

	/**
	 * Process the escape characters in a string
	 * <p>
	 * @param string the incoming stinrg
	 * @return the resulting string
	 */
	public static String processEscapes(String string) {
		if (string == null) {
			return (null);
		}
		StringBuilder sb = new StringBuilder();
		char[] ca = string.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			switch (ca[i]) {
				case '\\':
					if (i < ca.length - 1) {
						i++;
						// replace the escaped characters
						switch (ca[i]) {
							case 'n':
								sb.append("\n");
								break;
							case 'r':
								sb.append("\r");
								break;
							case 't':
								sb.append("\t");
								break;
							case '\\':
								// ignore
								break;
							default:
								// just take the next one as-is
								sb.append(ca[i]);
								break;
						}
					}
					break;
				default:
					sb.append(ca[i]);
					break;
			}
		}
		return (sb.toString());
	}

	/**
	 * Convert a path into a Java (dotted) path.
	 * <p>
	 * @param path the path
	 * @return the java path
	 */
	public static String javaPath(String path) {
		return path.replace("/", ".");
	}

	public static File filePath(File base, String path) {
		return new File(base, path.replace(".", "/"));
	}

	public static File filePath(String path) {
		return filePath(new File(""), path);
	}

}
