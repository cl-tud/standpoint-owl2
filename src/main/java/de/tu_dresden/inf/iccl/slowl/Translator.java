package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.function.Predicate;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLWriter;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/* M_<spName>_<int> are (not yet reserved) new OWLClasses.
 *
 * universal_role is the (not yet reserved) universal OWLObjectProperty.
 */

public class Translator {
	// number of precisifications
	int iPrecisifications;
	
	// maps standpoint name to set of concepts of the form M_<spName>_<int> (as string)
	Map<String, String[]> m; // might not need this anymore
	
	private Set<String> spAxiomNames;
	private Set<String> spNames;
	private SPParser parser;
	private ManchesterOWLSyntaxParser manchesterParser = OWLManager.createManchesterParser();
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLDataFactory dataFactory = manager.getOWLDataFactory();
	private OWLOntology ontology = null;
	private OWLOntology outputOntology = null;
	private OutputStream out;
	private String ontologyIRIString;
	private String outputOntologyIRIString;
	private IRI ontologyIRI;
	private IRI outputOntologyIRI;
	private SimpleShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	private OWLEntityChecker checker;
	
	// universal role
	private OWLObjectProperty u;
	
	private Predicate<OWLEntity> has_underscore = e -> (e.toStringID().indexOf("_", e.toStringID().lastIndexOf("#")) > 0);
	private boolean bWarning = false;
	
	public Translator(File f) throws OWLOntologyCreationException {
		
		ontology = manager.loadOntologyFromOntologyDocument(f); // throws OWLOntologyCreationException
			
		if (ontology != null) {
			parser = new SPParser(ontology);
			
			if (ontology.signature().anyMatch(has_underscore)) {
				System.out.println(this + " >> WARNING: Ontology contains entities with underscores. Make sure not to use reserved class or object property names.");
				bWarning = true;
			}
			
			ontologyIRIString = parser.getOntologyIRIString(ontology);
			ontologyIRI = IRI.create(ontologyIRIString);
			
			outputOntologyIRIString = ontologyIRIString + "_trans";
			outputOntologyIRI = IRI.create(outputOntologyIRIString);
			
			outputOntology = manager.createOntology(outputOntologyIRI); // throws OWLOntologyCreationException
			
			manchesterParser.setDefaultOntology(ontology);
			checker = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(manager, Set.of(ontology), shortFormProvider));
			manchesterParser.setOWLEntityChecker(checker);
			
			parser.countDiamonds();
			iPrecisifications = parser.diamondCount;
			parser.parseAxioms();
			spAxiomNames = parser.spAxiomNames;
			spNames = parser.spNames;
			
			u = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#universal_role"));
		
			m = new HashMap<String, String[]>();
			String[] new_concepts;
			for (String name : spNames) {
				new_concepts = new String[iPrecisifications];
				for (int i = 0; i < iPrecisifications; i++) {
					new_concepts[i] = "M" + "_" + name + "_" + i;
				}
				m.put(name, new_concepts);
			}
		
			Set<String> new_concepts_set = new HashSet<String>();
			for (String[] arr : m.values()) {
				for (String s : arr) {
					new_concepts_set.add(s);
				}
			}
			//System.out.print(this + " >> New concepts: ");
			//Renderer.printSet(new_concepts_set);
			
			// (TO DO) //
		} else {
			// (TO DO) //
			parser = new SPParser();
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
	 * Gathers the String representations of all standpointAxioms and booleanCombinations of the input ontology.
	 * For booleanCombinations, named standpointAxioms are being replaced by their corresponding axioms.
	 */
	public Set<String> compileSPAxiomsToTranslate() {
		Set<String> results = new HashSet<String>();
		
		// add annotated SubClassOf and EquivalentClasses axioms
		results.addAll(parser.spAxioms);
		
		// add booleanCombinations, where named standpointAxioms are being replaced by their corresponding axioms
		Set<String> booleanCombinations = parser.getBooleanCombinations();
		String newBoolComb;
		String boolCombLower;
		String toBeReplaced;
		String spAxiomName;
		String spAxiom;
		int i, j;
		
		outer:
		for (String boolComb : booleanCombinations) {
			newBoolComb = boolComb;
			
			// replace named standpoint axioms by their definitions
			while (true) {
				boolCombLower = newBoolComb.toLowerCase();
				i = boolCombLower.indexOf("<standpointaxiom ");
				if (i < 0) { // nothing to replace
					break;
				}
				j = boolCombLower.indexOf("/>", i);
				if (j < 0) {
					System.out.println(this + " >> ERROR: Illegal boolean combination\n <[" + boolComb + "]>.");
					continue outer;
				}
				toBeReplaced = boolComb.substring(i, j + 2);
				spAxiomName = parser.getFirstSPAxiomName(toBeReplaced);
				if (spAxiomName == null) {
					System.out.println(this + " >> ERROR: Missing standpoint axiom name in\n <[" + boolComb + "]>.");
					continue outer;
				}
				spAxiom = parser.spAxiomNameMap.get(spAxiomName);
				if (spAxiom == null) {
					System.out.println(this + " >> ERROR: Undefined standpoint axiom name in\n <[" + boolComb + "]>.");
					continue outer;
				} else if (spAxiom.toLowerCase().contains("<standpointaxiom ")) {
					System.out.println(this + " >> ERROR: Illegal standpoint axiom\n [" + spAxiom + "].");
					continue outer;
				}
				newBoolComb = boolComb.replace(toBeReplaced, spAxiom.trim());
			}
			
			results.add(newBoolComb);
		}
		
		return results;
	}
	
	/**
	 * Adds SubClassOfAxioms of the form "OWLThing subsumed by trans(prc, phi)" to the output ontology,
	 * where prc is a precisification and phi is an axiom to be translated.
	 */
	// needs testing
	public void translateOntology() {
		if (!outputOntology.isEmpty()) {
			System.out.println(this + " >> WARNING: Output ontology already contains axioms before translation.");
		}
		
		Set<OWLSubClassOfAxiom> translatedAxioms = new HashSet<OWLSubClassOfAxiom>();
		OWLClassExpression expr;
		
		// translate normal (non-standpoint) axioms
		OWLAxiom[] aBoxAxioms = ontology.aboxAxioms(Imports.INCLUDED).filter(a -> parser.supportedABoxAxiomTypes.contains(a.getAxiomType())).toArray(OWLAxiom[]::new);
		
		forABox:
		for (OWLAxiom ax : aBoxAxioms) {
			for (int i = 0; i < iPrecisifications; i++) {
				try {
					expr = transPos(i, ax);
				} catch (IllegalArgumentException e) {
					System.out.println(this + " >> SKIP: Could not translate ABox axiom.\n" + e.getMessage());
					continue forABox;
				}
				translatedAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
			}
		}
		
		// translate standpoint axioms
		Set<String> spAxioms = compileSPAxiomsToTranslate();
		
		forSP:
		for (String ax : spAxioms) {
			for (int i = 0; i < iPrecisifications; i++) {
				try {
					expr = trans(i, ax);
				} catch (IllegalArgumentException e) {
					System.out.println(this + " >> SKIP: Could not translate standpoint axiom.\n" + e.getMessage());
					continue forSP;
				}
				translatedAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
			}
		}
		
		// add translated axioms to outputOntology
		ChangeApplied change;
		int axiomCount;
		change = outputOntology.addAxioms(translatedAxioms);
		if (change != ChangeApplied.SUCCESSFULLY) {
			axiomCount = outputOntology.getAxiomCount(Imports.EXCLUDED);
			System.out.println(this + " >> ERROR: could not add " + (translatedAxioms.size() - axiomCount) + " translated axioms to output ontology.");
		}
		
		// add universal axioms to outputOntology
		for (int i = 0; i < iPrecisifications; i++) {
			expr = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_" + i)));
			change = outputOntology.addAxiom(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
			if (change != ChangeApplied.SUCCESSFULLY) {
				System.out.println(this + " >> ERROR: Could not add universal axiom [" + i + "] to output ontology.");
			}
		}
		
		axiomCount = outputOntology.getAxiomCount(Imports.EXCLUDED);
		System.out.println(this + " >> Added " + axiomCount + " axioms to output ontology.");
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
	
	/**
	 * Translates (positive) OWLAxiom to OWLClassExpression.
	 *
	 * @param prc	integer denoting a specific precisification
	 * @param axiom	OWLAxiom
	 */
	// needs more testing //
	public OWLClassExpression transPos(int prc, OWLAxiom axiom) throws IllegalArgumentException {
		AxiomType type = axiom.getAxiomType();
		OWLClassExpression nominal1;
		OWLClassExpression nominal2;
		if (type == AxiomType.SUBCLASS_OF) {
			OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
			OWLClassExpression subClass = subClassOfAxiom.getSubClass();
			OWLClassExpression superClass = subClassOfAxiom.getSuperClass();
			OWLClassExpression normExpr = normalize(prc, dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(subClass), superClass));
			return dataFactory.getOWLObjectAllValuesFrom(u, normExpr);
		} else if (type == AxiomType.CLASS_ASSERTION) {
			OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;
			OWLIndividual individual = classAssertionAxiom.getIndividual();
			OWLClassExpression nominal = dataFactory.getOWLObjectOneOf(rebaseToOutput(individual)); // throws IllegalArgumentException
			OWLClassExpression classExpr = classAssertionAxiom.getClassExpression();
			return dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nominal, normalize(prc, classExpr)));
		} else if (type == AxiomType.OBJECT_PROPERTY_ASSERTION) {
			OWLObjectPropertyAssertionAxiom roleAssertionAxiom = ((OWLObjectPropertyAssertionAxiom) axiom).getSimplified();
			OWLIndividual individual1 = roleAssertionAxiom.getSubject();
			OWLIndividual individual2 = roleAssertionAxiom.getObject();
			nominal1 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individual1)); // throws IllegalArgumentException
			nominal2 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individual2)); // throws IllegalArgumentException
			String roleName = shortFormProvider.getShortForm(roleAssertionAxiom.getProperty().asOWLObjectProperty());
			if (roleName.equals("")) {
				throw new IllegalArgumentException("Anonymous entity cannot be rebased.");
			}
			OWLObjectProperty role = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#" + roleName + "_" + prc));
			return dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nominal1, dataFactory.getOWLObjectSomeValuesFrom(role, nominal2)));
		} else if (type == AxiomType.SAME_INDIVIDUAL) {
			// we assume only one pair of individuals
			OWLSameIndividualAxiom sameIndividualAxiom = (OWLSameIndividualAxiom) axiom;
			OWLIndividual[] individuals = sameIndividualAxiom.individualsInSignature().toArray(OWLIndividual[]::new);
			if (individuals.length != 2) {
				throw new IllegalArgumentException("OWLSameIndividualAxiom is not exactly one pair of individuals.");	
			}
			nominal1 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individuals[0])); // throws IllegalArgumentException
			nominal2 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individuals[1])); // throws IllegalArgumentException
			return dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nominal1, nominal2));
		} else {
			throw new IllegalArgumentException("Unexpected AxiomType: " + type);
		}
	}
	
	/**
	 * Translates (negated) OWLAxiom to OWLClassExpression.
	 *
	 * @param prc	integer denoting a specific precisification
	 * @param axiom	OWLAxiom
	 */
	// needs more testing //
	public OWLClassExpression transNeg(int prc, OWLAxiom axiom) throws IllegalArgumentException {
		AxiomType type = axiom.getAxiomType();
		OWLClassExpression nominal1;
		OWLClassExpression nominal2;
		if (type == AxiomType.SUBCLASS_OF) {
			OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
			OWLClassExpression subClass = subClassOfAxiom.getSubClass();
			OWLClassExpression superClass = subClassOfAxiom.getSuperClass();
			OWLClassExpression normExpr = normalize(prc, dataFactory.getOWLObjectIntersectionOf(subClass, dataFactory.getOWLObjectComplementOf(superClass)));
			return dataFactory.getOWLObjectSomeValuesFrom(u, normExpr);
		} else if (type == AxiomType.CLASS_ASSERTION) {
			OWLClassAssertionAxiom classAssertionAxiom = (OWLClassAssertionAxiom) axiom;
			OWLIndividual individual = classAssertionAxiom.getIndividual();
			OWLClassExpression nominal;
			nominal = dataFactory.getOWLObjectOneOf(rebaseToOutput(individual)); // throws IllegalArgumentException
			OWLClassExpression classExpr = classAssertionAxiom.getClassExpression();
			return dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nominal, normalize(prc, dataFactory.getOWLObjectComplementOf(classExpr))));
		} else if (type == AxiomType.OBJECT_PROPERTY_ASSERTION) {
			OWLObjectPropertyAssertionAxiom roleAssertionAxiom = ((OWLObjectPropertyAssertionAxiom) axiom).getSimplified();
			OWLIndividual individual1 = roleAssertionAxiom.getSubject();
			OWLIndividual individual2 = roleAssertionAxiom.getObject();
			nominal1 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individual1)); // throws IllegalArgumentException
			nominal2 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individual2)); // throws IllegalArgumentException
			String roleName = shortFormProvider.getShortForm(roleAssertionAxiom.getProperty().asOWLObjectProperty());
			if (roleName.equals("")) {
				throw new IllegalArgumentException("Anonymous entity cannot be rebased.");
			}
			OWLObjectProperty role = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#" + roleName + "_" + prc));
			return dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nominal1, dataFactory.getOWLObjectAllValuesFrom(role, dataFactory.getOWLObjectComplementOf(nominal2))));
		} else if (type == AxiomType.SAME_INDIVIDUAL) {
			// we assume only one pair of individuals
			OWLSameIndividualAxiom sameIndividualAxiom = (OWLSameIndividualAxiom) axiom;
			OWLIndividual[] individuals = sameIndividualAxiom.individualsInSignature().toArray(OWLIndividual[]::new);
			if (individuals.length != 2) {
				throw new IllegalArgumentException("OWLSameIndividualAxiom is not exactly one pair of individuals.");	
			}
			nominal1 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individuals[0])); // throws IllegalArgumentException
			nominal2 = dataFactory.getOWLObjectOneOf(rebaseToOutput(individuals[1])); // throws IllegalArgumentException
			return dataFactory.getOWLObjectSomeValuesFrom(u, dataFactory.getOWLObjectIntersectionOf(nominal1, dataFactory.getOWLObjectComplementOf(nominal2)));
		} else {
			throw new IllegalArgumentException("Unexpected AxiomType: " + type);
		}		
	}
	
	/**
	 * Translates boolean combination of standpoint axioms to OWLClassExpression.
	 *
	 * @param prc		integer denoting a specific precisification
	 * @param boolComb	normalized boolean combination of standpoint axioms as XML String
	 */
	public OWLClassExpression trans(int prc, String boolComb) throws IllegalArgumentException {
		OWLClassExpression result = null;
		
		String[] elements = parser.getRootAndChildElements(boolComb);
		if (elements[0] == null) {
			throw new IllegalArgumentException("Illegal boolean combination.");
		}
		
		if (elements.length == 2 && elements[0].equalsIgnoreCase("booleanCombination")) {
			return trans(prc, elements[1]);
		}
		
		if (elements.length > 3 || elements.length < 2) {
			System.out.print(this + " >> Elements: ");
			for (String e : elements) {
				System.out.print(e + ", ");
			}
			throw new IllegalArgumentException("Illegal boolean combination; unexpected number of child elements.");
		}
		
		/* We assume a normalized booleanCombination, i.e.
		 * - <NOT> occurs only before <SubClassOf>, <EquivalentClasses>, <Box>, or <Diamond>;
		 * - <EquivalentClasses> can only have two children (<LHS>, <RHS>).
		 */
		OWLClassExpression expr;
		OWLSubClassOfAxiom subClassOfAxiom;
		OWLSubClassOfAxiom subClassOfAxiom2;
		Set<OWLClassExpression> disjunctSet;
		Set<OWLClassExpression> conjunctSet;
		String[] subElements;
		String[] subSubElements;
		if (elements[0].equalsIgnoreCase("NOT")) {
			if (elements.length != 2) {
				throw new IllegalArgumentException("Illegal boolean combination; <NOT> does not have exactly 1 child element.");
			}
			
			subElements = parser.getRootAndChildElements(elements[1]);
			if (subElements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; probably not normalized.");
			}
			
			if (subElements[0].equalsIgnoreCase("SubClassOf")) { // NOT SubClassOf
				try {
					subClassOfAxiom = createSubClassOfAxiom(subElements[1], subElements[2]);
				} catch (OWLParserException e) {
					e.printStackTrace();
					return null;
				}
				
				try {
					result = transNeg(prc, subClassOfAxiom);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			
			} else if (subElements[0].equalsIgnoreCase("EquivalentClasses")) { // NOT EquivalentClasses, i.e. (NOT SubClassOf) OR (NOT SubClassOf)
				try {
					subClassOfAxiom = createSubClassOfAxiom(subElements[1], subElements[2]);
					subClassOfAxiom2 = createSubClassOfAxiom(subElements[2].replace("RHS","LHS").replace("rhs","lhs"), subElements[1].replace("LHS","RHS").replace("lhs","rhs"));
				} catch (OWLParserException e) {
					e.printStackTrace();
					return null;
				}
				
				try {
					result = dataFactory.getOWLObjectUnionOf(transNeg(prc, subClassOfAxiom), transNeg(prc, subClassOfAxiom2));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				
			} else if (subElements[0].equalsIgnoreCase("Box")) {
				//System.out.println(this + " >> NOT BOX");
				subSubElements = parser.getRootAndChildElements(subElements[2]);
				if (subSubElements.length != 3) {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Box> does not have exactly 2 child elements.");
				}
				
				if (subSubElements[0].equalsIgnoreCase("SubClassOf")) { // NOT Box SubClassOf, i.e. Diamond (NOT SubClassOf)
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
					} catch (OWLParserException e) {
						e.printStackTrace();
						return null;
					}
				
					disjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(transExpr(i, subElements[1]), transNeg(i, subClassOfAxiom)));
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}
				
					result = dataFactory.getOWLObjectUnionOf(disjunctSet);
					
				} else if (subSubElements[0].equalsIgnoreCase("EquivalentClasses")) { // NOT Box EquivalentClasses, i.e. Diamond ((NOT SubClassOf) OR (NOT SubClassOf))
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
						subClassOfAxiom2 = createSubClassOfAxiom(subSubElements[2].replace("RHS","LHS").replace("rhs","lhs"), subSubElements[1].replace("LHS","RHS").replace("lhs","rhs"));
					} catch (OWLParserException e) {
						e.printStackTrace();
						return null;
					}
					
					disjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(transExpr(i, subElements[1]), dataFactory.getOWLObjectUnionOf(transNeg(prc, subClassOfAxiom), transNeg(prc, subClassOfAxiom2))));
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}
				
					result = dataFactory.getOWLObjectUnionOf(disjunctSet);
					
				} else {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Box> was not <SubClassOf> or <EquivalentClasses>.");
				}
				
			} else if (subElements[0].equalsIgnoreCase("Diamond")) {
				//System.out.println(this + " >> NOT DIAMOND");
				subSubElements = parser.getRootAndChildElements(subElements[2]);
				if (subSubElements.length != 3) {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Diamond> does not have exactly 2 child elements.");
				}
				
				if (subSubElements[0].equalsIgnoreCase("SubClassOf")) { // NOT Diamond SubClassOf, i.e. Box (NOT SubClassOf)
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
					} catch (OWLParserException e) {
						e.printStackTrace();
						return null;
					}
				
					conjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, subElements[1])).getNNF(), transNeg(i, subClassOfAxiom)));
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}
				
					result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
				
				} else if (subSubElements[0].equalsIgnoreCase("EquivalentClasses")) { // NOT Diamond EquivalentClasses, i.e. Box ((NOT SubClassOf) OR (NOT SubClassOf))
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
						subClassOfAxiom2 = createSubClassOfAxiom(subSubElements[2].replace("RHS","LHS").replace("rhs","lhs"), subSubElements[1].replace("LHS","RHS").replace("lhs","rhs"));
					} catch (OWLParserException e) {
						e.printStackTrace();
						return null;
					}
					
					conjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, subElements[1])).getNNF(), transNeg(i, subClassOfAxiom), transNeg(i, subClassOfAxiom2)));
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}
				
					result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
					
				} else {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Diamond> was not <SubClassOf> or <EquivalentClasses>.");
				}
				
				
				
			} else {
				throw new IllegalArgumentException("Illegal boolean combination; <NOT> not followed by <SubClassOf>, <Box>, or <Diamond>.");
			}
			
		} else if (elements[0].equalsIgnoreCase("AND")) {
			//System.out.println(this + " >> AND");
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <AND> does not have exactly 2 child elements.");
			}
			
			try {
				result = dataFactory.getOWLObjectIntersectionOf(trans(prc, elements[1]), trans(prc, elements[2]));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		} else if (elements[0].equalsIgnoreCase("OR")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <OR> does not have exactly 2 child elements.");
			}
			
			
			try {
				result = dataFactory.getOWLObjectUnionOf(trans(prc, elements[1]), trans(prc, elements[2]));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		} else if (elements[0].equalsIgnoreCase("Box")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <Box> does not have exactly 2 child elements.");
			}
			
			conjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 0; i < iPrecisifications; i++) {
				try {
					conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, elements[1])).getNNF(), trans(i, elements[2])));
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			
			result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
			
		} else if (elements[0].equalsIgnoreCase("Diamond")) {
			//System.out.println(this + " >> DIAMOND");
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <Diamond> does not have exactly 2 child elements.");
			}

			disjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 0; i < iPrecisifications; i++) {
				try {
					expr = trans(i, elements[2]);
					if (expr.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) { // flatten the intersections
						conjunctSet = expr.asConjunctSet();
						conjunctSet.add(transExpr(i, elements[1]));
						disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(conjunctSet));
					} else {
						disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(transExpr(i, elements[1]), expr));
					}
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			
			result = dataFactory.getOWLObjectUnionOf(disjunctSet);
			
		} else if (elements[0].equalsIgnoreCase("EquivalentClasses")) {
			// we treat <EquivalentClasses> as a conjunction of (two) <SubClassOf> elements
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <EquivalentClasses> does not have exactly 2 child elements.");
			}
			
			try {
				subClassOfAxiom = createSubClassOfAxiom(elements[1], elements[2]);
				subClassOfAxiom2 = createSubClassOfAxiom(elements[2].replace("RHS","LHS").replace("rhs","lhs"), elements[1].replace("LHS","RHS").replace("lhs","rhs"));
			} catch (OWLParserException e) {
				e.printStackTrace();
				return null;
			}
			
			try {
				result = dataFactory.getOWLObjectIntersectionOf(transPos(prc, subClassOfAxiom), transPos(prc, subClassOfAxiom2));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		} else if (elements[0].equalsIgnoreCase("SubClassOf")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <SubClassOf> does not have exactly 2 child elements.");
			}
			
			try {
				subClassOfAxiom = createSubClassOfAxiom(elements[1], elements[2]);
			} catch (OWLParserException e) {
				e.printStackTrace();
				return null;
			}
			
			try {
				result = transPos(prc, subClassOfAxiom);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		} else {
			throw new IllegalArgumentException("Illegal boolean combination; unknown operator: " + elements[0] + ".");
		}
		
		return result;
	}
	
	/**
	 * Creates an OWLSubClassOfAxiom from given LHS and RHS.
	 * 
	 * @param lhs_elem	XML element of the form <LHS>...</LHS>
	 * @param rhs_elem	XML element of the form <RHS>...</RHS>
	 */
	public OWLSubClassOfAxiom createSubClassOfAxiom(String lhs_elem, String rhs_elem) throws OWLParserException, StringIndexOutOfBoundsException {
		String lhs_elem_lower = lhs_elem.toLowerCase();
		String rhs_elem_lower = rhs_elem.toLowerCase();
		//System.out.println(this + " >> LHS|RHS: " + lhs_elem_lower + " | " + rhs_elem_lower);
		
		String lhs;
		String rhs;
		try {
			lhs = lhs_elem.substring(lhs_elem_lower.indexOf("<lhs>") + 5, lhs_elem_lower.indexOf("</lhs>"));
			rhs = rhs_elem.substring(rhs_elem_lower.indexOf("<rhs>") + 5, rhs_elem_lower.indexOf("</rhs>"));
		} catch (StringIndexOutOfBoundsException e) {
			throw e;
		}
		
		//System.out.println(this + " >> LHS: " + lhs);
		//System.out.println(this + " >> RHS: " + rhs);
		
		OWLClassExpression lhsClass;
		OWLClassExpression rhsClass;
		try {
			lhsClass = manchesterParser.parseClassExpression(lhs);
			rhsClass = manchesterParser.parseClassExpression(rhs);
		} catch (OWLParserException e) {
			throw e;
		}
		
		return dataFactory.getOWLSubClassOfAxiom(lhsClass, rhsClass);
	}
	
	/**
	 * Transforms an OWLClassExpression into NNF and adds precisification subscript to class names
	 * while rebasing them to output ontology.
	 *
	 * @param prc		integer denoting a specific precisification
	 * @param classExpr	OWLClassExpression
	 */
	public OWLClassExpression normalize(int prc, OWLClassExpression classExpr) {
		return addPrc(prc, classExpr.getNNF());
	}
	
	// PRIVATE METHODS //
	
	/**
	 * Recursive method extending original class names by precisification and rebasing them to output ontology.
	 */
	private OWLClassExpression addPrc(int prc, OWLClassExpression classExpr) {
		ClassExpressionType type = classExpr.getClassExpressionType();
		String classIRIString;
		if (type == ClassExpressionType.OWL_CLASS) {
			try { // should never fail since we check type before
				classIRIString = classExpr.asOWLClass().toStringID();
			} catch (Exception e) {
				System.out.println(this + " >> ERROR: " + classExpr + " is not an OWLClass.");
				return dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_" + prc));
			}
			String className = classIRIString.substring(classIRIString.indexOf("#"));
			return dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + className + "_" + prc));
		} else if (type == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
			return dataFactory.getOWLObjectComplementOf(addPrc(prc, Renderer.getInnerExpressionOfComplement(classExpr.nestedClassExpressions().toArray(OWLClassExpression[]::new))));
		} else if (type == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			Set<OWLClassExpression> newConjuncts = new HashSet<OWLClassExpression>();
			for (OWLClassExpression c : classExpr.asConjunctSet()) {
				newConjuncts.add(addPrc(prc, c));
			}
			return dataFactory.getOWLObjectIntersectionOf(newConjuncts);
		} else if (type == ClassExpressionType.OBJECT_UNION_OF) {
			Set<OWLClassExpression> newDisjuncts = new HashSet<OWLClassExpression>();
			for (OWLClassExpression c : classExpr.asDisjunctSet()) {
				newDisjuncts.add(addPrc(prc, c));
			}
			return dataFactory.getOWLObjectUnionOf(newDisjuncts);
		} else {
			System.out.println(this + " >> ERROR: unexpected ClassExpressionType " + type);
			return dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_" + prc));
		}
	}
	
	private OWLIndividual rebaseToOutput(OWLIndividual individual) throws IllegalArgumentException {
		if (individual.isAnonymous()) {
			throw new IllegalArgumentException("Anonymous individual cannot be rebased.");
		}
		String individualName = shortFormProvider.getShortForm(individual.asOWLNamedIndividual());
		if (individualName.equals("")) {
			throw new IllegalArgumentException("Anonymous individual cannot be rebased.");
		}
		return dataFactory.getOWLNamedIndividual(IRI.create(outputOntologyIRIString + "#" + individualName));
	}
}