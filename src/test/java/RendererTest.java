package de.tu_dresden.inf.iccl.slowl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class RendererTest {
	
	@Test
	public void givenClassExpression_whenGetTopLevelClassExpression_thenReturnClassExpression() {
		final String iri = "http://slowl.test.org";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		final OWLClassExpression a = dataFactory.getOWLClass(IRI.create(iri + "#A")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression b = dataFactory.getOWLClass(IRI.create(iri + "#B")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression c = dataFactory.getOWLClass(IRI.create(iri + "#C")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression x = dataFactory.getOWLClass(IRI.create(iri + "#X")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression y = dataFactory.getOWLClass(IRI.create(iri + "#Y")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression z = dataFactory.getOWLClass(IRI.create(iri + "#Z")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		
		final OWLClassExpression[] a_b = {a, b};
		final OWLClassExpression[] arr1 = dataFactory.getOWLObjectIntersectionOf(a_b).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression a_and_b = arr1[0];
		
		final OWLClassExpression[] x_y = {x, y};
		final OWLClassExpression[] arr2 = dataFactory.getOWLObjectUnionOf(x_y).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression x_or_y = arr2[1];
		
		final OWLClassExpression[] expr = {a_and_b, x_or_y};
		final OWLClassExpression[] arr3 = dataFactory.getOWLObjectIntersectionOf(expr).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter = arr3[0];
		
		final OWLClassExpression expectedExpression1 = a_and_b;
		final OWLClassExpression expectedExpression2 = x_or_y;
		final OWLClassExpression expectedExpression3 = inter;
		
		final OWLClassExpression actualExpression1 = Renderer.getTopLevelExpression(arr1);
		final OWLClassExpression actualExpression2 = Renderer.getTopLevelExpression(arr2);
		final OWLClassExpression actualExpression3 = Renderer.getTopLevelExpression(arr3);
		
		assertEquals(expectedExpression1, actualExpression1);
		assertEquals(expectedExpression2, actualExpression2);
		assertEquals(expectedExpression3, actualExpression3);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
	// TO DO //
	@Test
	public void givenClassExpression_whenWriteClassExpression_thenReturnManchesterSyntax() {
		final String iri = "http://slowl.test.org";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		final OWLClassExpression a = dataFactory.getOWLClass(IRI.create(iri + "#A")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression b = dataFactory.getOWLClass(IRI.create(iri + "#B")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression c = dataFactory.getOWLClass(IRI.create(iri + "#C")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression x = dataFactory.getOWLClass(IRI.create(iri + "#X")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression y = dataFactory.getOWLClass(IRI.create(iri + "#Y")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression z = dataFactory.getOWLClass(IRI.create(iri + "#Z")).nestedClassExpressions().toArray(OWLClassExpression[]::new)[0];
		
		final OWLClassExpression[] a_b = {a, b};
		OWLClassExpression[] arr = dataFactory.getOWLObjectIntersectionOf(a_b).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression a_and_b = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] x_y = {x, y};
		arr = dataFactory.getOWLObjectUnionOf(x_y).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression x_or_y = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] expr1 = {a_and_b, x_or_y};
		arr = dataFactory.getOWLObjectIntersectionOf(expr1).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter1 = Renderer.getTopLevelExpression(arr);
		
		arr = dataFactory.getOWLObjectComplementOf(c).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression not_c = Renderer.getTopLevelExpression(arr);
		arr = dataFactory.getOWLObjectComplementOf(z).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression not_z = Renderer.getTopLevelExpression(arr);
		final OWLClassExpression[] expr2 = {not_c, not_z};
		arr = dataFactory.getOWLObjectIntersectionOf(expr2).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter2 = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] expr3 = {inter1, inter2};
		arr = dataFactory.getOWLObjectUnionOf(expr3).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression union = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] expr4 = {inter2, union};
		arr = dataFactory.getOWLObjectIntersectionOf(expr4).nestedClassExpressions().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter3 = Renderer.getTopLevelExpression(arr);
		
		final String expectedString1 = "(A and B)";
		final String expectedString2 = "(not C)";
		final String expectedString3 = "(((A and B and (X or Y)) or ((not C) and (not Z))) and (not C) and (not Z))";
		// the order of conjunct/disjuncts might differ
		
		final String actualString1 = Renderer.writeClassExpression(a_and_b);
		final String actualString2 = Renderer.writeClassExpression(not_c);
		final String actualString3 = Renderer.writeClassExpression(inter3);
		
		assertEquals(expectedString1, actualString1);
		assertEquals(expectedString2, actualString2);
		assertEquals(expectedString3, actualString3);
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}
	
}