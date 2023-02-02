package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class TranslatorTest {
	
	@Test
	public void givenOntology_whenConstructTranslator_thenReturnNewConcepts() {
		final File f = new File("./src/test/SPTest.owl");
		
		Map<String, String[]> expectedMap = new HashMap<String, String[]>();
		String[] arr1 = {"M_s1_0", "M_s1_1", "M_s1_2", "M_s1_3"};
		String[] arr2 = {"M_s2_0", "M_s2_1", "M_s2_2", "M_s2_3"};
		expectedMap.put("s1", arr1);
		expectedMap.put("s2", arr2);
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		Map<String, String[]> actualMap = translator.m;
		
		for (String key : expectedMap.keySet()) {
			assertEquals(expectedMap.get(key), actualMap.get(key));
		}

		for (String key : actualMap.keySet()) {
			assertEquals(expectedMap.get(key), actualMap.get(key));
		}
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenCompileSPAxiomsToTranslate_ReturnAxiomStrings() {
		final File f = new File("./src/test/SPTest.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
		} catch (OWLOntologyCreationException e) {
			fail("Ontology test file " + f.getName() + " does not exist.");
		}
		
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
									  
		final String expectedAxiom5 = "<booleanCombination>\n" +
									  "  <OR>\n" +
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
									  "    <NOT>\n" +
									  "      <Box>\n" +
									  "    <Standpoint name=\"s1\"/>\n" +
									  "  <SubClassOf>\n" +
									  "<LHS>Y</LHS>\n" +
									  "<RHS>X</RHS>\n" +
									  "</SubClassOf>\n" +
									  "</Box>\n" +
								 	  "    </NOT>\n" +
									  "  </OR>\n" +
									  "</booleanCombination>";
		
		final Set<String> expectedAxioms = Set.of(expectedAxiom1, expectedAxiom2, expectedAxiom3, expectedAxiom4, expectedAxiom5);
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		final Set<String> actualAxioms = translator.compileSPAxiomsToTranslate();
		
		assertEquals(expectedAxioms, actualAxioms);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOntology_whenTranslateOntologyAndSaveOutputOntology_thenAddAxiomsToOutputOntologyAndSaveToFile() {
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
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		translator.translateOntology();
		//System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> " + translator.outputOntology);
		translator.saveOutputOntology();
	}
	
	@Test
	public void givenSPExpression_whenTransExpr_thenReturnOWLClassExpression() {
		/* Here, the test file is only used for the initialisation of the Translator;
		 * test does not depend on specific standpointLabels.
		 */ 
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
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final String outputOntologyIRIString = ontologyIRIString + "_trans";
		
		final String expr1 = "<Standpoint name=\"s1\"/>\n";
		final String expr2 = "<Standpoint name=\"*\"/>\n";
		final String expr3 = "<MINUS>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <UNION>\n" +
			"        <Standpoint name=\"s1\"/>\n" +
			"        <Standpoint name=\"s2\"/>\n" +
			"      </UNION>\n" +
			"    </MINUS>\n";
		final String expr4 = "<INTERSECTION>\n" +
			"  <MINUS>\n" +
			"    <Standpoint name = \"s1\" />\n" +
			"    <Standpoint name = \"s2\" />\n" +
			"  </MINUS>\n" +
			"  <Standpoint name = \"s1\" />\n" +
			"</INTERSECTION>";
			
		final OWLObjectProperty u = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#universal_role"));
		
		final OWLClassExpression s1Class = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s1_0"));
		final OWLClassExpression s2Class = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s2_0"));
		final OWLClassExpression starClass = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_0"));
		
		final OWLClassExpression expectedClass1 = dataFactory.getOWLObjectAllValuesFrom(u, s1Class);
		
		final OWLClassExpression expectedClass2 = dataFactory.getOWLObjectAllValuesFrom(u, starClass);
		
		final OWLClassExpression union = dataFactory.getOWLObjectUnionOf(expectedClass1, dataFactory.getOWLObjectAllValuesFrom(u, s2Class));
		final OWLClassExpression expectedClass3 = dataFactory.getOWLObjectIntersectionOf(expectedClass2, dataFactory.getOWLObjectComplementOf(union));
		
		final OWLClassExpression minus = dataFactory.getOWLObjectIntersectionOf(expectedClass1, dataFactory.getOWLObjectComplementOf(dataFactory.getOWLObjectAllValuesFrom(u, s2Class)));
		final OWLClassExpression expectedClass4 = dataFactory.getOWLObjectIntersectionOf(minus, expectedClass1);
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		final OWLClassExpression actualClass1 = translator.transExpr(0, expr1);
		final OWLClassExpression actualClass2 = translator.transExpr(0, expr2);
		final OWLClassExpression actualClass3 = translator.transExpr(0, expr3);
		final OWLClassExpression actualClass4 = translator.transExpr(0, expr4);
		
		assertEquals(expectedClass1, actualClass1);
		assertEquals(expectedClass2, actualClass2);
		assertEquals(expectedClass3, actualClass3);
		assertEquals(expectedClass4, actualClass4);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOWLAxiom_whenTransPos_thenReturnOWLClassExpression() {
		/* Here, the test file is only used for the initialisation of the Translator;
		 * test does not depend on specific standpointLabels.
		 */ 
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
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final String outputOntologyIRIString = ontologyIRIString + "_trans";
		
		final OWLObjectProperty u = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#universal_role"));
		
		final OWLClassExpression classA = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#A"));
		final OWLClassExpression classB = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#B"));
		final OWLClassExpression classC = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#C"));
		
		final OWLClassExpression classA0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_0"));
		final OWLClassExpression classB0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_0"));
		final OWLClassExpression classC0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_0"));
		
		final OWLObjectProperty r = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIString + "#r"));
		
		final OWLObjectProperty rOut = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#r_0"));
		
		final OWLSubClassOfAxiom a_sub_b = dataFactory.getOWLSubClassOfAxiom(classA, classB);
		
		final OWLNamedIndividual objX = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIString + "#x"));
		final OWLNamedIndividual objY = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIString + "#y"));
		
		final OWLNamedIndividual objXOut = dataFactory.getOWLNamedIndividual(IRI.create(outputOntologyIRIString + "#x"));
		final OWLNamedIndividual objYOut = dataFactory.getOWLNamedIndividual(IRI.create(outputOntologyIRIString + "#y"));
		
		final OWLClassExpression nomXOut = dataFactory.getOWLObjectOneOf(objXOut);
		final OWLClassExpression nomYOut = dataFactory.getOWLObjectOneOf(objYOut);
		
		final OWLClassAssertionAxiom x_in_A = dataFactory.getOWLClassAssertionAxiom(classA, objX);
		
		final OWLObjectPropertyAssertionAxiom x_r_y = dataFactory.getOWLObjectPropertyAssertionAxiom(r, objX, objY);
		
		final OWLSameIndividualAxiom x_eq_y = dataFactory.getOWLSameIndividualAxiom(objX, objY);
		
		// add more complex test cases
		
		final OWLClassExpression expectedExpr1 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA0), classB0));
		
		final OWLClassExpression expectedExpr2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nomXOut, classA0));
		
		final OWLClassExpression expectedExpr3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nomXOut, dataFactory.getOWLObjectSomeValuesFrom(rOut, nomYOut)));
		
		final OWLClassExpression expectedExpr4 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nomXOut, nomYOut));
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		final OWLClassExpression actualExpr1 = translator.transPos(0, a_sub_b);
		final OWLClassExpression actualExpr2 = translator.transPos(0, x_in_A);
		final OWLClassExpression actualExpr3 = translator.transPos(0, x_r_y);
		final OWLClassExpression actualExpr4 = translator.transPos(0, x_eq_y);
		
		assertEquals(expectedExpr1, actualExpr1);
		assertEquals(expectedExpr2, actualExpr2);
		assertEquals(expectedExpr3, actualExpr3);
		assertEquals(expectedExpr4, actualExpr4);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOWLAxiom_whenTransNeg_thenReturnOWLClassExpression() {
		/* Here, the test file is only used for the initialisation of the Translator;
		 * test does not depend on specific standpointLabels.
		 */ 
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
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final String outputOntologyIRIString = ontologyIRIString + "_trans";
		
		final OWLObjectProperty u = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#universal_role"));
		
		final OWLClassExpression classA = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#A"));
		final OWLClassExpression classB = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#B"));
		final OWLClassExpression classC = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#C"));
		
		final OWLClassExpression classA0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_0"));
		final OWLClassExpression classB0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_0"));
		final OWLClassExpression classC0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_0"));
		
		final OWLObjectProperty r = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIString + "#r"));
		
		final OWLObjectProperty rOut = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#r_0"));
		
		final OWLSubClassOfAxiom a_sub_b = dataFactory.getOWLSubClassOfAxiom(classA, classB);
		
		final OWLNamedIndividual objX = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIString + "#x"));
		final OWLNamedIndividual objY = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIString + "#y"));
		
		final OWLNamedIndividual objXOut = dataFactory.getOWLNamedIndividual(IRI.create(outputOntologyIRIString + "#x"));
		final OWLNamedIndividual objYOut = dataFactory.getOWLNamedIndividual(IRI.create(outputOntologyIRIString + "#y"));
		
		final OWLClassExpression nomXOut = dataFactory.getOWLObjectOneOf(objXOut);
		final OWLClassExpression nomYOut = dataFactory.getOWLObjectOneOf(objYOut);
		
		final OWLClassAssertionAxiom x_in_A = dataFactory.getOWLClassAssertionAxiom(classA, objX);
		
		final OWLObjectPropertyAssertionAxiom x_r_y = dataFactory.getOWLObjectPropertyAssertionAxiom(r, objX, objY);
		
		final OWLSameIndividualAxiom x_eq_y = dataFactory.getOWLSameIndividualAxiom(objX, objY);
		
		// add more complex test cases
		
		final OWLClassExpression expectedExpr1 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA0, dataFactory.getOWLObjectComplementOf(classB0)));
		
		final OWLClassExpression expectedExpr2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nomXOut, dataFactory.getOWLObjectComplementOf(classA0)));
		
		final OWLClassExpression expectedExpr3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nomXOut, dataFactory.getOWLObjectAllValuesFrom(rOut, dataFactory.getOWLObjectComplementOf(nomYOut))));
		
		final OWLClassExpression expectedExpr4 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nomXOut, dataFactory.getOWLObjectComplementOf(nomYOut)));
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		final OWLClassExpression actualExpr1 = translator.transNeg(0, a_sub_b);
		final OWLClassExpression actualExpr2 = translator.transNeg(0, x_in_A);
		final OWLClassExpression actualExpr3 = translator.transNeg(0, x_r_y);
		final OWLClassExpression actualExpr4 = translator.transNeg(0, x_eq_y);
		
		assertEquals(expectedExpr1, actualExpr1);
		assertEquals(expectedExpr2, actualExpr2);
		assertEquals(expectedExpr3, actualExpr3);
		assertEquals(expectedExpr4, actualExpr4);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenBooleanCombination_whenTrans_thenReturnOWLClassExpression() {
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
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final String outputOntologyIRIString = ontologyIRIString + "_trans";
		
		final String xmlString1 = "<booleanCombination>\n" +
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
			"        <RHS>C</RHS>\n" +
			"      </EquivalentClasses>\n" +
			"    </Diamond>\n" +
			"  </AND>\n" +
			"</booleanCombination>\n";
			
		final String xmlString2 = "<booleanCombination>\n" +
			"  <AND>\n" +
			"    <Box>\n" +
			"      <Standpoint name=\"*\"/>\n" +
			"      <SubClassOf>\n" +
			"        <LHS>A</LHS>\n" +
			"        <RHS>B</RHS>\n" +
			"      </SubClassOf>\n" +
			"    </Box>\n" +
			"    <OR>\n" +
			"      <Diamond>\n" +
			"        <Standpoint name = \"s1\"/>\n" +
			"        <SubClassOf>\n" +
			"          <LHS>A</LHS>\n" +
			"          <RHS>B</RHS>\n" +
			"        </SubClassOf>\n" +
			"      </Diamond>\n" +
			"      <NOT>\n" +
			"        <Diamond>\n" +
			"          <Standpoint name=\"s2\"/>\n" +
			"          <EquivalentClasses>\n" +
			"            <LHS>A</LHS>\n" +
			"            <RHS>B</RHS>\n" +
			"          </EquivalentClasses>\n" +
			"        </Diamond>\n" +
			"      </NOT>\n" +
			"    </OR>\n" +
			"  </AND>\n" +
			"</booleanCombination>";
			
			final OWLObjectProperty u = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#universal_role"));
			
			final int expectedPrcs = 4;
			
			final OWLClass classA0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_0"));
			final OWLClass classA1 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_1"));
			final OWLClass classA2 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_2"));
			final OWLClass classA3 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_3"));
			
			final OWLClass classB0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_0"));
			final OWLClass classB1 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_1"));
			final OWLClass classB2 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_2"));
			final OWLClass classB3 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_3"));
			
			final OWLClass classC0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_0"));
			final OWLClass classC1 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_1"));
			final OWLClass classC2 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_2"));
			final OWLClass classC3 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_3"));
			
			final OWLClass m_s1_0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s1_0"));
			final OWLClass m_s1_1 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s1_1"));
			final OWLClass m_s1_2 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s1_2"));
			final OWLClass m_s1_3 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s1_3"));
			
			final OWLClass m_s2_0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s2_0"));
			final OWLClass m_s2_1 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s2_1"));
			final OWLClass m_s2_2 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s2_2"));
			final OWLClass m_s2_3 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_s2_3"));
			
			final OWLClassExpression s1Trans0 = dataFactory.getOWLObjectAllValuesFrom(u, m_s1_0);
			final OWLClassExpression s1Trans1 = dataFactory.getOWLObjectAllValuesFrom(u, m_s1_1);
			final OWLClassExpression s1Trans2 = dataFactory.getOWLObjectAllValuesFrom(u, m_s1_2);
			final OWLClassExpression s1Trans3 = dataFactory.getOWLObjectAllValuesFrom(u, m_s1_3);
			
			final OWLClassExpression s2Trans0 = dataFactory.getOWLObjectAllValuesFrom(u, m_s2_0);
			final OWLClassExpression s2Trans1 = dataFactory.getOWLObjectAllValuesFrom(u, m_s2_1);
			final OWLClassExpression s2Trans2 = dataFactory.getOWLObjectAllValuesFrom(u, m_s2_2);
			final OWLClassExpression s2Trans3 = dataFactory.getOWLObjectAllValuesFrom(u, m_s2_3);
			
			final OWLClassExpression a_and_b_and_not_c_trans0 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA0, classB0, dataFactory.getOWLObjectComplementOf(classC0)));
			final OWLClassExpression a_and_b_and_not_c_trans1 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA1, classB1, dataFactory.getOWLObjectComplementOf(classC1)));
			final OWLClassExpression a_and_b_and_not_c_trans2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA2, classB2, dataFactory.getOWLObjectComplementOf(classC2)));
			final OWLClassExpression a_and_b_and_not_c_trans3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA3, classB3, dataFactory.getOWLObjectComplementOf(classC3)));
			
			Set<OWLClassExpression> disjunctSet = new HashSet<OWLClassExpression>();
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans0, a_and_b_and_not_c_trans0));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans1, a_and_b_and_not_c_trans1));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans2, a_and_b_and_not_c_trans2));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans3, a_and_b_and_not_c_trans3));
			
			final OWLClassExpression notBox1Trans = dataFactory.getOWLObjectUnionOf(disjunctSet);
			
			final OWLClassExpression not_a_or_not_b_or_c_trans0 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA0), dataFactory.getOWLObjectComplementOf(classB0), classC0));
			final OWLClassExpression not_a_or_not_b_or_c_trans1 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA1), dataFactory.getOWLObjectComplementOf(classB1), classC1));
			final OWLClassExpression not_a_or_not_b_or_c_trans2 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA2), dataFactory.getOWLObjectComplementOf(classB2), classC2));
			final OWLClassExpression not_a_or_not_b_or_c_trans3 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA3), dataFactory.getOWLObjectComplementOf(classB3), classC3));
			
			final OWLClassExpression not_c_or_a_and_b_trans0 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classC0), dataFactory.getOWLObjectIntersectionOf(classA0, classB0)));
			final OWLClassExpression not_c_or_a_and_b_trans1 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classC1), dataFactory.getOWLObjectIntersectionOf(classA1, classB1)));
			final OWLClassExpression not_c_or_a_and_b_trans2 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classC2), dataFactory.getOWLObjectIntersectionOf(classA2, classB2)));
			final OWLClassExpression not_c_or_a_and_b_trans3 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classC3), dataFactory.getOWLObjectIntersectionOf(classA3, classB3)));
			
			disjunctSet = new HashSet<OWLClassExpression>();
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s2Trans0, not_a_or_not_b_or_c_trans0, not_c_or_a_and_b_trans0));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s2Trans1, not_a_or_not_b_or_c_trans1, not_c_or_a_and_b_trans1));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s2Trans2, not_a_or_not_b_or_c_trans2, not_c_or_a_and_b_trans2));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s2Trans3, not_a_or_not_b_or_c_trans3, not_c_or_a_and_b_trans3));
			
			final OWLClassExpression diamond1Trans = dataFactory.getOWLObjectUnionOf(disjunctSet);
			
			final OWLClassExpression expectedExpr1 = dataFactory.getOWLObjectIntersectionOf(diamond1Trans, notBox1Trans);
			
			final OWLClass m_star_0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_0"));
			final OWLClass m_star_1 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_1"));
			final OWLClass m_star_2 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_2"));
			final OWLClass m_star_3 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_3"));
			
			final OWLClassExpression not_m_star_trans0 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_star_0));
			final OWLClassExpression not_m_star_trans1 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_star_1));
			final OWLClassExpression not_m_star_trans2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_star_2));
			final OWLClassExpression not_m_star_trans3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_star_3));
			
			final OWLClassExpression not_a_or_b_trans0 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA0), classB0));
			final OWLClassExpression not_a_or_b_trans1 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA1), classB1));
			final OWLClassExpression not_a_or_b_trans2 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA2), classB2));
			final OWLClassExpression not_a_or_b_trans3 = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(classA3), classB3));
			
			final OWLClassExpression not_m_s2_trans0 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_s2_0));
			final OWLClassExpression not_m_s2_trans1 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_s2_1));
			final OWLClassExpression not_m_s2_trans2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_s2_2));
			final OWLClassExpression not_m_s2_trans3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectComplementOf(m_s2_3));
			
			final OWLClassExpression a_and_not_b_trans0 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA0, dataFactory.getOWLObjectComplementOf(classB0)));
			final OWLClassExpression a_and_not_b_trans1 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA1, dataFactory.getOWLObjectComplementOf(classB1)));
			final OWLClassExpression a_and_not_b_trans2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA2, dataFactory.getOWLObjectComplementOf(classB2)));
			final OWLClassExpression a_and_not_b_trans3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(classA3, dataFactory.getOWLObjectComplementOf(classB3)));
			
			final OWLClassExpression not_a_and_b_trans0 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectComplementOf(classA0), classB0));
			final OWLClassExpression not_a_and_b_trans1 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectComplementOf(classA1), classB1));
			final OWLClassExpression not_a_and_b_trans2 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectComplementOf(classA2), classB2));
			final OWLClassExpression not_a_and_b_trans3 = dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectComplementOf(classA3), classB3));
			
			Set<OWLClassExpression> conjunctSet1 = new HashSet<OWLClassExpression>();
			conjunctSet1.add(dataFactory.getOWLObjectUnionOf(not_m_star_trans0, not_a_or_b_trans0));
			conjunctSet1.add(dataFactory.getOWLObjectUnionOf(not_m_star_trans1, not_a_or_b_trans1));
			conjunctSet1.add(dataFactory.getOWLObjectUnionOf(not_m_star_trans2, not_a_or_b_trans2));
			conjunctSet1.add(dataFactory.getOWLObjectUnionOf(not_m_star_trans3, not_a_or_b_trans3));
			
			disjunctSet = new HashSet<OWLClassExpression>();
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans0, not_a_or_b_trans0));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans1, not_a_or_b_trans1));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans2, not_a_or_b_trans2));
			disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(s1Trans3, not_a_or_b_trans3));
			
			Set<OWLClassExpression> conjunctSet2 = new HashSet<OWLClassExpression>();
			conjunctSet2.add(dataFactory.getOWLObjectUnionOf(not_m_s2_trans0, a_and_not_b_trans0, not_a_and_b_trans0));
			conjunctSet2.add(dataFactory.getOWLObjectUnionOf(not_m_s2_trans1, a_and_not_b_trans1, not_a_and_b_trans1));
			conjunctSet2.add(dataFactory.getOWLObjectUnionOf(not_m_s2_trans2, a_and_not_b_trans2, not_a_and_b_trans2));
			conjunctSet2.add(dataFactory.getOWLObjectUnionOf(not_m_s2_trans3, a_and_not_b_trans3, not_a_and_b_trans3));
			
			final OWLClassExpression expectedExpr2 = dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectUnionOf(disjunctSet), dataFactory.getOWLObjectIntersectionOf(conjunctSet2)), dataFactory.getOWLObjectIntersectionOf(conjunctSet1));
			
			Translator translator;
			try {
				translator = new Translator(f);
			} catch (Exception e) {
				fail("Test incorrectly implemented.");
				return;
			}
			
			assertEquals(expectedPrcs, translator.iPrecisifications);
			
			final OWLClassExpression actualExpr1 = translator.trans(0, xmlString1);
			final OWLClassExpression actualExpr2 = translator.trans(0, xmlString2);
			
			assertEquals(expectedExpr1, actualExpr1);
			assertEquals(expectedExpr2, actualExpr2);
			
			System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenLHSandRHS_whenCreateSubClassOfAxiom_thenReturnOWLSubClassOfAxiom() {
		/* Here, the test file is only used for the initialisation of the Translator;
		 * test does not depend on specific standpointLabels.
		 */ 
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
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		
		final String lhs1 = "<LHS>A</LHS>";
		final String rhs1 = "<RHS>B</RHS>";
		
		final String lhs2 = "<LHS>A and B</LHS>";
		final String rhs2 = "<RHS>C</RHS>";
		
		final String lhs3 = "<LHS>A and (not B)</LHS>";
		final String rhs3 = "<RHS>not (A or (not B))</RHS>";
		
		final OWLClassExpression classA = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#A"));
		final OWLClassExpression classB = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#B"));
		final OWLClassExpression classC = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#C"));
		
		final OWLSubClassOfAxiom expectedAxiom1 = dataFactory.getOWLSubClassOfAxiom(classA, classB);
		
		final OWLSubClassOfAxiom expectedAxiom2 = dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLObjectIntersectionOf(classA, classB), classC);
		
		final OWLClassExpression classLHS3 = dataFactory.getOWLObjectIntersectionOf(classA, dataFactory.getOWLObjectComplementOf(classB));
		final OWLClassExpression classRHS3 = dataFactory.getOWLObjectComplementOf(dataFactory.getOWLObjectUnionOf(classA, dataFactory.getOWLObjectComplementOf(classB)));
		final OWLSubClassOfAxiom expectedAxiom3 = dataFactory.getOWLSubClassOfAxiom(classLHS3, classRHS3);
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		final OWLSubClassOfAxiom actualAxiom1 = translator.createSubClassOfAxiom(lhs1, rhs1);
		final OWLSubClassOfAxiom actualAxiom2 = translator.createSubClassOfAxiom(lhs2, rhs2);
		final OWLSubClassOfAxiom actualAxiom3 = translator.createSubClassOfAxiom(lhs3, rhs3);
		
		assertEquals(expectedAxiom1, actualAxiom1);
		assertEquals(expectedAxiom2, actualAxiom2);
		assertEquals(expectedAxiom3, actualAxiom3);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	@Test
	public void givenOWLClassExpression_whenNormalize_thenReturnNormalizedOWLClassExpression() {
		/* Here, the test file is only used for the initialisation of the Translator;
		 * test does not depend on specific standpointLabels.
		 */ 
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
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final String outputOntologyIRIString = ontologyIRIString + "_trans";
		
		final OWLClassExpression classA = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#A"));
		final OWLClassExpression classB = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#B"));
		final OWLClassExpression classC = dataFactory.getOWLClass(IRI.create(ontologyIRIString + "#C"));
		
		final OWLClassExpression classA0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#A_0"));
		final OWLClassExpression classB0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#B_0"));
		final OWLClassExpression classC0 = dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#C_0"));
		
		final OWLClassExpression a_and_b = dataFactory.getOWLObjectIntersectionOf(classA, classB);
		final OWLClassExpression not_a_and_b = dataFactory.getOWLObjectComplementOf(a_and_b);
		
		final OWLClassExpression a_and_b_or_c = dataFactory.getOWLObjectUnionOf(a_and_b, classC);
		final OWLClassExpression not_a_and_b_or_c = dataFactory.getOWLObjectComplementOf(a_and_b_or_c);
		
		final OWLClassExpression expectedExpression1 = classA0;
		
		final OWLClassExpression not_a0 = dataFactory.getOWLObjectComplementOf(classA0);
		final OWLClassExpression not_b0 = dataFactory.getOWLObjectComplementOf(classB0);
		final OWLClassExpression not_a0_or_not_b0 = dataFactory.getOWLObjectUnionOf(not_a0, not_b0);
		final OWLClassExpression expectedExpression2 = not_a0_or_not_b0;
		
		final OWLClassExpression not_c0 = dataFactory.getOWLObjectComplementOf(classC0);
		final OWLClassExpression not_a0_or_not_b0_and_not_c0 = dataFactory.getOWLObjectIntersectionOf(not_a0_or_not_b0, not_c0);
		final OWLClassExpression expectedExpression3 = not_a0_or_not_b0_and_not_c0;
		
		Translator translator;
		try {
			translator = new Translator(f);
		} catch (Exception e) {
			fail("Test incorrectly implemented.");
			return;
		}
		
		final OWLClassExpression actualExpression1 = translator.normalize(0, classA);
		final OWLClassExpression actualExpression2 = translator.normalize(0, not_a_and_b);
		final OWLClassExpression actualExpression3 = translator.normalize(0, not_a_and_b_or_c);
		
		assertEquals(expectedExpression1, actualExpression1);
		assertEquals(expectedExpression2, actualExpression2);
		assertEquals(expectedExpression3, actualExpression3);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
}