package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.NoSuchElementException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

import org.junit.Test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SPParserTest {
	
	@Test
	public void givenSPLabelAnnotation_whenSPAnnotationToXML_thenReturnXMLString() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create("http://www.iccl.inf.tu-dresden.de/ontologies/SPTest#standpointLabel"));
		
		final String spExpression = "<standpointAxiom operator=\"box\">\n"+
			"  <Standpoint name=\"s1\"/>\n" +
			"</standpointAxiom>";
			
		OWLLiteral value = dataFactory.getOWLLiteral(spExpression);
		OWLAnnotation annotation = dataFactory.getOWLAnnotation(label, value);
		
		final String expectedXMLString = "<?xml version=\"1.0\"?>\n" +
			"<standpointAxiom operator=\"box\">\n" +
			"  <Standpoint name=\"s1\"/>\n" +
			"</standpointAxiom>";
			
		String actualXMLString = SPParser.spAnnotationToXML(annotation);
		
		assertEquals(expectedXMLString, actualXMLString);
	}
	
	@Test
	public void givenSPLabel_whenParseSPExpression_thenReturnSPNameSet() {
		Set<String> expectedSPNameSet = new HashSet<String>();
		expectedSPNameSet.add("spA");
		expectedSPNameSet.add("spB");
		expectedSPNameSet.add("s111");
		//System.out.print(this + " >> Expected standpoint names: ");
		//SPParser.printSet(expectedSPNameSet);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		final OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create("http://www.iccl.inf.tu-dresden.de/ontologies/SPTest#standpointLabel"));
		
		final String spExpression = "<standpointAxiom operator=\"diamond\">\n" +
			"  <MINUS>\n" +
			"    <Standpoint name=\"*\"/>\n" +
			"    <UNION>\n" +
			"      <INTERSECTION>\n" +
			"        <Standpoint name=\"spA\"/>\n" +
			"        <Standpoint name=\"spB\"/>\n" +
			"      </INTERSECTION>\n" +
			"      <Standpoint name=\"s111\"/>\n" +
			"    </UNION>\n" +
			"  </MINUS>\n" +
			"</standpointAxiom>";
		
		final OWLLiteral value = dataFactory.getOWLLiteral(spExpression);
		final OWLAnnotation annotation = dataFactory.getOWLAnnotation(label, value);
		
		SPParser parser = new SPParser();
		parser.parseSPExpression(SPParser.spAnnotationToXML(annotation));
		Set<String> actualSPNameSet = parser.spNames;
		
		assertEquals(expectedSPNameSet, actualSPNameSet);
	}
	
	@Test
	public void givenOntologyAndAxiomType_whenGetAnnotations_thenReturnAnnotations() {
		final File f = new File("./src/test/SPTest.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			fail("Ontology test file " + f.getName() + " does not exist.");
		}
		
		final String ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
		
		final OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		final String spExpression1 = "<standpointAxiom operator=\"box\">\n" +
			"  <Standpoint name=\"s1\"/>\n" +
			"</standpointAxiom>";
		final String spExpression2 = "<standpointAxiom operator=\"diamond\">\n" +
			"  <MINUS>\n" +
			"    <Standpoint name=\"*\"/>\n" +
			"    <UNION>\n" +
			"      <Standpoint name=\"s1\"/>\n" +
			"      <Standpoint name=\"s2\"/>\n" +
			"    </UNION>\n" +
			"  </MINUS>\n" +
			"</standpointAxiom>";
		
		final OWLLiteral value1 = dataFactory.getOWLLiteral(spExpression1);
		final OWLLiteral value2 = dataFactory.getOWLLiteral(spExpression2);
		final OWLAnnotation expectedAnnotation1 = dataFactory.getOWLAnnotation(label, value1);
		final OWLAnnotation expectedAnnotation2 = dataFactory.getOWLAnnotation(label, value2);
		
		Set<OWLAnnotation> expectedAnnotations = new HashSet<OWLAnnotation>();
		expectedAnnotations.add(expectedAnnotation1);
		expectedAnnotations.add(expectedAnnotation2);
		//System.out.println(this + " >> Expected annotations:\n" + expectedAnnotations);
		
		Set<OWLAnnotation> actualAnnotations = SPParser.getAnnotations(ontology, AxiomType.SUBCLASS_OF);
		
		assertEquals(expectedAnnotations, actualAnnotations);
	}
	
	@Test
	public void givenOntology_whenGetAnnotations_thenReturnAnnotations() {
		final File f = new File("./src/test/SPTest.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			fail("Ontology test file " + f.getName() + " does not exist.");
		}
		
		final String ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
		
		final OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRI + "#standpointLabel"));
		
		final String spExpression1 = "<standpointAxiom operator=\"box\">\n" +
			"  <Standpoint name=\"s1\"/>\n" +
			"</standpointAxiom>";
		final String spExpression2 = "<standpointAxiom operator=\"diamond\">\n" +
			"  <MINUS>\n" +
			"    <Standpoint name=\"*\"/>\n" +
			"    <UNION>\n" +
			"      <Standpoint name=\"s1\"/>\n" +
			"      <Standpoint name=\"s2\"/>\n" +
			"    </UNION>\n" +
			"  </MINUS>\n" +
			"</standpointAxiom>";
		final String spExpression3 = "<standpointAxiom operator=\"box\">\n" +
			"  <Standpoint name=\"*\"/>\n" +
			"</standpointAxiom>";
		
		final OWLLiteral value1 = dataFactory.getOWLLiteral(spExpression1);
		final OWLLiteral value2 = dataFactory.getOWLLiteral(spExpression2);
		final OWLLiteral value3 = dataFactory.getOWLLiteral(spExpression3);
		final OWLAnnotation expectedAnnotation1 = dataFactory.getOWLAnnotation(label, value1);
		final OWLAnnotation expectedAnnotation2 = dataFactory.getOWLAnnotation(label, value2);
		final OWLAnnotation expectedAnnotation3 = dataFactory.getOWLAnnotation(label, value3);
		
		Set<OWLAnnotation> expectedAnnotations = new HashSet<OWLAnnotation>();
		expectedAnnotations.add(expectedAnnotation1);
		expectedAnnotations.add(expectedAnnotation2);
		expectedAnnotations.add(expectedAnnotation3);
		//System.out.println(this + " >> Expected annotations:\n" + expectedAnnotations);
		
		Set<OWLAnnotation> actualAnnotations = SPParser.getAnnotations(ontology);
		
		assertEquals(expectedAnnotations, actualAnnotations);
	}
}