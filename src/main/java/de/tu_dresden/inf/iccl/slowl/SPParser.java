package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.StringReader;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import org.w3c.dom.Document;

import org.semanticweb.owlapi.model.IRI;

public class SPParser {
	/**
	 * @return Returns the HashSet of standpoint names recorded by the handler
	 * when parsing a standpoint expression.
	 */
	public static HashSet<String> parseSPExpression(File f) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			SPExpressionHandler spExpressionHandler = new SPExpressionHandler();
			saxParser.parse(f, spExpressionHandler);
			HashSet<String> spNames = spExpressionHandler.spNames;
			return spNames;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<String>();
		}
	}
	
	public static Document spLabelToXML(IRI spLabel) {
		
		// TO DO: conversion of annotation property to XML string //
		String spLabelString = "<?version=\"1.0\"?>\n" + spLabel.toString();
		System.out.println(spLabelString); // ?
		// END TO DO //
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(spLabelString)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}