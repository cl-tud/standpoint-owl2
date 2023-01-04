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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLWriter;

public class Translator {
	int iPrecisifications;
	Set<String> spAxiomNames;
	Set<String> spNames;
	SPParser parser;
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLOntology ontology = null;
	OWLOntology outputOntology = null;
	Map<String, String[]> m;
	OutputStream out;
	String ontologyIRIString;
	IRI ontologyIRI;
	IRI outputOntologyIRI;
	
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
			
			outputOntologyIRI = IRI.create(ontologyIRI + "_translated");
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
	  * Transforms standpoint expression to RDF/XML String (OWL2).
	  *
	  * @param prc 		integer denoting a precisification
	  * @param spExpr	the standpoint expression
	  */
	public String transExpr(int prc, String spExpr) {
		if (spNames.contains(spExpr)) {
			// TO DO //
			// need to use RDFXMLWriter or manage output ontology first and then write (probably)
		}
		
		// TO DO //
		return null;
	}
	
	
}