package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/* Class specifying what happens when parser encounters start/end
 * of elements in XML file.
 * (Only stores appearing standpoint names until now.)
 */
public class SPExpressionHandler extends DefaultHandler {
	
	HashSet<String> spNames = new HashSet<String>();
	
	boolean bSP = false;
	boolean bUnion = false;
	boolean bIntersection = false;
	boolean bMinus = false;
	
	Pattern sp = Pattern.compile("[a-zA-Z]+\\d*");
	
	protected boolean isSPName(String s) {
		if (s.equals("*")) {
			return true;
		} else {
			Matcher m = sp.matcher(s);
			return m.matches();
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("Standpoint")) {
			bSP = true;
			String spName = attributes.getValue("name");
			if (isSPName(spName)) {
				System.out.println("Valid standpoint name "+spName+" encountered.");
				spNames.add(spName);
			} else {
				throw new IllegalArgumentException("Encountered an invalid standpoint name.");
			}
		} else if (qName.equalsIgnoreCase("UNION")) {
			bUnion = true;
		} else if (qName.equalsIgnoreCase("INTERSECTION")) {
			bIntersection = true;
		} else if (qName.equalsIgnoreCase("MINUS")) {
			bMinus = true;
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equalsIgnoreCase("standpointAxiom")) {
			
			System.out.print("Standpoint names: <[");
			int k = 1;
			for (String i : spNames) { 
				System.out.print(i);
				if (k++ < spNames.size()) {
					System.out.print(", ");
				}
			}
			System.out.println("]>");
		} else if (bSP) {
			
			bSP = false;
		} else if (bUnion) {
			
			bUnion = false;
		} else if (bIntersection) {
			
			bIntersection = false;
		} else if (bMinus) {
			
			bMinus = false;
		}
		
	}
}