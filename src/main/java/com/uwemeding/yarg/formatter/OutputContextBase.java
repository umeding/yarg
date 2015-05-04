/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg.formatter;

import com.uwemeding.yarg.OutputFormatContext;
import com.uwemeding.yarg.YargException;
import com.uwemeding.yarg.bindings.Example;
import com.uwemeding.yarg.bindings.Method;
import com.uwemeding.yarg.bindings.Request;
import com.uwemeding.yarg.bindings.Response;

/**
 *
 * @author uwe
 */
public abstract class OutputContextBase implements OutputFormatContext {

	protected String callType(Method m) {
		switch (m.getType()) {
			default:
				throw new YargException(m.getType() + ": unknown method call type");
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
			case "text": return "";
			case "json": return "json";
			case "xml": return "xml";
			case "java": return "java";
			case "bash": return "bash";
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

}
