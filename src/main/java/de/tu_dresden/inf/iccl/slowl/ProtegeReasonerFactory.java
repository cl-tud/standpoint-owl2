package de.tu_dresden.inf.iccl.slowl;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class ProtegeReasonerFactory extends AbstractProtegeOWLReasonerInfo {
	protected final ReasonerFactory factory = new ReasonerFactory();
	
	public BufferingMode getRecommendedBuffering() {
		return BufferingMode.BUFFERING;
	}
	
	public OWLReasonerFactory getReasonerFactory() {
		return factory;
	}
	
	public OWLReasonerConfiguration getConfiguration(ReasonerProgressMonitor monitor) {
		return new SimpleConfiguration(monitor);
	}
	
	public void initialise() throws Exception {
	}
	
	public void dispose() throws Exception {
	}
}