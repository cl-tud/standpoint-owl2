package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomCollection;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

public class SPParser {
	
	// set of AxiomTypes whose annotation by standpointLabels is currently supported
	public static final Set<AxiomType> supportedAxiomTypes = Set.of(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES);
	
	// standpoint names recorded by the parser instance (without *)
	Set<String> spNames = new HashSet<String>();
	
	// standpoint axiom names recorded by the parser instance
	Set<String> spAxiomNames = new HashSet<String>();
	
	// map mapping axiom name to corresponding standpoint axiom
	Map<String, String> spAxiomNameMap = new HashMap<String, String>();
	
	// number of <Diamond> (or negated <Box>) elements counted by the parser instance
	int diamondCount = 0;
	
	// axiom names that count as <Diamond> if they have a <Box> operator
	private Set<String> checkAxiomNames = new HashSet<String>();
	
	private SAXParser saxParser;
	private XMLReader xmlReader;
	
	private DocumentBuilder domBuilder;

	private Transformer transformer;
	
	public SPParser() {
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			xmlReader = saxParser.getXMLReader();
			domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds recorded standpoint names and the standpoint axiom name to
	 * spNames and spAxiomNames, respectively.
	 */
	public void parseSPOperator(String spOperator) {
		SPOperatorHandler spOperatorHandler;
		try {
			spOperatorHandler = new SPOperatorHandler();
			xmlReader.setContentHandler(spOperatorHandler);
			xmlReader.parse(new InputSource(new StringReader(spOperator)));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		Set<String> names = spOperatorHandler.spNames;
		names.remove("*");
		Iterator<String> it = names.iterator();
		it.forEachRemaining(next -> spNames.add(next));
		System.out.print(this + " >> Standpoint names: ");
		Renderer.printSet(spNames);
		
		String axiomName = spOperatorHandler.spAxiomName;
		if (axiomName != null) {
			spAxiomNames.add(axiomName);
		}
		System.out.print(this + " >> Axiom names: ");
		Renderer.printSet(spAxiomNames);
	}
	
	/**
	 * Counts the number of occuring diamonds (and negated boxes) in the
	 * standpointLabels of the given ontology, and stores it in diamondCount.
	 */
	public void countDiamonds(OWLOntology ontology){
		// reset diamondCount
		diamondCount = 0;
		
		String ontologyIRI = getOntologyIRIString(ontology);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		String labelString = label.getIRI().toString();
		
		// we first count diamonds in BooleanCombinations
		Set<OWLAnnotation> ontAnnotations = ontology.getAnnotations();			// cannot give label as argument
		Set<OWLAnnotation> ontSPAnnotations = new HashSet<OWLAnnotation>();
		for (OWLAnnotation annotation : ontAnnotations) {
			// get only standpointLabel Annotations
			if (annotation.getProperty().getIRI().toString().equals(labelString)) {
				ontSPAnnotations.add(annotation);
			}
		}
		
		String xmlString = null;
		for (OWLAnnotation annotation : ontSPAnnotations) {
			xmlString = spAnnotationToXML(annotation);
			diamondCount = diamondCount + countDiamonds(xmlString);
		}
		
		// then we count diamonds in standpointLabels of Axioms
		Set<OWLAnnotation> spAnnotations = getAnnotations(ontology);
		int m;
		int n;
		int k;
		String xmlStringLower;
		for (OWLAnnotation annotation : spAnnotations) {
			xmlString = spAnnotationToXML(annotation);
			
			// count positive diamonds
			diamondCount = diamondCount + countDiamonds(xmlString);
			
			// count boxes that occur negated in some BooleanCombinations
			xmlStringLower = xmlString.toLowerCase();
			k = xmlStringLower.indexOf("<box>");
			if (k >= 0) {
				m = xmlStringLower.indexOf("\"", xmlStringLower.indexOf("<standpointaxiom name"));
				n = xmlStringLower.indexOf("\"", m + 1);
				if (checkAxiomNames.contains(xmlStringLower.substring(m + 1, n))) {
					diamondCount++;
				}
			}
		}
		
		System.out.println(this + " >> Diamonds counted: " + diamondCount);
	}
		
	/**
	 * @return number of diamonds (or negated boxes) occuring in
	 * given XML String.
	 */
	int countDiamonds(String s) {
		s = s.toLowerCase();
		
		// return variable
		int count = 0;
		
		// true if current nesting of <NOT> is even
		boolean bPositive = false;
		
		int i = s.indexOf("<not>");
		int j = s.indexOf("</not>");
		int next_i;
		int m;
		int n;
		
		// count <diamond> before first <not> (or all <diamond> if there is no <not>)
		if (i >= 0) {
			for (int k = s.indexOf("<diamond>"); (k >= 0 && k < i); k = s.indexOf("<diamond>", k + 1)) {
				count++;
			}
		} else {
			for (int k = s.indexOf("<diamond>"); (k >= 0); k = s.indexOf("<diamond>", k + 1)) {
				count++;
			}
		}
		
		// count <diamond> between first <not> and last </not>
		while (i >= 0) {
			// go through nested <NOT> and negate bPositive every time
			while (true) {
				next_i = s.indexOf("<not>", i + 1);
				if (next_i < 0 || next_i > j) break;
				bPositive = !bPositive;
				i = next_i;
			}
		
			// if number of nested <NOT> is even, then we count <Diamond>
			if (bPositive) {
				for (int k = s.indexOf("<diamond>", i); (k >= 0 && k < j); k = s.indexOf("<diamond>", k + 1)) {
					count++;
				}
			// if number of nested <NOT> is odd, then we count <Box>
			} else {
				for (int k = s.indexOf("<box>", i); (k >= 0 && k < j); k = s.indexOf("<box>", k + 1)) {
					count++;
				}
				for (int k = s.indexOf("<standpointaxiom name", i); (k >= 0 && k < j); k = s.indexOf("<standpointaxiom name", k + 1)) {
					m = s.indexOf("\"");
					n = s.indexOf("\"", m + 1);
					checkAxiomNames.add(s.substring(m + 1, n));
				}
			}
		
			// move on to next <NOT>
			i = s.indexOf("<not>", j + 1);
			j = s.indexOf("</not>", i + 1);
			bPositive = false;
		}
			
		// count <diamond> after last </not>
		j = s.lastIndexOf("</not>");
		if (j >= 0) {
			for (int k = s.indexOf("<diamond>", j); (k >= 0); k = s.indexOf("<diamond>", k + 1)) {
					count++;
				}
		}
		
		return count;
	}
	
	/**
	 * @return String array where first element is the local name of the root and the other elements the children.
	 */
	// needs testing //
	public String[] getRootAndChildElements(String xmlString) {
		Document xmlDoc;
		try {
			xmlDoc = domBuilder.parse(new InputSource(new StringReader(xmlString)));
		} catch (Exception e) {
			e.printStackTrace();
			return new String[3];
		}
		
		Node root = xmlDoc.getFirstChild();
		if (root.getNodeType() != Node.ELEMENT_NODE) {
			System.out.println(this + " >> No child elements.");
			return new String[3];
		}
		
		NodeList childList = root.getChildNodes();
		
		Node[] children = new Node[childList.getLength()];
		int length = 0;
		for (int i = 0; i < childList.getLength(); i++) {
			if (childList.item(i) == null || childList.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			children[length] = childList.item(i);
			length++;
		}
		
		String[] rootAndChildElements = new String[length + 1];
		
		rootAndChildElements[0] = ((Element) root).getTagName();
		
		StringWriter buffer;
		int idx;
		for (int i = 0; i < length; i++) {
			buffer = new StringWriter();
			try {
				transformer.transform(new DOMSource(children[i]), new StreamResult(buffer));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			rootAndChildElements[i + 1] = buffer.toString().replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("\r","").trim();
		}
		
		return rootAndChildElements;
	}
	
	public void getNames(OWLOntology ontology) {
		// reset spAxiomNames
		spAxiomNames = new HashSet<String>();
		
		// reset spNames
		spNames = new HashSet<String>();
		
		Set<OWLAnnotation> annotations = getAnnotations(ontology);
		String xmlString = null;
		Set<String> names = new HashSet<String>();
		for (OWLAnnotation annotation : annotations) {
			xmlString = spAnnotationToXML(annotation);
			SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
			xmlReader.setContentHandler(spOperatorHandler);
			
			try {
				xmlReader.parse(new InputSource(new StringReader(xmlString)));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			String axiomName = spOperatorHandler.spAxiomName;
			if (axiomName != null) {
				spAxiomNames.add(axiomName);
			}
			
			names = spOperatorHandler.spNames;
			names.remove("*");
			Iterator<String> it = names.iterator();
			it.forEachRemaining(next -> spNames.add(next));
		}
		
		System.out.print(this + " >> Standpoint names: ");
		Renderer.printSet(spNames);
		
		System.out.print(this + " >> Axiom names: ");
		Renderer.printSet(spAxiomNames);
	}
	
	public void getSPAxiomNames(OWLOntology ontology) {
		// reset spAxiomNames
		spAxiomNames = new HashSet<String>();
		
		// reset spAxiomNameMap
		spAxiomNameMap = new HashMap<String, String>();
		
		// TO DO //
		Set<OWLSubClassOfAxiom> subClassOfAxioms = getAnnotatedSubClassOfAxioms(ontology);
		Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = getAnnotatedEquivalentClassesAxioms(ontology);
		
		String axiomName;
		String axiomPart;
		String axiomPartStart;
		String axiomPartEnd;
		String axiomPartLHS;
		String axiomPartRHS;
		String operatorPart;
		String xmlString;
		SPOperatorHandler spOperatorHandler;
		for (OWLSubClassOfAxiom ax : subClassOfAxioms) {
			xmlString = spAnnotationToXML(ax.annotations().toArray(OWLAnnotation[]::new)[0], false);
			
			try {
				spOperatorHandler = getSPOperatorHandler(saxParser, ax, xmlString);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			axiomName = spOperatorHandler.spAxiomName;
			if (axiomName != null) {
				spAxiomNames.add(axiomName);
				
				operatorPart = getOperatorPart(xmlString, spOperatorHandler.operator);
			
				axiomPartStart = "<SubClassOf>\n";
				axiomPartEnd   = "</SubClassOf>\n";
				axiomPartLHS   = "<LHS>" + Renderer.writeClassExpression(ax.getSubClass()) + "</LHS>\n";
				axiomPartRHS   = "<RHS>" + Renderer.writeClassExpression(ax.getSuperClass()) + "</RHS>\n";
			
				axiomPart = axiomPartStart + axiomPartLHS + axiomPartRHS + axiomPartEnd;
				
				try {
					spAxiomNameMap.put(axiomName, operatorPart + axiomPart);
				} catch (Exception e) {
					System.out.println("Could not set value for " + axiomName + ":\n" + e.getMessage());
					continue;
				}
			}
		}
		
		Collection<OWLEquivalentClassesAxiom> pairwiseAxioms;
		OWLClassExpression[] operands;
		for (OWLEquivalentClassesAxiom ax : equivalentClassesAxioms) {
			xmlString = spAnnotationToXML(ax.annotations().toArray(OWLAnnotation[]::new)[0], false);
			
			try {
				spOperatorHandler = getSPOperatorHandler(saxParser, ax, xmlString);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			axiomName = spOperatorHandler.spAxiomName;
			if (axiomName != null) {
				spAxiomNames.add(axiomName);
				
				operatorPart = getOperatorPart(xmlString, spOperatorHandler.operator);
			
				axiomPartStart = "<EquivalentClasses>\n";
				axiomPartEnd   = "</EquivalentClasses>\n";
					
				// might need to add functionality for more than two operands in OWLEquivalentClassesAxiom
				pairwiseAxioms = ax.asPairwiseAxioms();
				if (pairwiseAxioms.size() > 1) {
					System.out.println("Encountered equivalent classes axiom with more than two operands.");
					continue;
				}
					
				operands = ax.operands().toArray(OWLClassExpression[]::new);
				axiomPartLHS = "<LHS>" + Renderer.writeClassExpression(operands[0]) + "</LHS>\n";
				axiomPartRHS = "<RHS>" + Renderer.writeClassExpression(operands[1]) + "</RHS>\n";
			
				axiomPart = axiomPartStart + axiomPartLHS + axiomPartRHS + axiomPartEnd;
				
				try {
					spAxiomNameMap.put(axiomName, operatorPart + axiomPart);
				} catch (Exception e) {
					System.out.println("Could not set value for " + axiomName + ":\n" + e.getMessage());
					continue;
				}
			}
		}			
		// END TO DO //
		
		System.out.print(this + " >> Axiom names: ");
		Renderer.printSet(spAxiomNames);
	}
	
	public void getSPAxiomNames(Set<OWLAnnotation> annotations) {
		// reset spAxiomNames
		spAxiomNames = new HashSet<String>();
		
		String xmlString = null;
		Set<String> names = new HashSet<String>();
		for (OWLAnnotation annotation : annotations) {
			xmlString = spAnnotationToXML(annotation);
			SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
			xmlReader.setContentHandler(spOperatorHandler);
			
			try {
				xmlReader.parse(new InputSource(new StringReader(xmlString)));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			String axiomName = spOperatorHandler.spAxiomName;
			if (axiomName != null) {
				spAxiomNames.add(axiomName);
			}
		}
		
		System.out.print(this + " >> Axiom names: ");
		Renderer.printSet(spAxiomNames);
	}
	
	public String getFirstSPName(String spExpr) {
		String spExprLower = spExpr.toLowerCase();
		
		int start = spExprLower.indexOf("<standpoint ");
		if (start < 0) {
			return null;
		}
		start = spExprLower.indexOf("name", start);
		if (start < 0) {
			return null;
		}
		start = spExprLower.indexOf("=", start);
		if (start < 0) {
			return null;
		}
		start = spExprLower.indexOf("\"", start);
		if (start < 0) {
			return null;
		}
		int end = spExprLower.indexOf("\"", start + 1);
		if (end < 0) {
			return null;
		}
		
		return spExpr.substring(start, end).replace("\"", " ").trim();
	}
	
	public void getSPNames(OWLOntology ontology) {
		// reset spNames
		spNames = new HashSet<String>();
		
		Set<OWLAnnotation> annotations = getAnnotations(ontology);
		String xmlString = null;
		Set<String> names = new HashSet<String>();
		for (OWLAnnotation annotation : annotations) {
			xmlString = spAnnotationToXML(annotation);
			SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
			xmlReader.setContentHandler(spOperatorHandler);
			
			try {
				xmlReader.parse(new InputSource(new StringReader(xmlString)));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			names = spOperatorHandler.spNames;
			names.remove("*");
			Iterator<String> it = names.iterator();
			it.forEachRemaining(next -> spNames.add(next));
		}
		
		System.out.print(this + " >> Standpoint names: ");
		Renderer.printSet(spNames);
	}
	
	public void getSPNames(Set<OWLAnnotation> annotations) {
		// reset spNames
		spNames = new HashSet<String>();
		
		String xmlString = null;
		Set<String> names = new HashSet<String>();
		for (OWLAnnotation annotation : annotations) {
			xmlString = spAnnotationToXML(annotation);
			SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
			xmlReader.setContentHandler(spOperatorHandler);
			
			try {
				xmlReader.parse(new InputSource(new StringReader(xmlString)));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			names = spOperatorHandler.spNames;
			names.remove("*");
			Iterator<String> it = names.iterator();
			it.forEachRemaining(next -> spNames.add(next));
		}
		
		System.out.print(this + " >> Standpoint names: ");
		Renderer.printSet(spNames);
	}


	// PUBLIC STATIC METHODS //
	
	/**
	 * @return the Set of all axiom that are annotated by the
	 * standpointLabel annotation property (returned axioms are generic OWLAxioms).
	 */
	 /*
	public static Set<OWLAxiom> getAnnotatedAxioms(OWLOntology ontology, AxiomType axiomType) {
		if (axiomType == AxiomType.SUBCLASS_OF) {
			return getAnnotatedSubClassOfAxioms(ontology);
		} else if (axiomType == AxiomType.EQUIVALENT_CLASSES) {
			return getAnnotatedEquivalentClassesAxioms(ontology);
		} else {
			System.out.println("Standpoint labels on axiom type " + axiomType + " not supported.");
			return new HashSet<OWLAxiom>();
		}
	}*/
	
	/*
	public static Set<OWLAxiom> getAnnotatedAxioms(OWLOntology ontology) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (AxiomType t : supportedAxiomTypes) {
			try {
				axioms.addAll(getAnnotatedAxioms(ontology, t));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return axioms;
	}*/
	
	public static Set<OWLEquivalentClassesAxiom> getAnnotatedEquivalentClassesAxioms(OWLOntology ontology) {
		String ontologyIRI = getOntologyIRIString(ontology);
		if (ontologyIRI.equals(null)) {
			return new HashSet<OWLEquivalentClassesAxiom>();
		}
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		Set<OWLEquivalentClassesAxiom> axiomSet = new HashSet<OWLEquivalentClassesAxiom>();
						
		Set<OWLAnnotation> annotations;
		OWLEquivalentClassesAxiom axiom;
		for (OWLEquivalentClassesAxiom a : ontology.axioms(AxiomType.EQUIVALENT_CLASSES).toArray(OWLEquivalentClassesAxiom[]::new)) {
			annotations = a.getAnnotations(label);
			if (!annotations.isEmpty()) {
				if (annotations.size() > 1) {
					System.out.println("Multiple standpointLabels for " + a + ".");
					continue;
				}
				axiom = a.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);
				axiomSet.add(axiom);
			}
		}
		
		return axiomSet;
	}
	
	public static Set<OWLSubClassOfAxiom> getAnnotatedSubClassOfAxioms(OWLOntology ontology) {
		String ontologyIRI = getOntologyIRIString(ontology);
		if (ontologyIRI.equals(null)) {
			return new HashSet<OWLSubClassOfAxiom>();
		}
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		Set<OWLSubClassOfAxiom> axiomSet = new HashSet<OWLSubClassOfAxiom>();
						
		Set<OWLAnnotation> annotations;
		OWLSubClassOfAxiom axiom;
		for (OWLSubClassOfAxiom a : ontology.axioms(AxiomType.SUBCLASS_OF).toArray(OWLSubClassOfAxiom[]::new)) {
			annotations = a.getAnnotations(label);
			if (!annotations.isEmpty()) {
				if (annotations.size() > 1) {
					System.out.println("Multiple standpointLabels for " + a + ".");
					continue;
				}
				axiom = a.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);
				axiomSet.add(axiom);
			}
		}
		
		return axiomSet;
	}
	
	/**
	 * @return the Set of all those OWLAnnotations in given ontology
	 * that annotate the given AxiomType and whose annotation property
	 * is equal to the standpointLabel annotation property.
	 */
	public static Set<OWLAnnotation> getAnnotations(OWLOntology ontology, AxiomType axiomType) {
		String ontologyIRI = getOntologyIRIString(ontology);
		if (ontologyIRI.equals(null)) {
			return new HashSet<OWLAnnotation>();
		}
				
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		Set<OWLAnnotation> annotationSet = new HashSet<OWLAnnotation>();		
		
		OWLAnnotation next = null;
		if (axiomType == AxiomType.SUBCLASS_OF) {
			for (OWLSubClassOfAxiom ax : ontology.getAxioms(AxiomType.SUBCLASS_OF, Imports.EXCLUDED)) {
				Set<OWLAnnotation> annotations = ax.getAnnotations(label);
				Iterator<OWLAnnotation> it = annotations.iterator();
				try {
					next = it.next();
				} catch (NoSuchElementException e) {
					//e.printStackTrace();
					continue;
				}
				if (next != null) {
					annotationSet.add(next);
				}
			}
		} else if (axiomType == AxiomType.EQUIVALENT_CLASSES) {
			for (OWLEquivalentClassesAxiom ax : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.EXCLUDED)) {
				Set<OWLAnnotation> annotations = ax.getAnnotations(label);
				// maybe change following to short form
				Iterator<OWLAnnotation> it = annotations.iterator();
				try {
					next = it.next();
				} catch (NoSuchElementException e) {
					continue;
				}
				if (next != null) {
					annotationSet.add(next);
				}
			}
		} else {
			System.out.println("Standpoint labels on axiom type " + axiomType + " not supported.");
		}
		
		return annotationSet;	
	}
	
	public static Set<OWLAnnotation> getAnnotations(OWLOntology ontology) {
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		for (OWLAnnotation a : getAnnotations(ontology, AxiomType.SUBCLASS_OF)) {
			annotations.add(a);
		}
		for (OWLAnnotation a : getAnnotations(ontology, AxiomType.EQUIVALENT_CLASSES)) {
			annotations.add(a);
		} 
		return annotations;
	}
	
	/**
	 * @return the IRI of the given ontology as a String,
	 * or null if ontology is null or anonymous.
	 */
	public static String getOntologyIRIString(OWLOntology ontology) {
		String ontologyIRI = "";
		if (ontology == null) {
			System.out.println("SPParser >> Received null ontology.");
			return null;
		} else if (ontology.isIRI()) {
			ontologyIRI = ontology.toString();
		} else {
			Optional<IRI> ontologyIRIOptional = ontology.getOntologyID().getOntologyIRI();
			if (ontologyIRIOptional.isPresent()) {
				ontologyIRI = ontologyIRIOptional.get().toString();
			} else {
				System.out.println("SPParser >> Received anonymous ontology.");
				return null;
			}
		}
		return ontologyIRI;
	}
	
	/**
	 * @return an XML String to be parsed.
	 */	
	public static String spAnnotationToXML(OWLAnnotation spLabel, boolean bEncoding) {
		String encoding = "";
		if (bEncoding) {
			encoding = "<?xml version=\"1.0\"?>\n";
		}
		
		OWLAnnotationValue value = spLabel.getValue();
		
		String spLabelString = "";
		if (value.isLiteral()) {
			spLabelString = encoding + value.asLiteral().get().getLiteral().toString();
			return spLabelString;
		} else {
			System.out.println("Value of " + spLabel + " is not a literal.");
			return null;
		}
	}
	
	public static String spAnnotationToXML(OWLAnnotation spLabel) {
		return spAnnotationToXML(spLabel, true);
	}
	
	
	// PRIVATE METHODS //
	
	/**
	 * Helper method for getSPAxiomNames.
	 */
	private static String getOperatorPart(String xmlString, int operator) {		
		int operatorStart = -1;
		int operatorEnd = -1;
		// offset to include endElement in substring
		int offset = 0;
		String xmlStringLower = xmlString.toLowerCase();
	
		if (operator == 0) {
			operatorStart = xmlStringLower.indexOf("<box>");
			operatorEnd = xmlStringLower.indexOf("</box>");
			offset = 7;
		} else if (operator == 1) {
			operatorStart = xmlStringLower.indexOf("<diamond>");
			operatorEnd = xmlStringLower.indexOf("</diamond>");
			offset = 11;
		} else {
			System.out.println("The axiom has no modal operator.");
			// do sth?
			return null;
		}
		return xmlString.substring(operatorStart, operatorEnd + offset);
	}
	
	/**
	 * Helper method for getSPAxiomNames.
	 */
	private static SPOperatorHandler getSPOperatorHandler(SAXParser saxParser, OWLAxiom axiom, String xmlString) throws Exception {
		SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
		try{
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(spOperatorHandler);
			xmlReader.parse(new InputSource(new StringReader(xmlString)));
		} catch (Exception e) {
			throw e;
		}
		return spOperatorHandler;
	}
	
	/**
	 * @return true if annotation acts along the standpointLabel annotation property
	 * of given ontology.
	 */
	private static boolean isStandpointLabel(OWLOntology ontology, OWLAnnotation annotation) {
		final String label = getOntologyIRIString(ontology) + "#standpointLabel";
		
		String annotationLabel;
		try {
			annotationLabel = annotation.getProperty().getIRI().toString();
		} catch (Exception e) {
			return false;
		}
		
		return label.equals(annotationLabel);
	}
	
	private static boolean isStandpointLabel(OWLOntology ontology, OWLAnnotationAssertionAxiom annotationAxiom) {
		return isStandpointLabel(ontology, annotationAxiom.getAnnotation());
	}
}