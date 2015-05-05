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
import java.util.Set;
import java.util.TreeSet;

/**
 * Java output formatter.
 * <p>
 * @author uwe
 */
public class JavaOutput extends OutputContextBase {

	@Override
	public void create(File outputDir, Application app) throws YargException {
		try {
			createApplicationConfig(outputDir, app);

			for (RestCalls restCalls : app.getRestCalls()) {
				createRestProxy(outputDir, app, restCalls);
			}

			System.out.println("Java output");

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
		clazz.addANNOTATION("ApplicationPath").string(app.getPath() == null ? "" : app.getPath());
		clazz.addIMPORT("javax.ws.rs.ApplicationPath");
		clazz.setComment("Application Resources");

		if (app.getPackage() != null && app.getPackage().getPath() != null) {
			clazz.setPackage(app.getPackage().getPath());
		}

		clazz.setExtends("Application");
		clazz.addIMPORT("javax.ws.rs.core.Application");

		Java.METHOD method;

		method = clazz.addMETHOD("public", "Set<Class<?>>", "getClasses");
		method.setComment("Get the resource classes for this application");
		method.addOverrideAnnotation();

		Java.VAR var = method.addVAR(null, "Set<Class<?>>", "resources", "new HashSet<>()");
		clazz.addIMPORT("java.util.Set", "java.util.HashSet");
		method.addS("addRestResourceClasses(resources)");
		method.addRETURN("resources");

		method = clazz.addMETHOD("private", "void", "addRestResourceClasses");
		method.setComment("Do not modify this method. It is automatically "
				+ "populated with all resources defined in the project. If required, comment "
				+ "out calling this method in getClasses().");
		method.addArg("Set<Class<?>>", "resources", "the resource set");

		Java.setBaseDirectory(outputDir);
		Java.createSource(clazz, false);
	}

	/*
	 * Create the REST proxy.
	 */
	private void createRestProxy(File outputDir, Application app, RestCalls restCalls)
			throws IOException {

		Java.INTERFACE proxy = Java.createInterface("public", restCalls.getName());
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
					method.setComment(m.getDesc());
				}
				method.addANNOTATION(callType(m));
				proxy.addIMPORT(callTypeImport(m));
				method.addANNOTATION("Path").string(call.getPath());
				method.setReturnComment("a REST response");
				proxy.addIMPORT("javax.ws.rs.core.Response");

				// Permissions
				if (m.getRoles() == null) {
					method.addANNOTATION("PermitAll");
					proxy.addIMPORT("javax.annotation.security.PermitAll");
				} else {
					String[] roles = m.getRoles().getPermit().split(",");
					for (int i = 0; i < roles.length; i++) {
						roles[i] = roles[i].trim().toUpperCase();
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
						if (temp.getType().contains(".")) {
							proxy.addIMPORT(temp.getType());
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
					proxy.addIMPORT(argType);

				} else if (m.getRequestParameters() != null) {
					for (RequestParameter para : m.getRequestParameters().getRequestParameter()) {
						Java.Arg arg = method.addArg(shortName(para.getType()), para.getName(), para.getvalue());
						arg.addANNOTATION("DefaultValue").string(para.getDefault());
						arg.addANNOTATION("QueryParam").string(para.getName());

						if (para.getType().contains(".")) {
							proxy.addIMPORT(para.getType());
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
							proxy.addIMPORT(c.getType());
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

}
