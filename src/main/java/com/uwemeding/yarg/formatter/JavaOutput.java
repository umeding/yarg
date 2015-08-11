/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.formatter;

import com.uwemeding.yarg.StringUtils;
import com.uwemeding.yarg.YargException;
import com.uwemeding.yarg.bindings.Application;
import com.uwemeding.yarg.bindings.Context;
import com.uwemeding.yarg.bindings.Method;
import com.uwemeding.yarg.bindings.RequestParameter;
import com.uwemeding.yarg.bindings.RequestTemplate;
import com.uwemeding.yarg.bindings.Response;
import com.uwemeding.yarg.bindings.RestCall;
import com.uwemeding.yarg.bindings.RestCalls;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * Java output formatter.
 * <p>
 * @author uwe
 */
public class JavaOutput extends OutputContextBase {

	private static final String SEP = "******************************************************";
	private static final String GEN_NOTE = "* NOTE: GENERATED CONTENT -- CHANGES WILL DISAPPEAR! *";

	private String copyrightText;

	@Override
	public void create(File outputDir, Application app) throws YargException {
		try {
			if (app.getCopyright() == null) {
				SimpleDateFormat yearFmt = new SimpleDateFormat("yyyy");
				String year = yearFmt.format(new Date());
				copyrightText = "Copyright (c) " + year + " -- All Rights Reserved.";
			} else {
				copyrightText = app.getCopyright();
			}

			createApplicationConfig(outputDir, app);

			for (RestCalls restCalls : app.getRestCalls()) {
				createRestProxy(outputDir, app, restCalls);
			}

//			System.out.println("Java output");
			Java.INTERFACE business = Java.createInterface("public", app.getName());
			Java.setBaseDirectory(outputDir);
//			Java.createSource(clazz);
		} catch (IOException ex) {
			throw new YargException(ex);
		}
	}

	private void createApplicationConfig(File outputDir, Application app) throws IOException {

		String appConfigName = app.getName() == null ? "ApplicationConfig" : app.getName();
		Java.CLASS clazz = Java.createClass("public", appConfigName);
		clazz.setCopyright(copyrightText);

		clazz.addC(true, SEP);
		clazz.addC(true, GEN_NOTE);
		clazz.addC(true, SEP);

		clazz.addIMPORT("javax.ws.rs.ApplicationPath");
		clazz.addANNOTATION("ApplicationPath").plain("ApplicationConfig.APPLICATION_PATH");
		clazz.addVAR("public static final", "String", "APPLICATION_PATH", "\"" + (app.getPath() == null ? "" : app.getPath()) + "\"");
		clazz.setComment("Application Resources");

		if (app.getPackage() != null && app.getPackage().getPath() != null) {
			clazz.setPackage(app.getPackage().getPath());
		}

		clazz.setExtends("Application");
		clazz.addIMPORT("javax.ws.rs.core.Application");

		Java.setBaseDirectory(outputDir);

		// implement the getClasses method.
		Java.METHOD classes = clazz.addMETHOD("public", "Set<Class<?>>", "getClasses");
		classes.setComment("Declare the implementing classes for this application");
		classes.setReturnComment("The set of classes for this application");
		classes.addOverrideAnnotation();
		clazz.addIMPORT("java.util.Set", "java.util.HashSet");
		classes.addS("Set<Class<?>> classes = new HashSet<>()");
		classes.addC(true, "add the implementing classes here");
		classes.addRETURN("classes");
		
		Java.createSource(clazz, false);  // don't override
//		Java.createSource(clazz);
	}

	/*
	 * Create the REST proxy.
	 */
	private void createRestProxy(File outputDir, Application app, RestCalls restCalls)
			throws IOException {

		Java.INTERFACE proxy = Java.createInterface("public", restCalls.getName());
		proxy.setCopyright(copyrightText);

		proxy.addC(true, SEP);
		proxy.addC(true, GEN_NOTE);
		proxy.addC(true, SEP);

		proxy.addEXTENDS("Serializable");
		proxy.setPackage(app.getPackage().getPath());
		proxy.addANNOTATION("Path").string(restCalls.getPath());
		proxy.addIMPORT("javax.ws.rs.Path", "java.io.Serializable");

		if (restCalls.getDesc() != null) {
			proxy.setComment(StringUtils.collapseWhitespace(restCalls.getDesc()));
		}

		for (RestCall call : restCalls.getRestCall()) {
			for (Method m : call.getMethod()) {
				Java.METHOD method = proxy.addMETHOD("Response", m.getName());
				if (m.getDesc() != null) {
					method.setComment(StringUtils.collapseWhitespace(m.getDesc()));
				}
				method.addANNOTATION(callType(m));
				proxy.addIMPORT(callTypeImport(m));

				// Create the @Path parameter, we also must include the position
				// parameter names
				String pathDesc;
				if (m.getRequestTemplates() != null) {
					StringBuilder sb = new StringBuilder(call.getPath());
					for (RequestTemplate temp : m.getRequestTemplates().getRequestTemplate()) {
						sb.append("/{").append(temp.getName()).append("}");
					}
					pathDesc = sb.toString();
				} else {
					pathDesc = call.getPath();
				}
				method.addANNOTATION("Path").string(pathDesc);

				method.setReturnComment("a REST response");
				proxy.addIMPORT("javax.ws.rs.core.Response");

				// Permissions
				if (m.getRoles() == null) {
					method.addANNOTATION("PermitAll");
					proxy.addIMPORT("javax.annotation.security.PermitAll");
				} else {
					String[] roles = m.getRoles().getPermit().split(",");
					for (int i = 0; i < roles.length; i++) {
						roles[i] = roles[i].trim();//.toUpperCase();
					}
					if (roles.length > 0) {
						method.addANNOTATION("RolesAllowed").string(roles);
						proxy.addIMPORT("javax.annotation.security.RolesAllowed");
					}
				}

				if (m.getRequestTemplates() != null) {
					for (RequestTemplate temp : m.getRequestTemplates().getRequestTemplate()) {
						method.addArg(shortName(temp.getType()), temp.getName(), temp.getvalue())
								.addANNOTATION("PathParam").string(temp.getName());

						proxy.addIMPORT("javax.ws.rs.PathParam");

						if (temp.getType().contains(".")) {
							addAnImportIfNeeded(app, proxy, temp.getType());
						}
					}
				}
				if (m.getRequest() != null) {
					String argName = m.getRequest().getName();
					String argType = m.getRequest().getType();
					String shortArgType = shortName(argType);
					method.addANNOTATION("Consumes").string(contentType(m.getRequest()));
					proxy.addIMPORT("javax.ws.rs.Consumes");

					method.addArg(shortArgType, argName, shortArgType);
					addAnImportIfNeeded(app, proxy, argType);

				} else if (m.getRequestParameters() != null) {
					for (RequestParameter para : m.getRequestParameters().getRequestParameter()) {
						Java.Arg arg = method.addArg(shortName(para.getType()), para.getName(), para.getvalue());
						arg.addANNOTATION("QueryParam").string(para.getName());
						if (para.getDefault() != null) {
							arg.addANNOTATION("DefaultValue").string(para.getDefault());
						}

						proxy.addIMPORT("javax.ws.rs.DefaultValue");
						proxy.addIMPORT("javax.ws.rs.QueryParam");

						if (para.getType().contains(".")) {
							addAnImportIfNeeded(app, proxy, para.getType());
						}
					}
				}
				// add the context arguments
				if (m.getContexts() != null && m.getContexts().getContext().size() > 0) {
					for (Context c : m.getContexts().getContext()) {
						Java.Arg arg = method.addArg(shortName(c.getType()), c.getName(), c.getvalue());
						arg.addANNOTATION("Context");
						proxy.addIMPORT("javax.ws.rs.core.Context");
						if (c.getType().contains(".")) {
							addAnImportIfNeeded(app, proxy, c.getType());
						}
					}
				}

				// handle the responses
				if (m.getResponses() != null) {
					Set<String> respTypes = new TreeSet<>();
					for (Response res : m.getResponses().getResponse()) {
						respTypes.add(contentType(res));
					}

					method.addANNOTATION("Produces").string(respTypes.toArray(new String[0]));
					proxy.addIMPORT("javax.ws.rs.Produces");
				}
			}
		}

		Java.setBaseDirectory(outputDir);
		Java.createSource(proxy);
	}

	// See if a desired import is in the package already
	private void addAnImportIfNeeded(Application app, Java.INTERFACE iface, String importName) {
		String packageName = app.getPackage().getPath();
		int last = importName.lastIndexOf(".");
		if (last > 0) {
			String path = importName.substring(0, last);
			if (!packageName.equals(path)) {
				iface.addIMPORT(importName);
			}
		}
	}

}
