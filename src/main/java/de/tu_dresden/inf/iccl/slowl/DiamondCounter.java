package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DiamondCounter extends DefaultHandler {
	
	int count = 0;
	
	Set<String> checkAxiomNames = new HashSet<String>();
	
	private boolean bPositive = true;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("NOT")) {
			bPositive = !bPositive;
		} else if (qName.equalsIgnoreCase("Diamond") && bPositive) {
			count++;
		} else if (qName.equalsIgnoreCase("Box") && !bPositive) {
			count++;
		} else if (qName.equalsIgnoreCase("standpointAxiom") && !bPositive) {
			String spAxiomName = attributes.getValue("name");
			if (spAxiomName != null && Translator.isSPAxiomName(spAxiomName)) {
				checkAxiomNames.add(spAxiomName);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("not")) {
			bPositive = !bPositive;
		}
	}
}