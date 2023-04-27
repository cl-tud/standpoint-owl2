package de.tu_dresden.inf.iccl.slowl;

import java.util.Arrays;
import java.util.Set;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

public class QueryParser {
	
	private static Set<String> axOps = Set.of("sub", "eq");
	
	private String query;
	
	public QueryParser(String s) {
		query = s;
	}
	
	public String parse() throws ParseException {
		if (query == null || query.equals("")) {
			throw new ParseException("Query was empty.", 0);
		}
		
		String opStart;
		String opEnd;
		String opClose;
		String[] spExprs = StringUtils.substringsBetween(query, "[", "]");
		if (spExprs == null) {
			spExprs = StringUtils.substringsBetween(query, "<", ">");
			if (spExprs == null) {
				throw new ParseException("Expected standpoint operator of the form \"[ SP_name ]\" or \"< SP_name >\".", 0);
			} else if (spExprs.length > 1) {
				throw new ParseException("Query cannot have multiple standpoint operators.", 0);
			}
			opStart = "<Diamond>\n";
			opEnd = "</Diamond>\n";
			opClose = ">";
		} else if (spExprs.length > 1) {
			throw new ParseException("Query cannot have multiple standpoint operators.", 0);
		} else {
			opStart = "<Box>\n";
			opEnd = "</Box>\n";
			opClose = "]";
		}
		
		String spExpr = parseSPExpr(spExprs[0]);
		
		String axPart = query.substring(query.indexOf("(", query.indexOf(opClose)) + 1, query.lastIndexOf(")"));
		
		String axStart;
		String axEnd;
		String axOp;
		int subCount = StringUtils.countMatches(axPart, " sub ");
		int eqCount = StringUtils.countMatches(axPart, " eq ");
		if (subCount == 0) {
			if (eqCount == 0) {
				throw new ParseException("Could not find \" sub \" or \" eq \" in axiom part of query.", 0);
			} else if (eqCount > 1) {
				throw new ParseException("Query cannot contain multiple equivalent classes axioms.", 0);
			}
			axStart = "<EquivalentClasses>\n";
			axEnd = "</EquivalentClasses>\n";
			axOp = " eq ";
		} else if (subCount > 1) {
			throw new ParseException("Query cannot contain chains of subclass axioms.", 0);
		} else {
			axStart = "<SubClassOf>\n";
			axEnd = "</SubClassOf>\n";
			axOp = " sub ";
		}
			
		String[] classExprs = StringUtils.splitByWholeSeparator(axPart, axOp);
		if (classExprs.length != 2) {
			throw new ParseException("Could not parse operands of axiom: " + Arrays.deepToString(classExprs), 0);
		}
		String lhs = "<LHS>" + classExprs[0].trim() + "</LHS>\n";
		String rhs = "<RHS>" + classExprs[1].trim() + "</RHS>\n";
		
		return opStart + spExpr + axStart + lhs + rhs + axEnd + opEnd;
	}
	
	private static String parseSPExpr(String expr) throws ParseException {
		if (expr.equals("")) {
			throw new ParseException("Empty standpoint expression.", 0);
		}
		
		if (Translator.isSPName(expr)) {
			return "<Standpoint name=\"" + expr + "\"/>\n";
		} else {
			throw new ParseException("Complex standpoint expressions are currently not supported in this syntax; please use an XML query.", 0);
		}
		
	}
	
}