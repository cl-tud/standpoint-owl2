package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
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
		
		final String spExpression = "<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		OWLLiteral value = dataFactory.getOWLLiteral(spExpression);
		OWLAnnotation annotation = dataFactory.getOWLAnnotation(label, value);
		
		final String expectedXMLString = "<?xml version=\"1.0\"?>\n" +
			"<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		String actualXMLString = SPParser.spAnnotationToXML(annotation);
		
		assertEquals(expectedXMLString, actualXMLString);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenSPLabel_whenParseSPOperator_thenReturnSPNameSet() {
		Set<String> expectedSPNameSet = new HashSet<String>();
		expectedSPNameSet.add("spA");
		expectedSPNameSet.add("spB");
		expectedSPNameSet.add("s111");
		//System.out.print(this + " >> Expected standpoint names: ");
		//SPParser.printSet(expectedSPNameSet);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		final OWLAnnotationProperty label = dataFactory.getOWLAnnotationProperty(IRI.create("http://www.iccl.inf.tu-dresden.de/ontologies/SPTest#standpointLabel"));
		
		final String spExpression = "<standpointAxiom>\n" +
			"  <Diamond>\n" +
			"    <MINUS>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <UNION>\n" +
			"        <INTERSECTION>\n" +
			"          <Standpoint name=\"spA\"/>\n" +
			"          <Standpoint name=\"spB\"/>\n" +
			"        </INTERSECTION>\n" +
			"        <Standpoint name=\"s111\"/>\n" +
			"      </UNION>\n" +
			"    </MINUS>\n" +
			"  </Diamond>\n" +
			"</standpointAxiom>";
		
		final OWLLiteral value = dataFactory.getOWLLiteral(spExpression);
		final OWLAnnotation annotation = dataFactory.getOWLAnnotation(label, value);
		
		SPParser parser = new SPParser();
		parser.parseSPOperator(SPParser.spAnnotationToXML(annotation));
		Set<String> actualSPNameSet = parser.spNames;
		
		assertEquals(expectedSPNameSet, actualSPNameSet);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenBooleanCombination_whenCountDiamonds_thenReturnNumberOfDiamonds() {
		final String xmlString1 = "<?xml version=\"1.0\"?>\n" +
			"<booleanCombination>\n" +
			"  <AND>\n" +
			"    <NOT>\n" +
			"      <Box>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <SubClassOf>\n" +
			"          <LHS>A</LHS>\n" +
			"          <RHS>B</RHS>\n" +
			"        </SubClassOf>\n" +
			"      </Box>\n" +
			"    </NOT>\n" +
			"    <OR>\n" +
			"      <NOT>\n" +
			"        <NOT>\n" +
			"          <Diamond>\n" +
			"            <Standpoint name = \"s1\"/>\n" +
			"            <SubClassOf>\n" +
			"              <LHS>A</LHS>\n" +
			"              <RHS>B</RHS>\n" +
			"            </SubClassOf>\n" +
			"          </Diamond>\n" +
			"        </NOT>\n" +
			"      </NOT>\n" +
			"      <NOT>\n" +
			"        <Box>\n" +
			"          <Standpoint name=\"s1\"/>\n" +
			"          <SubClassOf>\n" +
			"            <LHS>A</LHS>\n" +
			"            <RHS>B</RHS>\n" +
			"          </SubClassOf>\n" +
			"        </Box>\n" +
			"      </NOT>\n" +
			"    </OR>\n" +
			"  </AND>\n" +
			"</booleanCombination>";
		
		final int expectedDiamonds1 = 3;
		
		final String xmlString2 = "<?xml version=\"1.0\"?>\n" +
			"<booleanCombination>\n" +
			"  <AND>\n" +
			"    <NOT>\n" +
			"      <Box>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <SubClassOf>\n" +
			"          <LHS>A</LHS>\n" +
			"          <RHS>B</RHS>\n" +
			"        </SubClassOf>\n" +
			"      </Box>\n" +
			"    </NOT>\n" +
			"    <OR>\n" +
			"      <NOT>\n" +
			"        <NOT>\n" +
			"          <NOT>\n" +
			"            <Diamond>\n" +
			"              <Standpoint name = \"s1\"/>\n" +
			"              <SubClassOf>\n" +
			"                <LHS>A</LHS>\n" +
			"                <RHS>B</RHS>\n" +
			"              </SubClassOf>\n" +
			"            </Diamond>\n" +
			"          </NOT>\n" +
			"        </NOT>\n" +
			"      </NOT>\n" +
			"      <Box>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <SubClassOf>\n" +
			"          <LHS>A</LHS>\n" +
			"          <RHS>B</RHS>\n" +
			"        </SubClassOf>\n" +
			"      </Box>\n" +
			"    </OR>\n" +
			"  </AND>\n" +
			"</booleanCombination>";
			
		final int expectedDiamonds2 = 1;
		
		final String xmlString3 = "<?xml version=\"1.0\"?>\n" +
			"<booleanCombination>\n" +
			"  <AND>\n" +
			"    <Box>\n" +
			"      <Standpoint name=\"s1\"/>\n" +
			"      <SubClassOf>\n" +
			"        <LHS>A</LHS>\n" +
			"        <RHS>B</RHS>\n" +
			"      </SubClassOf>\n" +
			"    </Box>\n" +
			"    <OR>\n" +
			"      <NOT>\n" +
			"        <Diamond>\n" +
			"          <Standpoint name = \"s1\"/>\n" +
			"          <SubClassOf>\n" +
			"            <LHS>A</LHS>\n" +
			"            <RHS>B</RHS>\n" +
			"          </SubClassOf>\n" +
			"        </Diamond>\n" +
			"      </NOT>\n" +
			"      <NOT>\n" +
			"        <NOT>\n" +
			"          <Box>\n" +
			"            <Standpoint name=\"s1\"/>\n" +
			"            <SubClassOf>\n" +
			"              <LHS>A</LHS>\n" +
			"              <RHS>B</RHS>\n" +
			"            </SubClassOf>\n" +
			"          </Box>\n" +
			"        </NOT>\n" +
			"      </NOT>\n" +
			"    </OR>\n" +
			"  </AND>\n" +
			"</booleanCombination>";
		
		final int expectedDiamonds3 = 0;
		
		final String xmlString4 = "<?xml version=\"1.0\"?>\n" +
			"<standpointAxiom>\n" +
			"  <Diamond>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Diamond>\n" +
			"</standpointAxiom>";
		
		final int expectedDiamonds4 = 1;
		
		final String xmlString5 = "<?xml version=\"1.0\"?>\n" +
			"<booleanCombination>\n" +
			"  <AND>\n" +
			"    <NOT>\n" +
			"      <Box>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <SubClassOf>\n" +
			"          <LHS>A and B</LHS>\n" +
			"          <RHS>C</RHS>\n" +
			"        </SubClassOf>\n" +
			"      </Box>\n" +
			"    </NOT>\n" +
			"    <Diamond>\n" +
			"      <Standpoint name=\"s2\"/>\n" +
			"      <EquivalentClasses>\n" +
			"        <LHS>A and B</LHS>\n" +
			"        <RHS>X</RHS>\n" +
			"      </EquivalentClasses>\n" +
			"    </Diamond>\n" +
			"  </AND>\n" +
			"</booleanCombination>\n";

		final int expectedDiamonds5 = 2;
		
		SPParser parser = new SPParser();
		int actualDiamonds1 = parser.countDiamonds(xmlString1);
		int actualDiamonds2 = parser.countDiamonds(xmlString2);
		int actualDiamonds3 = parser.countDiamonds(xmlString3);
		int actualDiamonds4 = parser.countDiamonds(xmlString4);
		int actualDiamonds5 = parser.countDiamonds(xmlString5);
		
		assertEquals(expectedDiamonds1, actualDiamonds1);		
		assertEquals(expectedDiamonds2, actualDiamonds2);		
		assertEquals(expectedDiamonds3, actualDiamonds3);		
		assertEquals(expectedDiamonds4, actualDiamonds4);		
		assertEquals(expectedDiamonds5, actualDiamonds5);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");		
	}
	
	@Test
	public void givenOntology_whenCountDiamonds_thenReturnNumberOfDiamonds() {
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
		
		final int expectedDiamonds = 4;
		
		SPParser parser = new SPParser();
		parser.countDiamonds(ontology);
		final int actualDiamonds = parser.diamondCount;
		
		assertEquals(expectedDiamonds, actualDiamonds);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
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
		
		final String spExpression1 = "<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";

		final String spExpression2 = "<standpointAxiom>\n" +
			"  <Diamond>\n" +
			"    <MINUS>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <UNION>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <Standpoint name=\"s2\"/>\n" +
			"      </UNION>\n" +
			"    </MINUS>\n" +
			"  </Diamond>\n" +
			"</standpointAxiom>";
			
		final String spExpression3 = "<standpointAxiom name=\"§ax1\">\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
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
		
		Set<OWLAnnotation> actualAnnotations = SPParser.getAnnotations(ontology, AxiomType.SUBCLASS_OF);
		
		assertEquals(expectedAnnotations, actualAnnotations);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenGetAnnotatedEquivalentClassesAxioms_thenReturnAnnotatedAxioms() {
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
		
		final String spOperator = "<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"*\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		final OWLClass a = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#A"));
		final OWLClass b = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#B"));
		final OWLClass c = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#C"));
		
		final OWLLiteral value = dataFactory.getOWLLiteral(spOperator);
		
		final OWLAnnotation[] annotation = {dataFactory.getOWLAnnotation(label, value)};
		
		final OWLClassExpression[] a_b = {a, b};
		final OWLClassExpression a_and_b = dataFactory.getOWLObjectIntersectionOf(a_b);
		
		final OWLClassExpression[] c_a_and_b = {c, a_and_b};
		final OWLEquivalentClassesAxiom c_eq_a_and_b = dataFactory.getOWLEquivalentClassesAxiom(c_a_and_b);
		
		Set<OWLEquivalentClassesAxiom> expectedAxioms = new HashSet<OWLEquivalentClassesAxiom>();
		expectedAxioms.add(c_eq_a_and_b.getAnnotatedAxiom(Set.of(annotation)));
		
		Set<OWLEquivalentClassesAxiom> actualAxioms = SPParser.getAnnotatedEquivalentClassesAxioms(ontology);
		
		assertEquals(expectedAxioms, actualAxioms);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenGetAnnotatedSubClassOfAxioms_thenReturnAnnotatedAxioms() {
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
		
		final String spOperator1 = "<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		final String spOperator2 = "<standpointAxiom>\n" +
			"  <Diamond>\n" +
			"    <MINUS>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <UNION>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <Standpoint name=\"s2\"/>\n" +
			"      </UNION>\n" +
			"    </MINUS>\n" +
			"  </Diamond>\n" +
			"</standpointAxiom>";
			
		final String spOperator3 = "<standpointAxiom name=\"§ax1\">\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
		
		final OWLLiteral value1 = dataFactory.getOWLLiteral(spOperator1);
		final OWLLiteral value2 = dataFactory.getOWLLiteral(spOperator2);
		final OWLLiteral value3 = dataFactory.getOWLLiteral(spOperator3);
		
		final OWLAnnotation[] annotation1 = {dataFactory.getOWLAnnotation(label, value1)};
		final OWLAnnotation[] annotation2 = {dataFactory.getOWLAnnotation(label, value2)};
		final OWLAnnotation[] annotation3 = {dataFactory.getOWLAnnotation(label, value3)};
		
		final OWLClass a = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#A"));
		final OWLClass b = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#B"));
		final OWLClass x = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#X"));
		final OWLClass y = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Y"));
		final OWLClass z = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#Z"));
		
		
		final OWLSubClassOfAxiom b_sub_a = dataFactory.getOWLSubClassOfAxiom(b,a);
		final OWLSubClassOfAxiom y_sub_x = dataFactory.getOWLSubClassOfAxiom(y,x);
		final OWLSubClassOfAxiom z_sub_x = dataFactory.getOWLSubClassOfAxiom(z,x);
		
		Set<OWLSubClassOfAxiom> expectedAxioms = new HashSet<OWLSubClassOfAxiom>();
		expectedAxioms.add(b_sub_a.getAnnotatedAxiom(Set.of(annotation1)));
		expectedAxioms.add(z_sub_x.getAnnotatedAxiom(Set.of(annotation2)));
		expectedAxioms.add(y_sub_x.getAnnotatedAxiom(Set.of(annotation3)));
		
		Set<OWLSubClassOfAxiom> actualAxioms = SPParser.getAnnotatedSubClassOfAxioms(ontology);
		
		assertEquals(expectedAxioms, actualAxioms);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
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
		
		final String spExpression1 = "<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		final String spExpression2 = "<standpointAxiom>\n" +
			"  <Diamond>\n" +
			"    <MINUS>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <UNION>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <Standpoint name=\"s2\"/>\n" +
			"      </UNION>\n" +
			"    </MINUS>\n" +
			"  </Diamond>\n" +
			"</standpointAxiom>";
			
		final String spExpression3 = "<standpointAxiom>\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"*\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
			
		final String spExpression4 = "<standpointAxiom name=\"§ax1\">\n" +
			"  <Box>\n" +
			"    <Standpoint name=\"s1\"/>\n" +
			"  </Box>\n" +
			"</standpointAxiom>";
		
		final OWLLiteral value1 = dataFactory.getOWLLiteral(spExpression1);
		final OWLLiteral value2 = dataFactory.getOWLLiteral(spExpression2);
		final OWLLiteral value3 = dataFactory.getOWLLiteral(spExpression3);
		final OWLLiteral value4 = dataFactory.getOWLLiteral(spExpression4);
		final OWLAnnotation expectedAnnotation1 = dataFactory.getOWLAnnotation(label, value1);
		final OWLAnnotation expectedAnnotation2 = dataFactory.getOWLAnnotation(label, value2);
		final OWLAnnotation expectedAnnotation3 = dataFactory.getOWLAnnotation(label, value3);
		final OWLAnnotation expectedAnnotation4 = dataFactory.getOWLAnnotation(label, value4);
		
		Set<OWLAnnotation> expectedAnnotations = new HashSet<OWLAnnotation>();
		expectedAnnotations.add(expectedAnnotation1);
		expectedAnnotations.add(expectedAnnotation2);
		expectedAnnotations.add(expectedAnnotation3);
		expectedAnnotations.add(expectedAnnotation4);
		
		Set<OWLAnnotation> actualAnnotations = SPParser.getAnnotations(ontology);
		
		assertEquals(expectedAnnotations, actualAnnotations);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenGetNames_thenReturnSPNamesAndSPAxiomNames() {
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
		
		Set<String> expectedSPNames = new HashSet<String>();
		expectedSPNames.add("s1");
		expectedSPNames.add("s2");
		Set<String> expectedSPAxiomNames = new HashSet<String>();
		expectedSPAxiomNames.add("§ax1");
		
		SPParser parser = new SPParser();
		parser.getNames(ontology);
		
		Set<String> actualSPNames = parser.spNames;
		Set<String> actualSPAxiomNames = parser.spAxiomNames;
		
		assertEquals(expectedSPNames, actualSPNames);
		assertEquals(expectedSPAxiomNames, actualSPAxiomNames);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenParseAxioms_thenSetRelevantSets() {
		// relevant sets: spAxiomNames, spNames, spAxioms, spAxiomNameMap, standardAxioms
		
		final File f = new File("./src/test/SPTest.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
		} catch (OWLOntologyCreationException e) {
			fail("Ontology test file " + f.getName() + " does not exist.");
		}
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
									  
		final String expectedAxiom1 = "<Box>\n" +
									  "    <Standpoint name=\"s1\"/>\n" +
									  "  <SubClassOf>\n" +
									  "<LHS>B</LHS>\n" +
									  "<RHS>A</RHS>\n" +
									  "</SubClassOf>\n" +
									  "</Box>\n";
									  
		final String expectedAxiom2 = "<Box>\n" +
									  "    <Standpoint name=\"*\"/>\n" +
									  "  <EquivalentClasses>\n" +
									  "<LHS>C</LHS>\n" +
									  "<RHS>(A and B)</RHS>\n" +
									  "</EquivalentClasses>\n" +
									  "</Box>\n";
		
		final String expectedAxiom3 = "<Box>\n" +
									  "    <Standpoint name=\"s1\"/>\n" +
									  "  <SubClassOf>\n" +
									  "<LHS>Y</LHS>\n" +
									  "<RHS>X</RHS>\n" +
									  "</SubClassOf>\n" +
									  "</Box>\n";
									  
		final String expectedAxiom4 = "<Diamond>\n" +
									  "    <MINUS>\n" +
									  "      <Standpoint name=\"*\"/>\n" +
									  "      <UNION>\n" +
									  "        <Standpoint name=\"s1\"/>\n" +
									  "        <Standpoint name=\"s2\"/>\n" +
									  "      </UNION>\n" +
									  "    </MINUS>\n" +
									  "  <SubClassOf>\n" +
									  "<LHS>Z</LHS>\n" +
									  "<RHS>X</RHS>\n" +
									  "</SubClassOf>\n" +
									  "</Diamond>\n";
									
		final Set<String> expectedSPAxioms = Set.of(expectedAxiom1, expectedAxiom2, expectedAxiom3, expectedAxiom4);
		
		final Set<String> expectedAxiomNames = Set.of("§ax1");
		
		final String expectedValue = "<Box>\n" +
									 "    <Standpoint name=\"s1\"/>\n" +
									 "  <SubClassOf>\n" +
									 "<LHS>Y</LHS>\n" +
									 "<RHS>X</RHS>\n" +
									 "</SubClassOf>\n" +
									 "</Box>\n";
			
		Map<String, String> expectedMap = new HashMap<String, String>();
		expectedMap.put("§ax1", expectedValue);
		
		final Set<String> expectedSPNames = Set.of("s1", "s2");
		
		final OWLObjectProperty r = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIString + "#r"));
		final OWLNamedIndividual a = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIString + "#a"));
		final OWLNamedIndividual b = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIString + "#b"));
		final OWLAxiom r_b_a = dataFactory.getOWLObjectPropertyAssertionAxiom(r, b, a);
		
		final OWLClass classA = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#A"));
		final OWLAxiom a_in_A = dataFactory.getOWLClassAssertionAxiom(classA, a);
		
		final OWLAxiom a_eq_b = dataFactory.getOWLSameIndividualAxiom(a, b);
		
		final OWLClass classW = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#W"));
		final OWLAxiom w_sub_all_r_A = dataFactory.getOWLSubClassOfAxiom(classW, dataFactory.getOWLObjectAllValuesFrom(r, classA));
		
		final Set<OWLAxiom> expectedStandardAxioms = Set.of(r_b_a, a_in_A, a_eq_b, w_sub_all_r_A);
		
		SPParser parser = new SPParser(ontology);
		parser.parseAxioms();
		final Set<String> actualAxiomNames = parser.spAxiomNames;
		final Map<String, String> actualMap = parser.spAxiomNameMap;
		final Set<String> actualSPNames = parser.spNames;
		final Set<OWLAxiom> actualStandardAxioms = parser.standardAxioms;
		final Set<String> actualSPAxioms = parser.spAxioms;
		
		assertEquals(expectedAxiomNames, actualAxiomNames);
		assertEquals(expectedSPNames, actualSPNames);
		assertEquals(expectedStandardAxioms, actualStandardAxioms);
		assertEquals(expectedSPAxioms, actualSPAxioms);

		for (String key : expectedMap.keySet()) {
			assertEquals(expectedMap.get(key), actualMap.get(key));
		}

		for (String key : actualMap.keySet()) {
			assertEquals(expectedMap.get(key), actualMap.get(key));
		}

		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenBooleanCombination_whenGetFirstSPAxiomName_thenReturnSPAxiomName() {		
		final String boolComb1 = "<booleanCombination>\n" +
								 "  <AND>\n" +
								 "    <standpointAxiom name=\"§ax1\"/>\n" +
								 "    <Box>\n" +
								 "      <Standpoint name=\"s1\"/>\n" +
								 "      <SubClassOf>\n" +
								 "        <LHS>A</LHS>" +
								 "        <RHS>B</RHS>" +
								 "      </SubClassOf>\n" +
								 "    </Box>\n" +
								 "  </AND>\n" +
								 "</booleanCombination>";
								 
		final String boolComb2 = "<booleanCombination>\n" +
								 "  <AND>\n" +
								 "    <NOT>\n" +
								 "      <Box>\n" +
								 "        <Standpoint name=\"s1\"/>\n" +
								 "        <SubClassOf>\n" +
								 "          <LHS>A and B</LHS>\n" +
								 "          <RHS>C</RHS>\n" +
								 "        </SubClassOf>\n" +
								 "      </Box>\n" +
								 "    </NOT>\n" +
								 "    <standpointAxiom name=\"§ax2\"/>\n" +
								 "  </AND>\n" +
								 "</booleanCombination>\n";
								 
		final String expectedName1 = "§ax1";
		final String expectedName2 = "§ax2";

		SPParser parser = new SPParser();
		
		String actualName1 = parser.getFirstSPAxiomName(boolComb1);
		String actualName2 = parser.getFirstSPAxiomName(boolComb2);
		
		assertEquals(expectedName1, actualName1);
		assertEquals(expectedName2, actualName2);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenGetSPNames_thenReturnSPNames() {
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
		
		Set<String> expectedSPNames = new HashSet<String>();
		expectedSPNames.add("s1");
		expectedSPNames.add("s2");
		
		SPParser parser = new SPParser();
		parser.getSPNames(ontology);
		Set<String> actualSPNames = parser.spNames;
		
		assertEquals(expectedSPNames, actualSPNames);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenXMLString_whenGetRootAndChildElements_thenReturnStringArray() {
		final String xmlString1 = "<?xml version=\"1.0\"?>\n" +
			"<ROOT>\n" +
			"  <Child1 name=\"s1\"/>\n" +
			"  <Child2 name=\"s2\"/>\n" +
			"</ROOT>";
		final String xmlString2 = "<ROOT attr=\"value\">\n" +
			"  <Child1>\n" +
			"    <SubChild name=\"s1\"/>\n" +
			"  </Child1>\n" +
			"  <Child2 name=\"s2\"/>\n" +
			"</ROOT>";
			
		final String[] expectedArr1 = {"ROOT", "<Child1 name=\"s1\"/>", "<Child2 name=\"s2\"/>"};
		final String child1 = "<Child1>\n" +
			"    <SubChild name=\"s1\"/>\n" +
			"</Child1>";
		final String[] expectedArr2 = {"ROOT", child1, "<Child2 name=\"s2\"/>"};
		
		SPParser parser = new SPParser();
		
		final String[] actualArr1 = parser.getRootAndChildElements(xmlString1);
		final String[] actualArr2 = parser.getRootAndChildElements(xmlString2);
		
		assertEquals(expectedArr1, actualArr1);
		assertEquals(expectedArr2, actualArr2);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
}