package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLWriter;

/* M_<spName>_<int> are (not yet reserved) new OWLClasses.
 *
 * universal_role is the (not yet reserved) univeral OWLObjectProperty.
 */

public class Translator {
	int iPrecisifications;
	Set<String> spAxiomNames;
	Set<String> spNames;
	SPParser parser;
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLDataFactory dataFactory = manager.getOWLDataFactory();
	OWLOntology ontology = null;
	OWLOntology outputOntology = null;
	OutputStream out;
	String ontologyIRIString;
	String outputOntologyIRIString;
	IRI ontologyIRI;
	IRI outputOntologyIRI;
	
	// maps standpoint name to set of concepts of the form M_<spName>_<int> (as string)
	Map<String, String[]> m;
	
	// universal role
	OWLObjectProperty u;
	
	public Translator(File f) {
		parser = new SPParser();
		
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		if (ontology != null) {
			ontologyIRIString = parser.getOntologyIRIString(ontology);
			ontologyIRI = IRI.create(ontologyIRIString);
			
			outputOntologyIRIString = ontologyIRIString + "_trans";
			outputOntologyIRI = IRI.create(outputOntologyIRIString);
			try {
				outputOntology = manager.createOntology(outputOntologyIRI);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			parser.countDiamonds(ontology);
			iPrecisifications = parser.diamondCount;
			parser.getNames(ontology);
			spAxiomNames = parser.spAxiomNames;
			spNames = parser.spNames;
			
			u = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#universal_role"));
		
			m = new HashMap<String, String[]>();
			String[] new_concepts;
			try {
				for (String name : spNames) {
					new_concepts = new String[iPrecisifications];
					for (int i = 0; i < iPrecisifications; i++) {
						new_concepts[i] = "M" + "_" + name + "_" + i;
					}
					m.put(name, new_concepts);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			Set<String> new_concepts_set = new HashSet<String>();
			for (String[] arr : m.values()) {
				for (String s : arr) {
					new_concepts_set.add(s);
				}
			}
			System.out.print(this + " >> New concepts: ");
			Renderer.printSet(new_concepts_set);
			
			// TO DO //
		} else {
			// TO DO //
			outputOntologyIRI = IRI.create("outputOntology");
		}
	}
	
	public void setOutputFile(String filename) {
		// set file extension to .owl
		int k = filename.lastIndexOf(".");
		if (k > 0) {
			if (filename.indexOf(".owl", k) < 0) {
				filename = filename + ".owl";
			}
		} else {
			filename = filename + ".owl";
		}
		
		// create OutputStream
		try {
			out = new FileOutputStream(filename);
		} catch (Exception e) {
			e.printStackTrace();
			// default OutputStream ?
		}
	}
	
	/**
	 * Transforms standpoint expression to OWLClassExpression.
	 *
	 * @param prc 		integer denoting a precisification
	 * @param spExpr	standpoint expression
	 */
	public OWLClassExpression transExpr(int prc, String spExpr) throws IllegalArgumentException {
		if (spExpr.toLowerCase().trim().indexOf("<standpoint ") == 0) {
			String spName = parser.getFirstSPName(spExpr);
			if (spName == null) {
				throw new IllegalArgumentException("Illegal standpoint expression; no standpoint name.");
			}
			
			// do we need to treat * separately? //
			
			return dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_" + spName + "_" + prc)));
		}
		
		String[] elements = parser.getRootAndChildElements(spExpr);
		if (elements.length != 3) {
			System.out.print(this + " >> Elements: ");
			for (String e : elements) {
				System.out.print(e + ", ");
			}
			throw new IllegalArgumentException("Illegal standpoint expression; not exactly 2 child elements.");
		}
		
		if (elements[0].equalsIgnoreCase("UNION")) {
			return dataFactory.getOWLObjectUnionOf(transExpr(prc, elements[1]), transExpr(prc, elements[2]));
		} else if (elements[0].equalsIgnoreCase("INTERSECTION")) {
			return dataFactory.getOWLObjectIntersectionOf(transExpr(prc, elements[1]), transExpr(prc, elements[2]));
		} else if (elements[0].equalsIgnoreCase("MINUS")) {
			return dataFactory.getOWLObjectIntersectionOf(transExpr(prc, elements[1]), dataFactory.getOWLObjectComplementOf(transExpr(prc, elements[2])));
		} else {
			throw new IllegalArgumentException("Illegal standpoint expression.");
		}
	}
	
	
}