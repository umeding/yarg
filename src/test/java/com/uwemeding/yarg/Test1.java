/*
 * Copyright (c) 2015 Meding Software Technik - All Rights Reserved.
 */
package com.uwemeding.yarg;

import com.uwemeding.yarg.bindings.Application;
import java.io.File;
import java.io.FileInputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author uwe
 */
public class Test1 {

	public Test1() {
	}

	@BeforeClass
	public static void setUpClass() {
//		System.setProperty("javax.xml.accessExternalDTD", "file, http");
		System.setProperty("javax.xml.accessExternalDTD", "all");
	}

	@AfterClass
	public static void tearDownClass() {
		System.clearProperty("javax.xml.accessExternalSchema");
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void readDesc() throws Throwable {
		// Read an XML description
		File file = new File("src/test/resources/Test1.xml");
		try (FileInputStream fp = new FileInputStream(file)) {
			Application app = new RestDescBuilder().fromXML(fp);

			// Convert the description back to xml
			new RestDescBuilder().toXMLString(app);
		}
	}


}
