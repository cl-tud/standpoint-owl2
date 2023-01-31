package de.tu_dresden.inf.iccl.slowl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

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
	
	// needs testing //
	public static String axiomToXML(OWLAxiom axiom) throws IllegalArgumentException {
		String result = null;
		AxiomType type = axiom.getAxiomType();
		
		if (type == AxiomType.SUBCLASS_OF) {
			OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
			OWLClassExpression subClass = subClassOfAxiom.getSubClass();
			OWLClassExpression superClass = subClassOfAxiom.getSuperClass();
			
			result = "<SubClassOf>\n" +
					 "  <LHS>" + writeClassExpression(subClass)   + "</LHS>\n" +
					 "  <RHS>" + writeClassExpression(superClass) + "</RHS>\n" +
					 "</SubClassOf>";
				
		} else if (type == AxiomType.EQUIVALENT_CLASSES) {
			OWLEquivalentClassesAxiom equivalentClassesAxiom = (OWLEquivalentClassesAxiom) axiom;
			OWLSubClassOfAxiom[] subClassOfAxioms = equivalentClassesAxiom.asOWLSubClassOfAxioms().toArray(OWLSubClassOfAxiom[]::new);
			
			if (subClassOfAxioms.length != 2) { // we assume only one pair of OWLSubClassOfAxioms
				throw new IllegalArgumentException("EquivalentClassesAxiom should have exactly 2 operands.");
			}
			
			result = "<EquivalentClasses>\n" +
					 "  <LHS>" + writeClassExpression(subClassOfAxioms[0].getSubClass())   + "</LHS>\n" +
					 "  <RHS>" + writeClassExpression(subClassOfAxioms[0].getSuperClass()) + "</RHS>\n" +
					 "</EquivalentClasses>";
					 
		} else {
			throw new IllegalArgumentException("Unsupported AxiomType: " + type + ".");
		}
		
		return result;
	}
	
	/**
	 * @return String representation of given OWLClassExpression in Manchester syntax.
	 */
	public static String writeClassExpression(OWLClassExpression expr) {
		final ClassExpressionType type = expr.getClassExpressionType();
		String classID;
		String propertyID;
		String exprString;
		OWLObjectPropertyExpression objectPropertyExpr;
		OWLObjectProperty objectProperty;
		OWLObjectRestriction objectRestriction;
		OWLClassExpression[] components;
		if (type == ClassExpressionType.OWL_CLASS) {
			classID = expr.classesInSignature().toArray(OWLClass[]::new)[0].toStringID();
			return classID.substring(classID.lastIndexOf("#") + 1);
		} else if (type == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
			objectRestriction = (OWLObjectRestriction) expr;
			objectPropertyExpr = objectRestriction.getProperty();
			objectProperty = objectPropertyExpr.getNamedProperty();
			propertyID = objectProperty.toStringID();
			propertyID = propertyID.substring(propertyID.lastIndexOf("#") + 1);
			exprString = "(";
			if (objectPropertyExpr.compareTo(objectProperty.getInverseProperty()) != 0) { // role name
				exprString += propertyID + " only " + writeClassExpression(getClassExpressionOfObjectRestriction(objectRestriction));
			} else { // inverse role
				exprString += "inverse(" + propertyID + ") only " + writeClassExpression(getClassExpressionOfObjectRestriction(objectRestriction));
			}
			exprString += ")";
			return exprString;
		} else if (type == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			objectRestriction = (OWLObjectRestriction) expr;
			objectPropertyExpr = objectRestriction.getProperty();
			objectProperty = objectPropertyExpr.getNamedProperty();
			propertyID = objectProperty.toStringID();
			propertyID = propertyID.substring(propertyID.lastIndexOf("#") + 1);
			exprString = "(";
			if (objectPropertyExpr.compareTo(objectProperty.getInverseProperty()) != 0) { // role name
				exprString += propertyID + " some " + writeClassExpression(getClassExpressionOfObjectRestriction(objectRestriction));
			} else { // inverse role
				exprString += "inverse(" + propertyID + ") some " + writeClassExpression(getClassExpressionOfObjectRestriction(objectRestriction));
			}
			exprString += ")";
			return exprString;
		} else if (type == ClassExpressionType.OBJECT_UNION_OF) {
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
		} else {
			System.out.println("Renderer >> ERROR: Unsupported ClassExpressionType: " + type + ".");
			return ""; // maybe change to exception
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