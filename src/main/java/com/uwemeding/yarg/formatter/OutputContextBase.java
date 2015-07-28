/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.formatter;

import com.uwemeding.yarg.OutputFormatContext;
import com.uwemeding.yarg.YargException;
import com.uwemeding.yarg.bindings.Application;
import com.uwemeding.yarg.bindings.Example;
import com.uwemeding.yarg.bindings.Method;
import com.uwemeding.yarg.bindings.Request;
import com.uwemeding.yarg.bindings.Response;
import com.uwemeding.yarg.bindings.RestCall;
import com.uwemeding.yarg.bindings.RestCalls;

/**
 *
 * @author uwe
 */
public abstract class OutputContextBase implements OutputFormatContext {

	protected String callType(Method m) {
		switch (m.getCallType()) {
			default:
				throw new YargException(m.getCallType() + ": unknown method call type");
			case "get":
				return "GET";
			case "put":
				return "PUT";
			case "post":
				return "POST";
			case "delete":
				return "DELETE";
		}
	}

	protected String callTypeImport(Method m) {
		switch (m.getCallType()) {
			default:
				throw new YargException(m.getCallType() + ": unknown method call type");
			case "get":
				return "javax.ws.rs.GET";
			case "put":
				return "javax.ws.rs.PUT";
			case "post":
				return "javax.ws.rs.POST";
			case "delete":
				return "javax.ws.rs.DELETE";
		}
	}

	protected String contentType(Request req) {
		return contentType(req.getContentType());
	}

	protected String contentType(Response res) {
		return contentType(res.getContentType());
	}

	protected String contentType(String string) {
		switch (string) {
			default:
				throw new YargException(string + ": unknown content type");
			case "json":
				return "application/json";
			case "xml":
				return "application/xml";
			case "text":
				return "text/plain";
			case "csv":
				return "text/csv";
		}
	}

	protected String mdContentType(Example example) {
		return mdContentType(example.getContentType());

	}

	protected String mdContentType(Request req) {
		return mdContentType(req.getContentType());
	}

	protected String mdContentType(String string) {
		switch (string) {
			case "text":
				return "";
			case "json":
				return "json";
			case "xml":
				return "xml";
			case "java":
				return "java";
			case "bash":
				return "bash";
			case "csv":
				return "csv";
			default:
				throw new YargException(string + ": unknown content type");
		}
	}

	protected String shortName(String fullName) {
		int lastIndex = fullName.lastIndexOf('.');
		if (lastIndex >= 0) {
			return fullName.substring(lastIndex + 1);
		} else {
			return fullName;
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void prepare(Application app) throws YargException {

		// Set the method name to the rest call path if not set. Append the
		// call type if we have more than one call.
		for (RestCalls restCalls : app.getRestCalls()) {
			for (RestCall restCall : restCalls.getRestCall()) {
				if (restCall.getMethod().size() == 1) {
					Method m = restCall.getMethod().get(0);
					if (m.getName() == null) {
						m.setName(restCall.getPath());
					}
				} else {
					for (Method m : restCall.getMethod()) {
						if (m.getName() == null) {
							m.setName(restCall.getPath() + "_" + m.getCallType());
						}
					}
				}
			}
		}
	}

}
