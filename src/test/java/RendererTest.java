package de.tu_dresden.inf.iccl.slowl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class RendererTest {
	
	@Test
	public void givenClassExpression_whenGetTopLevelClassExpression_thenReturnClassExpression() {
		final String iri = "http://slowl.test.org";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		final OWLClassExpression a = dataFactory.getOWLClass(IRI.create(iri + "#A")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression b = dataFactory.getOWLClass(IRI.create(iri + "#B")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression c = dataFactory.getOWLClass(IRI.create(iri + "#C")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression x = dataFactory.getOWLClass(IRI.create(iri + "#X")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression y = dataFactory.getOWLClass(IRI.create(iri + "#Y")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression z = dataFactory.getOWLClass(IRI.create(iri + "#Z")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		
		final OWLClassExpression[] a_b = {a, b};
		final OWLClassExpression[] arr1 = dataFactory.getOWLObjectIntersectionOf(a_b).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression a_and_b = dataFactory.getOWLObjectIntersectionOf(a_b);
		
		final OWLClassExpression[] x_y = {x, y};
		final OWLClassExpression[] arr2 = dataFactory.getOWLObjectUnionOf(x_y).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression x_or_y = dataFactory.getOWLObjectUnionOf(x_y);
		
		final OWLClassExpression[] expr = {a_and_b, x_or_y};
		final OWLClassExpression[] arr3 = dataFactory.getOWLObjectIntersectionOf(expr).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter = dataFactory.getOWLObjectIntersectionOf(expr);
		
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
	
	/*@Test
	public void givenClassExpression_whenWriteClassExpression_thenReturnManchesterSyntax() {
		final String iri = "http://slowl.test.org";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		final OWLClassExpression a = dataFactory.getOWLClass(IRI.create(iri + "#A")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression b = dataFactory.getOWLClass(IRI.create(iri + "#B")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression c = dataFactory.getOWLClass(IRI.create(iri + "#C")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression x = dataFactory.getOWLClass(IRI.create(iri + "#X")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression y = dataFactory.getOWLClass(IRI.create(iri + "#Y")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		final OWLClassExpression z = dataFactory.getOWLClass(IRI.create(iri + "#Z")).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new)[0];
		
		final OWLObjectProperty r = dataFactory.getOWLObjectProperty(IRI.create(iri + "#r"));
		final OWLClassExpression r_only_A = dataFactory.getOWLObjectAllValuesFrom(r, a);
		final OWLClassExpression inv_r_only_A_and_B = dataFactory.getOWLObjectAllValuesFrom(dataFactory.getOWLObjectInverseOf(r), dataFactory.getOWLObjectIntersectionOf(a, b));
		final OWLClassExpression r_some_A = dataFactory.getOWLObjectSomeValuesFrom(r, a);
		
		final OWLClassExpression[] a_b = {a, b};
		OWLClassExpression[] arr = dataFactory.getOWLObjectIntersectionOf(a_b).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression a_and_b = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] x_y = {x, y};
		arr = dataFactory.getOWLObjectUnionOf(x_y).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression x_or_y = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] expr1 = {a_and_b, x_or_y};
		arr = dataFactory.getOWLObjectIntersectionOf(expr1).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter1 = Renderer.getTopLevelExpression(arr);
		
		arr = dataFactory.getOWLObjectComplementOf(c).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression not_c = Renderer.getTopLevelExpression(arr);
		arr = dataFactory.getOWLObjectComplementOf(z).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression not_z = Renderer.getTopLevelExpression(arr);
		final OWLClassExpression[] expr2 = {not_c, not_z};
		arr = dataFactory.getOWLObjectIntersectionOf(expr2).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter2 = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] expr3 = {inter1, inter2};
		arr = dataFactory.getOWLObjectUnionOf(expr3).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression union = Renderer.getTopLevelExpression(arr);
		
		final OWLClassExpression[] expr4 = {inter2, union};
		arr = dataFactory.getOWLObjectIntersectionOf(expr4).getNestedClassExpressions().stream().toArray(OWLClassExpression[]::new);
		final OWLClassExpression inter3 = Renderer.getTopLevelExpression(arr);
		
		final String expectedString1 = "(A and B)";
		final String expectedString2 = "(not C)";
		final String expectedString3 = "((not C) and (not Z) and ((A and B and (X or Y)) or ((not C) and (not Z))))";
		// the order of conjunct/disjuncts might differ
		final String expectedString4 = "(r only A)";
		final String expectedString5 = "(inverse(r) only (A and B))";
		final String expectedString6 = "(r some A)";
		
		final String actualString1 = Renderer.writeClassExpression(a_and_b);
		final String actualString2 = Renderer.writeClassExpression(not_c);
		final String actualString3 = Renderer.writeClassExpression(inter3);
		final String actualString4 = Renderer.writeClassExpression(r_only_A);
		final String actualString5 = Renderer.writeClassExpression(inv_r_only_A_and_B);
		final String actualString6 = Renderer.writeClassExpression(r_some_A);
		
		assertEquals(expectedString1, actualString1);
		assertEquals(expectedString2, actualString2);
		assertEquals(expectedString3, actualString3);
		assertEquals(expectedString4, actualString4);
		assertEquals(expectedString5, actualString5);
		assertEquals(expectedString6, actualString6);
		
		System.out.println(new Throwable().getStackTrace()[0].getMethodName() + " >> Test successful.");
	}*/
	
}