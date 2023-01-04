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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
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
}