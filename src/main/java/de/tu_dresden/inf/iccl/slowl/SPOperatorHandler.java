package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/* Class specifying what happens when parser encounters start/end
 * of elements in XML file.
 * (Only stores appearing standpoint names until now.)
 */
public class SPOperatorHandler extends DefaultHandler {
	
	Set<String> spNames = new HashSet<String>();
	String spAxiomName = null;
	
	/* Integer value representing modal operator.
	 * -1 - error
	 *  0 - box
	 *  1 - diamond
	 */ 
	Integer operator = -1;
	
	/* Counter for number of <Box> and <Diamond> elements used.
	 * Should be exactly 1 at the end of the expression.
	 */
	Integer opCount = 0;
	
	private boolean bSP = false;
	private boolean bUnion = false;
	private boolean bIntersection = false;
	private boolean bMinus = false;
	
	private Pattern sp = Pattern.compile("[a-zA-Z]+\\d*");
	private Pattern ax = Pattern.compile("§[a-zA-z]+\\d*");
	
	protected boolean isSPName(String s) {
		if (s.equals("*")) {
			return true;
		} else {
			Matcher m = sp.matcher(s);
			return m.matches();
		}
	}
	
	protected boolean isSPAxiomName(String s) {
		Matcher m = ax.matcher(s);
		return m.matches();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("standpointAxiom")) {
			spAxiomName = attributes.getValue("name");
			if (spAxiomName != null) {
				if (!isSPAxiomName(spAxiomName)) {
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
			bSP = true;
			String spName = attributes.getValue("name");
			if (isSPName(spName)) {
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
			System.out.println(this + " >> Standoint axiom name: " + spAxiomName);
		} else if (qName.equalsIgnoreCase("Box") || qName.equalsIgnoreCase("Diamond")) {
			if (opCount != 1) {
				throw new SAXException("Number of <Box> or <Diamond> elements must be exactly 1.");
			} else {
				System.out.print(this + " >> Standpoint names: ");
				SPParser.printSet(spNames);
			}
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