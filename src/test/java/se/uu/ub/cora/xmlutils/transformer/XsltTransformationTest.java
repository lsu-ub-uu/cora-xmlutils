/*
 * Copyright 2021 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.xmlutils.transformer;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XsltTransformationTest {

	private static final String XSLT_FEDORA_TO_CORA_PLACE = "xslt/FedoraToCoraPlaceExample.xsl";
	private static final String XML_FEDORA_PLACE = "place/xmlFedoraPlace_679.xml";
	private static final String XML_CORA_PLACE = "place/xmlCoraPlace_679.xml";
	private XsltTransformation xsltTransformation;

	@BeforeMethod
	public void setUp() {
		xsltTransformation = new XsltTransformation(XSLT_FEDORA_TO_CORA_PLACE);
	}

	@Test
	public void testInitWithXsltPath() throws Exception {
		String usedXsltPath = xsltTransformation.getXsltPath();
		assertEquals(usedXsltPath, XSLT_FEDORA_TO_CORA_PLACE);
	}

	@Test
	public void testTransformSetsAttributesOnFactory() throws Exception {
		System.setProperty("javax.xml.transform.TransformerFactory",
				"se.uu.ub.cora.xmlutils.transformer.TransformerFactorySpy");
		String inputXml = ResourceReader.readResourceAsString(XML_FEDORA_PLACE);
		xsltTransformation.transform(inputXml);

		String xsltFedoraToCoraPlace = ResourceReader
				.readResourceAsString(XSLT_FEDORA_TO_CORA_PLACE);
		assertEquals(xsltTransformation.getXslt(), xsltFedoraToCoraPlace);

		TransformerFactorySpy transformerFactorySpy = TransformerFactorySpy.factory;
		assertEquals(transformerFactorySpy.attributes.get(XMLConstants.ACCESS_EXTERNAL_DTD), "");
		assertEquals(transformerFactorySpy.attributes.get(XMLConstants.ACCESS_EXTERNAL_STYLESHEET),
				"");
		System.clearProperty("javax.xml.transform.TransformerFactory");
	}

	@Test
	public void testSimpleTransformation() throws Exception {
		String inputXml = ResourceReader.readResourceAsString(XML_FEDORA_PLACE);
		String outputXml = xsltTransformation.transform(inputXml);

		String expectedOutput = ResourceReader.readResourceAsString(XML_CORA_PLACE);
		assertEquals(outputXml, expectedOutput);
	}

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error transforming xml: Can not read xml: "
			+ "javax.xml.transform.TransformerException: "
			+ "com.sun.org.apache.xml.internal.utils.WrappedRuntimeException: "
			+ "The element type \"pid\" must be terminated by the matching end-tag \"</pid>\".")
	public void parseExceptionShouldBeThrownOnMalformedXML() throws Exception {
		String inputXml = "<pid></notPid>";
		xsltTransformation.transform(inputXml);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to read resource to string for file: path/not/found.xls")
	public void testExceptionThrownCannotReadXsltFile() throws Exception {
		XsltTransformation xsltTransformation2 = new XsltTransformation("path/not/found.xls");
		xsltTransformation2.transform("someXml");
	}

	@Test
	public void testSimpleTransformationWithParametersCheckParameters() throws Exception {
		System.setProperty("javax.xml.transform.TransformerFactory",
				"se.uu.ub.cora.xmlutils.transformer.TransformerFactorySpy");
		String inputXml = ResourceReader.readResourceAsString(XML_FEDORA_PLACE);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("domain", "uu");
		parameters.put("someIntegerParameter", 45);

		xsltTransformation.transformWithParameters(inputXml, parameters);

		TransformerFactorySpy transformerFactorySpy = TransformerFactorySpy.factory;
		TransformerSpy transformerSpy = transformerFactorySpy.transformers.get(0);

		assertEquals(transformerSpy.parameters.get("domain"), "uu");
		assertEquals(transformerSpy.parameters.get("someIntegerParameter"), 45);

	}

	@Test
	public void testSimpleTransformationWithParametersCheckResult() throws Exception {
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		String inputXml = ResourceReader.readResourceAsString(XML_FEDORA_PLACE);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("domain", "uu");
		parameters.put("someIntegerParameter", 45);

		String outputXml = xsltTransformation.transformWithParameters(inputXml, parameters);

		String expectedOutput = ResourceReader.readResourceAsString(XML_CORA_PLACE);
		assertEquals(outputXml, expectedOutput);
	}

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error transforming xml: Can not read xml: "
			+ "javax.xml.transform.TransformerException: "
			+ "com.sun.org.apache.xml.internal.utils.WrappedRuntimeException: "
			+ "The element type \"pid\" must be terminated by the matching end-tag \"</pid>\".")
	public void parseExceptionShouldBeThrownOnMalformedXMLForTransforWithParameters()
			throws Exception {
		String inputXml = "<pid></notPid>";
		Map<String, Object> parameters = new HashMap<>();
		xsltTransformation.transformWithParameters(inputXml, parameters);
	}

}
