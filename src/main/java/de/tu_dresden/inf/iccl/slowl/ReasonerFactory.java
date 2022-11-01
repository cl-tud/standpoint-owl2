package de.tu_dresden.inf.iccl.slowl;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ReasonerFactory implements OWLReasonerFactory {
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		return new Reasoner(ontology);
	}
	
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
		return new Reasoner(ontology);
	}
	
	public OWLReasoner createReasoner(OWLOntology ontology) {
		return new Reasoner(ontology);
	}
	
	public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
		return new Reasoner(ontology);
	}
	
	public String getReasonerName() {
		return getClass().getPackage().getImplementationTitle();
	}
}