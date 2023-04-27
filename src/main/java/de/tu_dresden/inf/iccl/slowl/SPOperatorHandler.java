package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/* Class specifying what happens when parser encounters start/end
 * of elements in a standpoint operator XML String.
 */
public class SPOperatorHandler extends DefaultHandler {
	
	Set<String> spNames = new HashSet<String>();
	String spAxiomName = null;
	
	/* Integer value representing modal operator.
	 * -1 - error
	 *  0 - box
	 *  1 - diamond
	 */ 
	int operator = -1;
	
	/* Counter for number of <Box> and <Diamond> elements used.
	 * Should be exactly 1 at the end of the expression.
	 */
	private int opCount = 0;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("standpointAxiom")) {
			spAxiomName = attributes.getValue("name");
			if (spAxiomName != null) {
				if (!Translator.isSPAxiomName(spAxiomName)) {
					spAxiomName = null;
				}
			}
		} else if (qName.equalsIgnoreCase("Box")) {
			operator = 0;
			opCount++;
		} else if (qName.equalsIgnoreCase("Diamond")) {
			operator = 1;
			opCount++;
		} else if (qName.equalsIgnoreCase("Standpoint")) {
			String spName = attributes.getValue("name");
			if (Translator.isSPName(spName)) {
				spNames.add(spName);
			} else {
				throw new SAXException("Encountered an invalid standpoint name.");
			}
		} else if (qName.equalsIgnoreCase("UNION")) {

		} else if (qName.equalsIgnoreCase("INTERSECTION")) {

		} else if (qName.equalsIgnoreCase("MINUS")) {

		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("standpointAxiom")) {
			
		} else if (qName.equalsIgnoreCase("Box") || qName.equalsIgnoreCase("Diamond")) {
			if (opCount != 1) {
				throw new SAXException("Number of <Box> or <Diamond> elements must be exactly 1.");
			}
		} else if (qName.equalsIgnoreCase("Standpoint")) {
			
		} else if (qName.equalsIgnoreCase("UNION")) {
			
		} else if (qName.equalsIgnoreCase("INTERSECTION")) {

		} else if (qName.equalsIgnoreCase("MINUS")) {
			
		}
	}
}