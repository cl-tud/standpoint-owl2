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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLiteral;

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
	
	public static Document spLabelToXML(OWLAnnotation spLabel) {
		String spLabelString = "<?xml version=\"1.0\"?>\n" + spLabel.getValue().asLiteral().get().getLiteral().toString();
		System.out.println(spLabelString);
		
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