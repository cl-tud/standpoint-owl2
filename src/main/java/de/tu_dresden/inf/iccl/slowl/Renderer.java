package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRestriction;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;


public class Renderer {
	
	private SimpleShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	private ManchesterOWLSyntaxOWLObjectRendererImpl manRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	public Renderer() {
		manRenderer.setShortFormProvider(shortFormProvider);
	}
	
	public String writeClassExpression(OWLClassExpression expr) {
		return replaceEscapeChars(manRenderer.render(expr));
	}
	
	// needs testing (still unused) //
	public String axiomToXML(OWLAxiom axiom) throws IllegalArgumentException {
		String result = null;
		AxiomType type = axiom.getAxiomType();
		
		if (type == AxiomType.SUBCLASS_OF) {
			OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
			OWLClassExpression subClass = subClassOfAxiom.getSubClass();
			OWLClassExpression superClass = subClassOfAxiom.getSuperClass();
			
			result = "<SubClassOf>\n" +
					 "  <LHS>" + writeClassExpression(subClass)   + "</LHS>\n" +
					 "  <RHS>" + writeClassExpression(superClass) + "</RHS>\n" +
					 "</SubClassOf>"; // throws IllegalArguemntException
				
		} else if (type == AxiomType.EQUIVALENT_CLASSES) {
			OWLEquivalentClassesAxiom equivalentClassesAxiom = (OWLEquivalentClassesAxiom) axiom;
			OWLSubClassOfAxiom[] subClassOfAxioms = equivalentClassesAxiom.asOWLSubClassOfAxioms().toArray(new OWLSubClassOfAxiom[0]);
			
			if (subClassOfAxioms.length != 2) { // we assume only one pair of OWLSubClassOfAxioms
				throw new IllegalArgumentException("EquivalentClassesAxiom should have exactly 2 operands.");
			}
			
			result = "<EquivalentClasses>\n" +
					 "  <LHS>" + writeClassExpression(subClassOfAxioms[0].getSubClass())   + "</LHS>\n" +
					 "  <RHS>" + writeClassExpression(subClassOfAxioms[0].getSuperClass()) + "</RHS>\n" +
					 "</EquivalentClasses>"; // throws IllegalArgumentException
					 
		} else {
			throw new IllegalArgumentException("Unsupported AxiomType: " + type + ".");
		}
		
		return result;
	}
	
	// PUBLIC STATIC METHODS //
	
	public static String replaceEscapeChars(String s) {
		return s.replace("&quot;", "\"").replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("\n", "");
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
	
	/**
	 * @args expressions array-representation of the nested class expressions of an OWLClassExpression.
	 * @return top-level OWLClassExpression.
	 */
	public static OWLClassExpression getTopLevelExpression(OWLClassExpression[] expressions) {
		int max = 0;
		int maxIndex = 0;
		int len;
		for (int i = 0; i < expressions.length; i++) {
			len = expressions[i].getNestedClassExpressions().size();
			if (len > max) {
				max = len;
				maxIndex = i;
			}
		}
		return expressions[maxIndex];
	}
	
	public static OWLClassExpression getInnerExpressionOfComplement(OWLClassExpression[] expressions) throws IllegalArgumentException {
		OWLClassExpression topExpr = getTopLevelExpression(expressions);
		for (OWLClassExpression expr : expressions) {
			if (topExpr.compareTo(expr.getObjectComplementOf()) == 0) {
				return expr;
			}
		}
		throw new IllegalArgumentException("Argument array did not represent subexpressions of OWLObjectComplementOf.");
	}
	
	public static OWLClassExpression getClassExpressionOfObjectRestriction(OWLObjectRestriction objRestriction) {
		return objRestriction.nestedClassExpressions().filter(e -> e.compareTo(objRestriction) != 0).toArray(OWLClassExpression[]::new)[0];
	}
}