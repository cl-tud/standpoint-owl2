package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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
	// standpoint names recorded by the parser instance (without *)
	Set<String> spNames = new HashSet<String>();
	
	// standpoint axiom names recorded by the parser instance
	Set<String> spAxiomNames = new HashSet<String>();
	
	// map mapping axiom name to corresponding standpoint axiom
	Map<String, String> spAxiomNameMap = new HashMap<String, String>();
	
	// set of ontology axioms as String
	Set<String> spAxioms = new HashSet<String>();
	
	// array of booleanCombinations and sharpening statements as String
	String[] generalSPStatements;
	
	// set of standard (non-standpoint) OWLAxioms
	Set<OWLAxiom> standardAxioms = new HashSet<OWLAxiom>();
	
	// number of <Diamond> (or negated <Box>) elements counted by the parser instance
	int diamondCount = 0;
	
	// axiom names that count as <Diamond> if they have a <Box> operator
	private Set<String> checkAxiomNames = new HashSet<String>();
	
	private SAXParser saxParser;
	private XMLReader xmlReader;
	
	private Renderer renderer = new Renderer();
	
	private DocumentBuilder domBuilder;

	private Transformer transformer;
	
	// true if warnings and skip information should be printed
	private boolean bVerbose;
	
	private boolean bInit = false;
	private OWLOntology initOntology = null;
	private OWLOntologyManager initManager;
	private OWLDataFactory initDataFactory;
	private String initOntologyIRIString;
	private OWLAnnotationProperty initLabel;
	
	public SPParser(boolean verbose) {
		bVerbose = verbose;
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			xmlReader = saxParser.getXMLReader();
			domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (SAXException e) {
			if (bVerbose) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			if (bVerbose) {
				e.printStackTrace();
			}
		} catch (TransformerConfigurationException e) {
			if (bVerbose) {
				e.printStackTrace();
			}
		}
	}
	
	public SPParser() {
		this(true);
	}
	
	public SPParser(OWLOntology ontology, boolean verbose) {
		bVerbose = verbose;
		
		bInit = true;
		initOntology = ontology;
		initManager = initOntology.getOWLOntologyManager();
		initDataFactory = initManager.getOWLDataFactory();
		initOntologyIRIString = getOntologyIRIString(initOntology);
		initLabel = initDataFactory.getOWLAnnotationProperty(IRI.create(initOntologyIRIString + "#standpointLabel"));
		
		generalSPStatements = initOntology.annotations().filter(a -> (a.getProperty().compareTo(initLabel) == 0)).map(a -> spAnnotationToXML(a, false)).toArray(String[]::new);
		
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			xmlReader = saxParser.getXMLReader();
			domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (SAXException e) {
			if (bVerbose) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			if (bVerbose) {
				e.printStackTrace();
			}
		} catch (TransformerConfigurationException e) {
			if (bVerbose) {
				e.printStackTrace();
			}
		}
	}
	
	public SPParser(OWLOntology ontology) {
		this(ontology, true);
	}
	
	/**
	 * Counts the number of occuring diamonds (and negated boxes) in the
	 * standpointLabels of the given ontology, and stores it in diamondCount.
	 */
	public void countDiamonds(OWLOntology ontology) {
		// reset diamondCount
		diamondCount = 0;
		
		String ontologyIRI = getOntologyIRIString(ontology);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		// we first count diamonds in BooleanCombinations
		for (String s : generalSPStatements) {
			try {
				diamondCount = diamondCount + countDiamonds(s);
			} catch (IllegalArgumentException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not count diamonds in boolean combination. " + e.getMessage());
				}
				continue;
			} catch (SAXException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not count diamonds in boolean combination. " + e.getMessage());
				}
				continue;
			}
		}
		
		// then we count diamonds in standpointLabels of Axioms
		Set<OWLAnnotation> spAnnotations = getAnnotations(ontology);
		int k;
		String xmlString;
		String name;
		for (OWLAnnotation annotation : spAnnotations) {
			try {
				xmlString = spAnnotationToXML(annotation); // throws IllegalArgumentException
			} catch (IllegalArgumentException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not count diamonds in standpointLabel annotation. " + e.getMessage());
				}
				continue;
			}
			
			// count positive diamonds and negated boxes
			try {
				diamondCount = diamondCount + countDiamonds(xmlString);
			} catch (IllegalArgumentException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not count diamonds in standpointLabel annotation. " + e.getMessage());
				}
				continue;
			} catch (SAXException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not count diamonds in standpointLabel annotation. " + e.getMessage());
				}
				continue;
			}
			
			// count box operator of each named standpointAxiom which occured in a booleanCombination
			k = xmlString.toLowerCase().indexOf("<box>");
			if (k >= 0) {
				name = getFirstSPAxiomName(xmlString);
				if (name != null && checkAxiomNames.contains(name)) {
					diamondCount++;
				}
			}
		}
	}
	
	public void countDiamonds() throws IllegalArgumentException {
		if (bInit) {
			countDiamonds(initOntology); // could replace manager etc. in method
		} else {
			throw new IllegalArgumentException("Use method countDiamonds(OWLOntology) if parser has not been initialised with an OWLOntology.");
		}
	}
	
	int countDiamonds(String xmlString) throws IllegalArgumentException, SAXException {
		DiamondCounter diamondCounter = new DiamondCounter();
		XMLReader xmlReader = saxParser.getXMLReader(); // throws SAXException
		xmlReader.setContentHandler(diamondCounter);
		try {
			xmlReader.parse(new InputSource(new StringReader(xmlString))); // throws SAXException
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not count diamonds in String. " + e.getMessage());
		}
		checkAxiomNames.addAll(diamondCounter.checkAxiomNames);
		return diamondCounter.count;
	}
	
	/**
	 * @return String array where first element is the local name of the root and the other elements the children.
	 */
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
			if (bVerbose) {
				System.out.println(this + " >> No child elements.");
			}
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
	
	/**
	 * Goes through all axioms of the given ontology, records unannotated axiom in standardAxioms,
	 * and annotated axioms as their String representation in spAxioms.
	 * The names of standpointAxioms are recorded in spAxiomNames, and the mapping of standpoint axiom names
	 * to the corresponding standpoint axiom is recorded in spAxiomNameMap.
	 * Occuring standpoint names are recorded in spNames.
	 */
	public void parseAxioms(OWLOntology ontology) {
		// reset spAxiomNames
		spAxiomNames = new HashSet<String>();
		
		// reset spAxiomNameMap
		spAxiomNameMap = new HashMap<String, String>();
		
		// reset spNames
		spNames = new HashSet<String>();
		
		// reset spAxioms
		spAxioms = new HashSet<String>();
		
		// reset standardAxioms
		standardAxioms = new HashSet<OWLAxiom>();
		
		//Set<OWLSubClassOfAxiom> subClassOfAxioms = getAnnotatedSubClassOfAxioms(ontology);
		//Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = getAnnotatedEquivalentClassesAxioms(ontology);
		
		OWLSubClassOfAxiom[] subClassOfAxioms = ontology.axioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED).toArray(OWLSubClassOfAxiom[]::new);
		OWLEquivalentClassesAxiom[] equivalentClassesAxioms = ontology.axioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED).toArray(OWLEquivalentClassesAxiom[]::new);
		OWLAxiom[] aBoxAxioms = ontology.aboxAxioms(Imports.INCLUDED).toArray(OWLAxiom[]::new);
		
		Predicate<OWLAnnotation> isSPLabel = a -> isStandpointLabel(ontology, a);
		
		String axiomName;
		String axiomPart;
		String axiomPartStart;
		String axiomPartEnd;
		String axiomPartLHS;
		String axiomPartRHS;
		String operatorPart;
		String xmlString;
		OWLAnnotation[] annotations;
		SPOperatorHandler spOperatorHandler;
		
		for (OWLSubClassOfAxiom ax : subClassOfAxioms) {
			axiomPartStart = "<SubClassOf>\n";
			axiomPartEnd   = "</SubClassOf>\n";
			
			try {
				axiomPartLHS   = "<LHS>" + renderer.writeClassExpression(ax.getSubClass()) + "</LHS>\n";
				axiomPartRHS   = "<RHS>" + renderer.writeClassExpression(ax.getSuperClass()) + "</RHS>\n";
			} catch (IllegalArgumentException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not write ClassExpression. " + e.getMessage());
				}
				continue;
			}
			axiomPart = axiomPartStart + axiomPartLHS + axiomPartRHS + axiomPartEnd;
			
			annotations = ax.annotations().filter(isSPLabel).toArray(OWLAnnotation[]::new);
			if (annotations.length > 0) {
				if (bVerbose && annotations.length > 1) {
					System.out.println(this + " >> WARNING: Of multiple standpointLabel annotations for SubClassOfAxiom, just one will be considered.");
				}
				xmlString = spAnnotationToXML(annotations[0], false);
				if (xmlString.equals("")) {
					if (bVerbose) {
						System.out.println(this + " >> SKIP: standpointLabel of SubClassOfAxiom could not be parsed.");
					}
					continue;
				}
				
				try {
					spOperatorHandler = getSPOperatorHandler(saxParser, xmlString);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
				operatorPart = getOperatorPart(xmlString, spOperatorHandler.operator, axiomPart);
				
				spNames.addAll(spOperatorHandler.spNames);
				
				axiomName = spOperatorHandler.spAxiomName;
				if (axiomName != null) {
					spAxiomNames.add(axiomName);
					spAxiomNameMap.put(axiomName, operatorPart);
				} else { // do not translate named standpoint axioms outside booleanCombinations
					spAxioms.add(operatorPart);
				}
			} else { // normal SubClassOfAxiom without standpointLabel
				standardAxioms.add(ax.getAxiomWithoutAnnotations());
			}
		}
		
		Collection<OWLEquivalentClassesAxiom> pairwiseAxioms;
		OWLClassExpression[] operands;
		for (OWLEquivalentClassesAxiom ax : equivalentClassesAxioms) {
			axiomPartStart = "<EquivalentClasses>\n";
			axiomPartEnd   = "</EquivalentClasses>\n";
				
			// might want to add functionality for more than two operands in OWLEquivalentClassesAxiom
			pairwiseAxioms = ax.asPairwiseAxioms();
			if (pairwiseAxioms.size() > 1) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Encountered equivalent classes axiom with more than two operands.");
				}
				continue;
			}
				
			operands = ax.operands().toArray(OWLClassExpression[]::new);
			try {
				axiomPartLHS = "<LHS>" + renderer.writeClassExpression(operands[0]) + "</LHS>\n";
				axiomPartRHS = "<RHS>" + renderer.writeClassExpression(operands[1]) + "</RHS>\n";
			} catch (IllegalArgumentException e) {
				if (bVerbose) {
					System.out.println(this + " >> SKIP: Could not write OWLClassExpression. " + e.getMessage());
				}
				continue;
			}
		
			axiomPart = axiomPartStart + axiomPartLHS + axiomPartRHS + axiomPartEnd;
			
			annotations = ax.annotations().filter(isSPLabel).toArray(OWLAnnotation[]::new);
			if (annotations.length > 0) {
				xmlString = spAnnotationToXML(annotations[0], false);
				if (xmlString.equals("")) {
					if (bVerbose) {
						System.out.println(this + " >> SKIP: standpointLabel of EquivalentClassesAxiom could not be parsed.");
					}
					continue;
				}
			
				try {
					spOperatorHandler = getSPOperatorHandler(saxParser, xmlString);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
					
				operatorPart = getOperatorPart(xmlString, spOperatorHandler.operator, axiomPart);
				
				spNames.addAll(spOperatorHandler.spNames);
					
				axiomName = spOperatorHandler.spAxiomName;
				if (axiomName != null) {
					spAxiomNames.add(axiomName);	
					spAxiomNameMap.put(axiomName, operatorPart);
				} else { // do not translate named standpoint axioms outside booleanCombinations
					spAxioms.add(operatorPart);
				}
			} else { // normal EquivalentClassesAxiom without standpointLabel
				standardAxioms.add(ax.getAxiomWithoutAnnotations());
			}
		}
		
		AxiomType type;
		for (OWLAxiom ax : aBoxAxioms) {
			type = ax.getAxiomType();
			if (Translator.supportedABoxAxiomTypes.contains(type)) {
				standardAxioms.add(ax.getAxiomWithoutAnnotations());
			} else if (bVerbose) {
				System.out.println(this + " >> WARNING: Ontology contains axiom of a type (" + type + ") which will not be considered in the translation.");
			}
		}
		
		// remove universal standpoint (*) from set of standpoint names
		spNames.remove("*");
	}
	
	public void parseAxioms() {
		if (bInit) {
			parseAxioms(initOntology);
		} else {
			throw new IllegalArgumentException("Use method parseAxioms(OWLOntology) if parser has not been initialised with an OWLOntology.");
		}
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
	
	public String getFirstSPAxiomName(String boolComb) {
		String boolCombLower = boolComb.toLowerCase();
		
		int start = boolCombLower.indexOf("<standpointaxiom ");
		if (start < 0) {
			return null;
		}
		start = boolCombLower.indexOf("name", start);
		if (start < 0) {
			return null;
		}
		start = boolCombLower.indexOf("=", start);
		if (start < 0) {
			return null;
		}
		start = boolCombLower.indexOf("\"", start);
		if (start < 0) {
			return null;
		}
		int end = boolCombLower.indexOf("\"", start + 1);
		if (end < 0) {
			return null;
		}
		
		return boolComb.substring(start, end).replace("\"", " ").trim();
	}

	public boolean isStandpointLabel(OWLAnnotation annotation) {
		if (bInit) {
			return isStandpointLabel(initOntology, annotation);
		} else {
			throw new IllegalArgumentException("Use method isStandpointLabel(OWLOntology, OWLAnnotation) if parser has not been initialised with an OWLOntology.");
		}
	}


	// PUBLIC STATIC METHODS //
	
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
			System.err.println("SPParser >> ERROR: Standpoint labels on axiom type " + axiomType + " not supported.");
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
	
	public static String[] getBooleanCombinations(OWLOntology ontology) throws IllegalArgumentException {
		String ontologyIRI = getOntologyIRIString(ontology); // throws IllegalArgumentException
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		String labelString = label.toString();
		
		return ontology.annotations().filter(a -> (a.getProperty().compareTo(label) == 0)).map(a -> spAnnotationToXML(a, false)).toArray(String[]::new); // throws IllegalArgumentException
	}
	
	/**
	 * @return the IRI of the given ontology as a String,
	 * or null if ontology is null or anonymous.
	 */
	public static String getOntologyIRIString(OWLOntology ontology) throws IllegalArgumentException {
		String ontologyIRI = "";
		if (ontology == null) {
			throw new IllegalArgumentException("Received null ontology.");
		} else if (ontology.isIRI()) {
			ontologyIRI = ontology.toString();
		} else {
			Optional<IRI> ontologyIRIOptional = ontology.getOntologyID().getOntologyIRI();
			if (ontologyIRIOptional.isPresent()) {
				ontologyIRI = ontologyIRIOptional.get().toString();
			} else {
				throw new IllegalArgumentException("Received anonymous ontology.");
			}
		}
		return ontologyIRI;
	}
	
	/**
	 * @return an XML String to be parsed; returns empty String if annotation is empty or not a literal.
	 */	
	public static String spAnnotationToXML(OWLAnnotation spLabel, boolean bEncoding) throws IllegalArgumentException {
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
			throw new IllegalArgumentException("Value of " + spLabel + " is not a literal.");
		}
	}
	
	public static String spAnnotationToXML(OWLAnnotation spLabel) throws IllegalArgumentException {
		return spAnnotationToXML(spLabel, true); // throws IllegalArgumentException
	}

	/**
	 * @return true if annotation acts along the standpointLabel annotation property
	 * of given ontology.
	 */
	public static boolean isStandpointLabel(OWLOntology ontology, OWLAnnotation annotation) {
		final String label = getOntologyIRIString(ontology) + "#standpointLabel";
		String annotationLabel = annotation.getProperty().getIRI().toString();
		return label.equals(annotationLabel);
	}
	
	public static boolean isStandpointLabel(OWLOntology ontology, OWLAnnotationAssertionAxiom annotationAxiom) {
		return isStandpointLabel(ontology, annotationAxiom.getAnnotation());
	}
	
	
	// PRIVATE METHODS //
	
	/**
	 * Helper method for parseAxioms.
	 */
	private static String getOperatorPart(String xmlString, int operator, String axiom) {		
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
			System.err.println("SPParser >> ERROR: The axiom has no modal operator.");
			return "";
		}
		
		String endElement = xmlString.substring(operatorEnd, operatorEnd + offset);
		
		return xmlString.substring(operatorStart, operatorEnd) + axiom + endElement;
	}
	
	/**
	 * Helper method for parseAxioms.
	 */
	private static SPOperatorHandler getSPOperatorHandler(SAXParser saxParser, String xmlString) throws Exception {
		SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(spOperatorHandler);
		xmlReader.parse(new InputSource(new StringReader(xmlString)));
		return spOperatorHandler;
	}
}