/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.formatter;

import com.uwemeding.yarg.StringUtils;
import com.uwemeding.yarg.YargException;
import com.uwemeding.yarg.bindings.Application;
import com.uwemeding.yarg.bindings.Example;
import com.uwemeding.yarg.bindings.Method;
import com.uwemeding.yarg.bindings.RequestParameter;
import com.uwemeding.yarg.bindings.RequestTemplate;
import com.uwemeding.yarg.bindings.Response;
import com.uwemeding.yarg.bindings.RestCall;
import com.uwemeding.yarg.bindings.RestCalls;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * Markdown output.
 * <p>
 * @author uwe
 */
public class MarkdownOutput extends OutputContextBase {

	@Override
	public void create(File outputDir, Application app) throws YargException {
		System.out.println("Markdown output");

		File path = app.getPath().startsWith("/") ? new File(app.getPath()) : new File("/" + app.getPath());

		createIndex(outputDir, path, app);

		createRestCalls(outputDir, path, app);
	}

	private void createIndex(File outputDir, File path, Application app) {

		for (RestCalls restCalls : app.getRestCalls()) {
			File restCallsDir = new File(outputDir, restCalls.getName());
			restCallsDir.mkdirs();

			try (PrintWriter fp = new PrintWriter(new FileWriter(new File(restCallsDir, "index.md")))) {
				fp.println("# " + restCalls.getName());
				fp.println();
				fp.println(StringUtils.collapseWhitespace(restCalls.getDesc()));
				fp.println();

				File restCallsPath = new File(path, restCalls.getPath());

				fp.println("Type | Resource | Description");
				fp.println("--- | --- | ---:");
				for (RestCall restCall : restCalls.getRestCall()) {

					File restCallPath = new File(restCallsPath, restCall.getPath());

					for (Method m : restCall.getMethod()) {
						fp.println("__" + callType(m) + "__ | [`" + restCallPath + "`](http://www.github.com) | " + StringUtils.firstSentence(m.getDesc()));
					}
				}
			} catch (IOException ex) {
				throw new YargException(ex);
			}
		}
	}

	private void createRestCalls(File outputDir, File path, Application app) {

		for (RestCalls restCalls : app.getRestCalls()) {
			File restCallsDir = new File(outputDir, restCalls.getName());
			restCallsDir.mkdirs();

			File restCallsPath = new File(path, restCalls.getPath());

			int ncall = 1;
			for (RestCall restCall : restCalls.getRestCall()) {
				try (PrintWriter fp = new PrintWriter(new FileWriter(new File(restCallsDir, restCalls.getName() + "-" + ncall + ".md")))) {
					ncall++;

					File restCallPath = new File(restCallsPath, restCall.getPath());

					for (Method m : restCall.getMethod()) {

						// extend the path with the query parameters
						String fullRestCallPath = restCallPath.toString();
						if (m.getRequestParameters() != null) {
							StringBuilder sb = new StringBuilder(fullRestCallPath);

							String delim = "?";
							for (RequestParameter p : m.getRequestParameters().getRequestParameter()) {
								sb.append(delim).append(p.getName());
								delim = "&";
							}
							fullRestCallPath = sb.toString();
						}

						fp.println("# __" + callType(m) + "__ `" + fullRestCallPath + "`");
						fp.println(StringUtils.collapseWhitespace(m.getDesc()));

						// print the template parameters
						if (m.getRequestTemplates() != null && m.getRequestTemplates().getRequestTemplate().size() > 0) {
							fp.println("### Resource template parameters");
							fp.println("Parameter | Type | Description");
							fp.println("--- | --- | ---");
							if (m.getRequestTemplates().getRequestTemplate() != null) {
								for (RequestTemplate t : m.getRequestTemplates().getRequestTemplate()) {
									fp.println(t.getName() + " | " + shortName(t.getType()) + " | " + StringUtils.collapseWhitespace(t.getvalue()));
								}
							}
						}

						// print the request parameters
						if (m.getRequest() != null) {
							fp.println("### Request parameters");
							fp.println("`" + contentType(m.getRequest()) + "`, for example:");
							fp.println("```" + mdContentType(m.getRequest()));
							fp.println(StringUtils.removeLeadingSpaces(m.getRequest().getvalue()));
							fp.println("```");
						}

						// print the query parameters
						if (m.getRequestParameters() != null && m.getRequestParameters().getRequestParameter().size() > 0) {
							fp.println("### Request query parameters");
							fp.println("Parameter | Type | Default | Description");
							fp.println("--- | --- | --- | ---");
							if (m.getRequestParameters().getRequestParameter() != null) {
								for (RequestParameter p : m.getRequestParameters().getRequestParameter()) {
									fp.println(p.getName() + " | " + p.getType() + " | " + p.getDefault() + " | " + StringUtils.collapseWhitespace(p.getvalue()));
								}
							}
						}

						// print the available responses
						if (m.getResponses() != null && m.getResponses().getResponse().size() > 0) {
							fp.println("### Available responses");
							fp.println("Code | Type | Description");
							fp.println("--- | --- | ---");
							for (Response res : m.getResponses().getResponse()) {
								fp.println("__" + res.getCode() + "__ | `" + contentType(res) + "`|" + StringUtils.collapseWhitespace(res.getvalue()));
							}
						}

						// print the examples
						if (m.getExamples() != null && m.getExamples().getExample().size() > 0) {
							int nexamples = m.getExamples().getExample().size();
							if (nexamples > 1) {
								fp.println("### Examples");
							}
							int n = 1;
							for (Example example : m.getExamples().getExample()) {
								String title = example.getTitle() == null ? "" : example.getTitle();
								if (nexamples > 1) {
									fp.println(n + ". " + StringUtils.collapseWhitespace(title));
								} else {
									fp.println("### Example: " + StringUtils.collapseWhitespace(title));
								}
								n++;

								fp.println();
								fp.println("   ```" + mdContentType(example));
								try (LineNumberReader lnr = new LineNumberReader(
										new StringReader(StringUtils.removeLeadingSpaces(example.getvalue())))) {
									String line;
									while ((line = lnr.readLine()) != null) {
										fp.println("   " + line);
									}
								}
								fp.println("   ```");
								fp.println();
							}
						}
					}
				} catch (IOException ex) {
					throw new YargException(ex);
				}
			}
		}
	}

}
