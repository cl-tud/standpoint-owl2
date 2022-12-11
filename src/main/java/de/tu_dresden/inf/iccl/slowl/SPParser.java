package de.tu_dresden.inf.iccl.slowl;

import com.google.common.base.Optional;

import java.io.File;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.w3c.dom.Document;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SPParser {
	
	// standpoint names recorded by the parser instance (without *)
	Set<String> spNames = new HashSet<String>();
	
	// standpoint axiom names recorded by the parser instance
	Set<String> spAxiomNames = new HashSet<String>();
	
	/**
	 * Adds recorded standpoint names and standpoint axiom names to
	 * spNames and spAxiomNames, respectively.
	 */
	public void parseSPOperator(String spOperator) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			SPOperatorHandler spOperatorHandler = new SPOperatorHandler();
			xmlReader.setContentHandler(spOperatorHandler);
			xmlReader.parse(new InputSource(new StringReader(spOperator)));
			
			Set<String> names = spOperatorHandler.spNames;
			names.remove("*");
			Iterator<String> it = names.iterator();
			it.forEachRemaining(next -> spNames.add(next));
			System.out.print(this + " >> Standpoint names: ");
			printSet(spNames);
			
			String axiomName = spOperatorHandler.spAxiomName;
			if (axiomName != null) {
				spAxiomNames.add(axiomName);
			}
			System.out.print(this + " >> Axiom names: ");
			printSet(spAxiomNames);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// STATIC METHODS //
	
	/**
	 * @return Returns the Set of all those OWLAnnotations in given ontology
	 * that annotate the given AxiomType and whose annotation property
	 * is equal to the standpointLabel annotation property.
	 */
	public static Set<OWLAnnotation> getAnnotations(OWLOntology ontology, AxiomType axiomType) {
		String ontologyIRI = "";
		if (ontology == null) {
			System.out.println("Received null ontology.");
			return new HashSet<OWLAnnotation>();
		} else if (ontology.isIRI()) {
			ontologyIRI = ontology.toString();
		} else {
			Optional<IRI> ontologyIRIOptional = ontology.getOntologyID().getOntologyIRI();
			if (ontologyIRIOptional.isPresent()) {
				ontologyIRI = ontologyIRIOptional.get().toString();
			} else {
				System.out.println("Received anonymous ontology.");
				return new HashSet<OWLAnnotation>();
			}
		}
				
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		Set<OWLAnnotation> annotationSet = new HashSet<OWLAnnotation>();		
		
		OWLAnnotation next = null;
		if (axiomType == AxiomType.SUBCLASS_OF) {
			for (OWLSubClassOfAxiom ax : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
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
			for (OWLEquivalentClassesAxiom ax : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
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
	 * @return Returns an XML String to be parsed.
	 */
	public static String spAnnotationToXML(OWLAnnotation spLabel) {
		OWLAnnotationValue value = spLabel.getValue();
		
		String spLabelString = "";
		if (value.isLiteral()) {
			spLabelString = "<?xml version=\"1.0\"?>\n" + value.asLiteral().get().getLiteral().toString();
			return spLabelString;
		} else {
			System.out.println("Value of " + spLabel + " is not a literal.");
			return null;
		}
	}
	
	public static void printSet(Set<String> names) {
		System.out.print("<[");
		int k = 1;
		for (String i : names) { 
			System.out.print(i);
			if (k++ < names.size()) {
				System.out.print(", ");
			}
		}
		System.out.println("]>");
	}
}