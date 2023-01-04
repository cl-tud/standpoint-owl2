package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class Renderer {
	
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
	 * @return String representation of given OWLClassExpression in Manchester syntax.
	 */
	public static String writeClassExpression(OWLClassExpression expr) {
		final ClassExpressionType type = expr.getClassExpressionType();
		//System.out.println("Renderer >> Expression: " + expr);
		String classID;
		String exprString;
		OWLClassExpression[] components;
		if (type == ClassExpressionType.OBJECT_UNION_OF) {
			components = expr.disjunctSet().toArray(OWLClassExpression[]::new);
			exprString = "(";
			for (int i = 0; i < components.length; i++) {
				exprString += writeClassExpression(components[i]);
				if (i < components.length - 1) {
					exprString += " or ";
				}
			}
			exprString += ")";
			return exprString;
		} else if (type == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			components = expr.conjunctSet().toArray(OWLClassExpression[]::new);
			exprString = "(";
			for (int i = 0; i < components.length; i++) {
				exprString += writeClassExpression(components[i]);
				if (i < components.length - 1) {
					exprString += " and ";
				}
			}
			exprString += ")";
			return exprString;
		} else if (type == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
			components = expr.nestedClassExpressions().toArray(OWLClassExpression[]::new);
			return "(not " + writeClassExpression(getInnerExpressionOfComplement(components)) + ")";
		} else if (type == ClassExpressionType.OWL_CLASS) {
			classID = expr.classesInSignature().toArray(OWLClass[]::new)[0].toStringID();
			return classID.substring(classID.lastIndexOf("#") + 1);
		} else {
			System.out.println("Unsupported ClassExpressionType " + type + ".");
			return "?";
		}
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
			len = expressions[i].nestedClassExpressions().toArray(OWLClassExpression[]::new).length;
			if (len > max) {
				max = len;
				maxIndex = i;
			}
		}
		//System.out.println("Renderer >> Index: " + maxIndex);
		return expressions[maxIndex];
	}
	
	private static OWLClassExpression getInnerExpressionOfComplement(OWLClassExpression[] expressions) throws IllegalArgumentException {
		OWLClassExpression topExpr = getTopLevelExpression(expressions);
		for (OWLClassExpression expr : expressions) {
			if (topExpr.compareTo(expr.getObjectComplementOf()) == 0) {
				return expr;
			}
		}
		throw new IllegalArgumentException("Argument array did not represent subexpressions of OWLObjectComplementOf.");
	}

}