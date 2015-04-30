/*
 * Copyright (c) 2014 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import com.uwemeding.yarg.bindings.Application;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Helper: Manage REST descriptions.
 *
 * @author uwe
 */
public class RestDescBuilder {

	private final JAXBContext jCxt;

	public RestDescBuilder() {
		try {
			jCxt = JAXBContext.newInstance(Application.class.getPackage().getName());
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create an XML string from the devices.
	 *
	 * @param app the application.
	 * @return the XML string
	 */
	public String toXMLString(Application app) {
		try {
			StringWriter xmlSW = new StringWriter();
			Marshaller marshaller = jCxt.createMarshaller();
			marshaller.marshal(app, xmlSW);
			return xmlSW.toString() + "\n";
		} catch (JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create an application structure from a string.
	 *
	 * @param xml the XML string
	 * @return the applications structure
	 * @throws javax.xml.bind.JAXBException
	 */
	public Application fromXML(String xml) throws JAXBException {
		return fromXML(new ByteArrayInputStream(xml.getBytes()));
	}

	/**
	 * Create an applications structure from a stream.
	 *
	 * @param fp is the input stream
	 * @return the applications structure
	 * @throws javax.xml.bind.JAXBException
	 */
	public Application fromXML(InputStream fp) throws JAXBException {
		Unmarshaller unmarshaller = jCxt.createUnmarshaller();
		return (Application) unmarshaller.unmarshal(fp);
	}

}
