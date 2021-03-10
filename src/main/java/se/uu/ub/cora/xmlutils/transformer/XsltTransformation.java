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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XsltTransformation implements CoraTransformation {

	private String xslt;
	private String xsltPath;

	public XsltTransformation(String xsltPath) {
		this.xsltPath = xsltPath;
	}

	@Override
	public String transform(String inputXml) {
		this.xslt = ResourceReader.readResourceAsString(xsltPath);
		return tryToTransform(inputXml);
	}

	private String tryToTransform(String inputXml) {
		try {
			return transformXmlUsingXslt(inputXml).trim();
		} catch (Exception e) {
			throw ParseException.withMessageAndException(
					"Error transforming xml: Can not read xml: " + e.getCause(), e);
		}
	}

	private String transformXmlUsingXslt(String inputXml) throws Exception {
		Transformer transformer = generateTransformer();
		return transformUsingTransformer(inputXml, transformer);
	}

	private Transformer generateTransformer() throws Exception {
		Source xslInput = new StreamSource(new StringReader(xslt));
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		return transformerFactory.newTransformer(xslInput);
	}

	private String transformUsingTransformer(String inputXml, Transformer transformer)
			throws TransformerException {
		Source xmlSource = createSourceFromXml(inputXml);
		return transformUsingTransformerAndSource(transformer, xmlSource);
	}

	private Source createSourceFromXml(String inputXml) {
		StringReader stringReader = new StringReader(inputXml);
		return new StreamSource(stringReader);
	}

	private String transformUsingTransformerAndSource(Transformer transformer, Source xmlSource)
			throws TransformerException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Result outputResult = new StreamResult(output);
		transformer.transform(xmlSource, outputResult);
		return output.toString(StandardCharsets.UTF_8);
	}

	String getXslt() {
		return xslt;
	}

	String getXsltPath() {
		return xsltPath;
	}

	@Override
	public String transformWithParameters(String inputXml, Map<String, Object> parameters) {
		this.xslt = ResourceReader.readResourceAsString(xsltPath);
		return tryToTransformWithParameters(inputXml, parameters);
	}

	private String tryToTransformWithParameters(String inputXml, Map<String, Object> parameters) {
		try {
			Transformer transformer = createTransformerWithParameters(parameters);
			return transformUsingTransformer(inputXml, transformer).trim();
		} catch (Exception e) {
			throw ParseException.withMessageAndException(
					"Error transforming xml: Can not read xml: " + e.getCause(), e);
		}
	}

	private Transformer createTransformerWithParameters(Map<String, Object> parameters)
			throws Exception {
		Transformer transformer = generateTransformer();
		for (Entry<String, Object> parameter : parameters.entrySet()) {
			transformer.setParameter(parameter.getKey(), parameter.getValue());
		}
		return transformer;
	}
}
