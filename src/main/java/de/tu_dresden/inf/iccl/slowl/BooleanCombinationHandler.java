package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// * should use SPOperatorHandler and the OWLAPI parser for complex concepts (somehow)

// * we do not allow nested standpoint operators
// * named standpointAxioms must be defined outside of the boolean combination

public class BooleanCombinationHandler extends DefaultHandler {
	
	/* Stack to keep track of the nesting of the XML elements.
	 * -1 - root (saves us additional emptiness checking)
	 * 0  - NOT
	 * 1  - AND
	 * 2  - OR
	 * 3  - LHS
	 * 4  - RHS
	 * 5  - EquivalentClasses
	 * 6  - SubClassOf
	 */
	private Stack<Integer> elements = new Stack<Integer>();
	
	// counters for LHS, RHS
	private int ls = 0;
	private int rs = 0;
	
	// true when there occurs an unnamed standpointAxiom
	private boolean bSPAxiom = false;
	
	// set of defined axiom names initialised by constructor
	private Set<String> defAxiomNames = new HashSet<String>();
	
	public BooleanCombinationHandler(Set<String> spAxiomNames) {
		defAxiomNames = spAxiomNames;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("booleanCombination")) {
			elements.push(-1);
		} else if (qName.equalsIgnoreCase("NOT")) {
			elements.push(0);
		} else if (qName.equalsIgnoreCase("AND")) {
			elements.push(1);
		} else if (qName.equalsIgnoreCase("OR")) {
			elements.push(2);
		} else if (qName.equalsIgnoreCase("LHS")) {
			// check that top element is an OWLAxiom
			if (elements.peek() < 5) {
				throw new SAXException("Encountered <LHS> in unexpected location.");
			// check that there has not already been another <LHS> for the same parent element
			} else if (ls > 0) {
				throw new SAXException("There can only be one <LHS> for an axiom.");
			}
			elements.push(3);
			ls = ls + 1;
		} else if (qName.equalsIgnoreCase("RHS")) {
			// check that top element is an OWLAxiom
			if (elements.peek() < 5) {
				throw new SAXException("Encountered <RHS> in unexpected location.");
			// check that there has not already been another <RHS> for the same parent element 
			} else if (rs > 0) {
				throw new SAXException("There can only be one <RHS> for an axiom.");
			}
			elements.push(4);
			rs = rs + 1;
		} else if (qName.equalsIgnoreCase("EquivalentClasses")) {
			elements.push(5);
		} else if (qName.equalsIgnoreCase("SubClassOf")) {
			elements.push(6);
		} else if (qName.equalsIgnoreCase("standpointAxiom")) {
			String spAxiomName = attributes.getValue("name");
			if (spAxiomName != null) {
				// check if there really is a standpoint axiom with that name
				if (defAxiomNames.contains(spAxiomName)) {
					// TO DO //
				} else {
					throw new SAXException("There is no axiom with name \"" + spAxiomName + "\".");
				}
			}
		} else if (qName.equalsIgnoreCase("Box") || qName.equalsIgnoreCase("Diamond")) {
			// use SPOperatorHandler
			// TO DO //
		}
		
	}
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("booleanCombination")) {
			if (elements.peek() != -1) {
				throw new SAXException("Parsing ended with non-empty stack: " + elements);
			}
			
		} else if (qName.equalsIgnoreCase("NOT")) {
			
			elements.pop();
		} else if (qName.equalsIgnoreCase("AND")) {
			
			elements.pop();
		} else if (qName.equalsIgnoreCase("OR")) {
			
			elements.pop();
		} else if (qName.equalsIgnoreCase("LHS")) {
			
			elements.pop();
		} else if (qName.equalsIgnoreCase("RHS")) {
			
			elements.pop();
		} else if (qName.equalsIgnoreCase("EquivalentClasses")) {
			if (ls != 1 || rs != 1) {
				throw new SAXException("Missing <LHS> or <RHS>.");
			}
			
			ls = 0;
			rs = 0;
			elements.pop();
		} else if (qName.equalsIgnoreCase("SubClassOf")) {
			if (ls != 1 || rs != 1) {
				throw new SAXException("Missing <LHS> or <RHS>.");
			}
			
			ls = 0;
			rs = 0;
			elements.pop();
		} else if (qName.equalsIgnoreCase("standpointAxiom")) {
			
		}
	}
}