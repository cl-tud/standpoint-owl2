package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SPParserTest {
	
	@Test
	public void givenSPLabelAnnotation_whenSPLabelToXML_thenReturnXMLDocument() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("./src/test/SPTest.owl");
		IRI iri = IRI.create(f);
		
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create("http://www.iccl.inf.tu-dresden.de/ontologies/SPTest#standpointLabel"));	
		
		final String expectedSPLabelString = "<?xml version=\"1.0\"?>\n" +
			"<standpointAxiom operator=\"box\">\n" +
			"  <Standpoint name=\"s1\"/>\n" +
			"</standpointAxiom>";
			
		DocumentBuilder builder = null;
		Document expectedDocument = null;
		try {
			System.out.println("Building expectedDocument...");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			System.out.println(expectedSPLabelString);
			expectedDocument = builder.parse(new InputSource(new StringReader(expectedSPLabelString)));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		Document actualDocument = null;
		OWLAnnotation next = null;
		for (OWLSubClassOfAxiom ax : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
			Set<OWLAnnotation> annotations = ax.getAnnotations(label);
			Iterator<OWLAnnotation> it = annotations.iterator();
			try {
				next = it.next();
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}
			if (next != null) {
				System.out.println("Building actualDocument from " + f.getName() + " ...");
				actualDocument = SPParser.spLabelToXML(next);
				break;
			}
		}
		
		// cannot directly compare Documents in JUnit,
		// so we transform to XML string
		if (expectedDocument != null) {
			try {
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				
				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(expectedDocument), new StreamResult(writer));
				String expectedXMLString = writer.getBuffer().toString().replaceAll("\n|\r", "");
		
				writer = new StringWriter();
				transformer.transform(new DOMSource(actualDocument), new StreamResult(writer));
				String actualXMLString = writer.getBuffer().toString().replaceAll("\n|\r", "");
				
				assertEquals(expectedXMLString, actualXMLString);	
			} catch (TransformerException e) {
				e.printStackTrace();
				fail("Transformation from XML Document to XML String incorrectly implemented.");
			}
		} else {
			fail("Expected test result not correctly implemented.");
		}
	

	}
}