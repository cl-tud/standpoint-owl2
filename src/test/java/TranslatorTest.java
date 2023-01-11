package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class TranslatorTest {
	
	@Test
	public void givenOntology_whenContructTranslator_thenReturnNewConcepts() {
		final File f = new File("./src/test/SPTest.owl");
		
		Map<String, String[]> expectedMap = new HashMap<String, String[]>();
		String[] arr1 = {"M_s1_0", "M_s1_1", "M_s1_2", "M_s1_3"};
		String[] arr2 = {"M_s2_0", "M_s2_1", "M_s2_2", "M_s2_3"};
		expectedMap.put("s1", arr1);
		expectedMap.put("s2", arr2);
		
		Translator translator = new Translator(f);
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
		
		Translator translator = new Translator(f);
		
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
}