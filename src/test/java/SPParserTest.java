package de.tu_dresden.inf.iccl.slowl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

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
		
		final String xmlString6 = "<booleanCombination>\n" +
								  "  <AND>\n" +
								  "    <NOT>\n" +
								  "      <standpointAxiom name=\"§ax\"/>\n" +
								  "    </NOT>\n" +
								  "    <Diamond>\n" +
								  "      <SubClassOf>\n" +
								  "        <LHS>A</LHS>\n" +
								  "        <RHS>B</RHS>\n" +
								  "      </SubClassOf>\n" +
								  "    </Diamond>\n" +
								  "    <NOT>\n" +
								  "      <standpointAxiom name=\"§ax1\"/>\n" +
								  "    </NOT>\n" +
								  "  </AND>\n" +
								  "</booleanCombination>";
								 
		final int expectedDiamonds6 = 1;
		
		SPParser parser = new SPParser();
		int actualDiamonds1;
		int actualDiamonds2;
		int actualDiamonds3;
		int actualDiamonds4;
		int actualDiamonds5;
		int actualDiamonds6;
		try {
			actualDiamonds1 = parser.countDiamonds(xmlString1);
			actualDiamonds2 = parser.countDiamonds(xmlString2);
			actualDiamonds3 = parser.countDiamonds(xmlString3);
			actualDiamonds4 = parser.countDiamonds(xmlString4);
			actualDiamonds5 = parser.countDiamonds(xmlString5);
			actualDiamonds6 = parser.countDiamonds(xmlString6);
		} catch (Exception e) {
			fail(e.getMessage());
			return;
		}
		
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
		
		SPParser parser = new SPParser(ontology);
		parser.countDiamonds();
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
									  "<RHS>A and B</RHS>\n" +
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
									
		final Set<String> expectedSPAxioms = Set.of(expectedAxiom1, expectedAxiom2, expectedAxiom4);
		
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
		
		//assertEquals(expectedArr1, actualArr1);
		//assertEquals(expectedArr2, actualArr2);
		
		for (int i = 0; i < Math.max(expectedArr1.length, actualArr1.length); i++) {
			assertEquals(expectedArr1[i], actualArr1[i]);
		}
		
		for (int i = 0; i < Math.max(expectedArr2.length, actualArr2.length); i++) {
			assertEquals(expectedArr2[i], actualArr2[i]);
		}
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
}