package de.tu_dresden.inf.iccl.slowl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.util.Version;

public class Reasoner implements OWLReasoner {
	protected final OWLOntology m_rootOntology;
	
	public Reasoner(OWLOntology rootOntology){
		m_rootOntology = rootOntology;
	}
	
	public void dispose(){
	}
	
	public void flush(){
	}
	
	public Node<OWLClass> getBottomClassNode() {
		return new OWLClassNode();
	}
	
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		return new OWLDataPropertyNode();
	}
	
	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		return new OWLObjectPropertyNode();
	}
	
	public BufferingMode getBufferingMode(){
		return BufferingMode.NON_BUFFERING;
	}
	
	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
		return new OWLClassNodeSet();
	}
	
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
		Set<OWLLiteral> owlLiterals = new HashSet<OWLLiteral>();
		return owlLiterals;
	}
	
	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
		return new OWLNamedIndividualNodeSet();
	}
	
	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
		return new OWLClassNodeSet();
	}
	
	public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
		return new OWLDataPropertyNodeSet();
	}
	
	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {
		return new OWLObjectPropertyNodeSet();
	}
	
	public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
		return new OWLClassNode();
	}
	
	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
		return new OWLDataPropertyNode();
	}
	
	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {
		return new OWLObjectPropertyNode();
	}
	
	public FreshEntityPolicy getFreshEntityPolicy(){
		return FreshEntityPolicy.DISALLOW;
	}
	
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy(){
		return IndividualNodeSetPolicy.BY_SAME_AS;
	}
	
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
		return new OWLNamedIndividualNodeSet();
	}
	
	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
		return new OWLObjectPropertyNode();
	}
	
	public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
		return new OWLClassNodeSet();
	}
	
	public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
		return new OWLClassNodeSet();
	}
	
	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe) {
		return new OWLNamedIndividualNodeSet();
	}
	
	public Set<OWLAxiom> getPendingAxiomAdditions(){
		Set<OWLAxiom> owlAxioms = new HashSet<OWLAxiom>();
		return owlAxioms;
	}
	
	public Set<OWLAxiom> getPendingAxiomRemovals(){
		Set<OWLAxiom> owlAxioms = new HashSet<OWLAxiom>();
		return owlAxioms;
	}

	public List<OWLOntologyChange> getPendingChanges(){
		List<OWLOntologyChange> owlChanges = new ArrayList<OWLOntologyChange>();
		return owlChanges;
	}
	
	public Set<InferenceType> getPrecomputableInferenceTypes() {
		Set<InferenceType> inferenceTypes = new HashSet<InferenceType>();
		return inferenceTypes;
	}
	
	public String getReasonerName() {
		return Reasoner.class.getPackage().getImplementationTitle();
	}
	
	public Version getReasonerVersion() {
		String versionString = Reasoner.class.getPackage().getImplementationVersion();
		String[] splitted;
		int filled = 0;
		int version[] = new int[4];
		if (versionString != null) {
			splitted = versionString.replaceAll("[^\\d.]", "").split("\\.");
			while (filled < splitted.length && filled < version.length) {
				String part = splitted[filled];
				if (part.length() > 8) {
					part = part.substring(0, 8);
				}
				version[filled] = Integer.parseInt(part);
				filled++;
			}
		}
		while (filled < version.length) {
			version[filled] = 0;
			filled++;
		}
		return new Version(version[0], version[1], version[2], version[3]);
	}
	
	public OWLOntology getRootOntology(){
		return m_rootOntology;
	}
	
	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
		return new OWLNamedIndividualNode();
	}
	
	public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
		return new OWLClassNodeSet();
	}
	
	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
		return new OWLDataPropertyNodeSet();
	}
	
	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
		return new OWLObjectPropertyNodeSet();
	}
	
	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
		return new OWLClassNodeSet();
	}
	
	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
		return new OWLDataPropertyNodeSet();
	}
	
	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
		return new OWLObjectPropertyNodeSet();
	}
	
	public long getTimeOut(){
		return 0;
	}
	
	public Node<OWLClass> getTopClassNode() {
		return new OWLClassNode();
	}
	
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		return new OWLDataPropertyNode();
	}
	
	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		return new OWLObjectPropertyNode();
	}
	
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
		return new OWLClassNodeSet();
	}
	
	public Node<OWLClass> getUnsatisfiableClasses() {
		return new OWLClassNode();
	}
	
	public void handleChanges(Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2) {
	}
	
	public void interrupt() {
	}
	
	public boolean isConsistent() {
		return false;
	}
	
	public boolean isEntailed(OWLAxiom axiom) {
		return false;
	}
	
	public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
		return false;
	}
	
	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		return false;
	}
	
	public boolean isPrecomputed(InferenceType inferenceType) {
		return false;
	}
	
	public boolean isSatisfiable(OWLClassExpression classExpression) {
		return false;
	}
	
	public void precomputeInferences(InferenceType... inferenceTypes) {
	}
}