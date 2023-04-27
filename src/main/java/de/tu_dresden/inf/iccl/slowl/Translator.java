package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLWriter;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;


public class Translator {
	// set of AxiomTypes whose annotation by standpointLabels is currently supported
	public static final Set<AxiomType> supportedSPAxiomTypes = Set.of(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES);
	
	// set of (normal) ABox AxiomTypes which will be considered in the translation
	public static final Set<AxiomType> supportedABoxAxiomTypes = Set.of(AxiomType.CLASS_ASSERTION, AxiomType.OBJECT_PROPERTY_ASSERTION, AxiomType.SAME_INDIVIDUAL);
	
	// set of (normal) unannotatble TBox AxiomTypes which will be considered in the translation
	public static final Set<AxiomType> supportedTBoxAxiomTypes = Set.of(AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION);
	
	// set of (normal) RBox AxiomTypes which will be considered in the translation
	public static final Set<AxiomType> supportedRBoxAxiomTypes = Set.of(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, AxiomType.DISJOINT_OBJECT_PROPERTIES, AxiomType.EQUIVALENT_OBJECT_PROPERTIES, AxiomType.FUNCTIONAL_OBJECT_PROPERTY, AxiomType.INVERSE_OBJECT_PROPERTIES, AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE, AxiomType.REFLEXIVE_OBJECT_PROPERTY, AxiomType.SUB_OBJECT_PROPERTY, AxiomType.SUB_PROPERTY_CHAIN_OF, AxiomType.TRANSITIVE_OBJECT_PROPERTY);
	
	// number of precisifications
	int iPrecisifications;
	
	// pattern for standpoint names
	private static Pattern sp = Pattern.compile("[a-zA-Z]+\\d*");
	
	// pattern for standpoint axiom names
	private static Pattern ax = Pattern.compile("§[a-zA-Z]+\\d*");
	
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
	private OWLAnnotationProperty standpointLabel;
	private IRI ontologyIRI;
	private IRI outputOntologyIRI;
	private SimpleShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	private OWLEntityChecker checker;
	private File outputFile;
	
	// universal role
	private OWLObjectProperty u = dataFactory.getOWLTopObjectProperty();
	
	// true if warnings and skip information should be printed
	private boolean bVerbose;
	
	private boolean bWarning = false;
	
	public Translator(OWLOntology ont, boolean verbose) throws IllegalArgumentException, OWLOntologyCreationException {
		bVerbose = verbose;
		
		if (ont == null) {
			throw new IllegalArgumentException("Cannot initialise Translator with null ontology.");
		}
		
		ontology = ont;
		
		parser = new SPParser(ontology);
		
		Predicate<OWLEntity> has_underscore = e -> (e.toStringID().indexOf("_", e.toStringID().lastIndexOf("#")) > 0);
		if (ontology.signature().anyMatch(has_underscore)) {
			System.out.println("[WARNING] Ontology contains entities with underscores. Make sure not to use reserved class or object property names.");
			bWarning = true;
		}
		
		ontologyIRIString = parser.getOntologyIRIString(ontology);
		ontologyIRI = IRI.create(ontologyIRIString);
		
		outputOntologyIRIString = ontologyIRIString + "_trans";
		outputOntologyIRI = IRI.create(outputOntologyIRIString);
		
		outputOntology = manager.createOntology(outputOntologyIRI); // throws OWLOntologyCreationException
		
		setOutputFile(new File(ontologyIRIString.substring(ontologyIRIString.lastIndexOf("#") + 1) + "_trans.owl"));
		
		standpointLabel = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRIString + "#standpointLabel"));
		
		manchesterParser.setDefaultOntology(ontology);
		checker = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(manager, Set.of(ontology), shortFormProvider));
		manchesterParser.setOWLEntityChecker(checker);
		
		parser.countDiamonds();
		iPrecisifications = Math.max(1, parser.diamondCount);
		spAxiomNames = parser.spAxiomNames;
		spNames = parser.spNames;
	}
	
	public Translator(File f, boolean verbose) throws IllegalArgumentException, OWLOntologyCreationException {
		bVerbose = verbose;
		
		ontology = manager.loadOntologyFromOntologyDocument(f); // throws OWLOntologyCreationException
			
		if (ontology == null) {
			throw new IllegalArgumentException("Cannot initialise translator with null ontology.");
		}
		
		parser = new SPParser(ontology);
		
		Predicate<OWLEntity> has_underscore = e -> (e.toStringID().indexOf("_", e.toStringID().lastIndexOf("#")) > 0);
		if (ontology.signature().anyMatch(has_underscore)) {
			System.out.println("[WARNING] Ontology contains entities with underscores. Make sure not to use reserved class or object property names.");
			bWarning = true;
		}
		
		ontologyIRIString = parser.getOntologyIRIString(ontology);
		ontologyIRI = IRI.create(ontologyIRIString);
		
		outputOntologyIRIString = ontologyIRIString + "_trans";
		outputOntologyIRI = IRI.create(outputOntologyIRIString);
		
		outputOntology = manager.createOntology(outputOntologyIRI); // throws OWLOntologyCreationException
		
		setOutputFile(new File(f.getParent().replace(File.separator, "/") + "/" + removeFileExtension(f.getName()) + "_trans.owl"));
		
		standpointLabel = dataFactory.getOWLAnnotationProperty(IRI.create(ontologyIRIString + "#standpointLabel"));
		
		manchesterParser.setDefaultOntology(ontology);
		checker = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(manager, Set.of(ontology), shortFormProvider));
		manchesterParser.setOWLEntityChecker(checker);
		
		parser.countDiamonds();
		iPrecisifications = Math.max(1, parser.diamondCount);
		parser.parseAxioms();
		spAxiomNames = parser.spAxiomNames;
		spNames = parser.spNames;
	}
	
	public Translator(OWLOntology ont) throws IllegalArgumentException, OWLOntologyCreationException {
		this(ont, true);
	}
	
	public Translator(File f) throws IllegalArgumentException, OWLOntologyCreationException {
		this(f, true);
	}
	
	public void setOutputFile(File f) {
		// set file extension to .owl
		String filename = f.getName();
		int k = filename.lastIndexOf(".");
		if (k > 0) {
			if (filename.indexOf(".owl", k) < 0) {
				filename = filename + ".owl";
			}
		} else {
			filename = filename + ".owl";
		}
		
		outputFile = new File(f.getParent().replace(File.separator, "/") + "/" + filename);
	}
	
	public void saveOutputOntology() {
		if (outputOntology == null) {
			System.err.println("[ERROR] Translated ontology is null.");
			return;
		} else if (outputOntology.isEmpty()) {
			System.out.println("[WARNING] Translated ontology is empty.");
		}
		
		try {
			manager.saveOntology(outputOntology, new OWLXMLDocumentFormat(), new FileOutputStream(outputFile));
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Output file " + outputFile + " not found.");
			return;
		} catch (OWLOntologyStorageException e) {
			System.err.println("[ERROR] Could not save translated ontology. " + e.getMessage());
			return;
		}
		
		System.out.println("[INFO] Saved translated ontology as \"" + outputFile + "\".");
	}
	
	public void dumpOutputOntology() {
		if (outputOntology == null) {
			System.err.println("[ERROR] Translated ontology is null.");
			return;
		} else if (outputOntology.isEmpty()) {
			System.out.println("[WARNING] Translated ontology is empty.");
		}
		
		try {
			manager.saveOntology(outputOntology, new OWLXMLDocumentFormat(), System.out);
		} catch (OWLOntologyStorageException e) {
			System.err.println("[ERROR] Could not save translated ontology. " + e.getMessage());
			return;
		}
	}
	
	/**
	 * Gathers the String representations of all standpointAxioms, booleanCombinations and sharpening statements of the input ontology.
	 * For booleanCombinations, named standpointAxioms are being replaced by their corresponding axioms.
	 */
	public Set<String> compileSPAxiomsToTranslate() {
		Set<String> results = new HashSet<String>();
		
		// add annotated SubClassOf and EquivalentClasses axioms
		results.addAll(parser.spAxioms);
		
		// add sharpening statements and booleanCombinations, where named standpointAxioms are being replaced by their corresponding axioms
		String[] generalSPStatements = parser.generalSPStatements;
		String newBoolComb;
		String boolCombLower;
		String toBeReplaced;
		String spAxiomName;
		String spAxiom;
		int i, j;
		
		outer:
		for (String s : generalSPStatements) {
			newBoolComb = s;
			
			// replace named standpoint axioms by their definitions (sharpening statements do not contain <standpointAxiom>)
			while (true) {
				boolCombLower = newBoolComb.toLowerCase();
				i = boolCombLower.indexOf("<standpointaxiom ");
				if (i < 0) { // nothing to replace
					break;
				}
				j = boolCombLower.indexOf("/>", i);
				if (j < 0) {
					System.err.println("[ERROR] Illegal boolean combination\n <[" + s + "]>.");
					continue outer;
				}
				try {
					toBeReplaced = newBoolComb.substring(i, j + 2);
				} catch (StringIndexOutOfBoundsException e) {
					System.err.println("[ERROR] Something went wrong. " + e.getMessage() + "\n<[" + newBoolComb + "]>.");
					continue outer;
				}
				spAxiomName = parser.getFirstSPAxiomName(toBeReplaced);
				if (spAxiomName == null) {
					System.err.println("[ERROR] Missing standpoint axiom name in\n <[" + s + "]>.");
					continue outer;
				}
				spAxiom = parser.spAxiomNameMap.get(spAxiomName);
				if (spAxiom == null) {
					System.err.println("[ERROR] Undefined standpoint axiom name in\n <[" + s + "]>.");
					continue outer;
				} else if (spAxiom.toLowerCase().contains("<standpointaxiom ")) {
					System.err.println("[ERROR] Illegal standpoint axiom\n [" + spAxiom + "].");
					continue outer;
				}
				newBoolComb = newBoolComb.replace(toBeReplaced, spAxiom.trim());
			}
			
			results.add(newBoolComb);
		}
		
		return results;
	}
	
	/**
	 * Adds SubClassOfAxioms of the form "OWLThing subsumed by trans(prc, phi)" and translated RBox axioms to the output ontology,
	 * where prc is a precisification and phi is an axiom to be translated.
	 */
	public void translateOntology() {
		if (!outputOntology.isEmpty() && bVerbose) {
			System.out.println("[WARNING] Output ontology already contains axioms before translation.");
		}
		
		Set<OWLAxiom> translatedAxioms = new HashSet<OWLAxiom>();
		int inputAxiomCount = ontology.getAxiomCount();
		int skipCount = 0;
		OWLClassExpression expr;
		
		// translate normal (non-standpoint) ABox axioms
		OWLAxiom[] aBoxAxioms = ontology.aboxAxioms(Imports.EXCLUDED).filter(a -> supportedABoxAxiomTypes.contains(a.getAxiomType())).toArray(OWLAxiom[]::new);
		
		forABox:
		for (OWLAxiom ax : aBoxAxioms) {
			for (int i = 0; i < iPrecisifications; i++) {
				try {
					expr = transPos(i, ax);
				} catch (IllegalArgumentException e) {
					if (bVerbose) {
						System.out.println("[SKIP] Could not translate ABox axiom. " + e.getMessage());
					}
					skipCount++;
					continue forABox;
				}
				translatedAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
			}
		}
		
		// translate normal (non-standpoint) annotatable TBox axioms
		OWLAxiom[] tBoxAxioms = ontology.tboxAxioms(Imports.EXCLUDED).filter(a -> supportedSPAxiomTypes.contains(a.getAxiomType())).filter(a -> !a.annotationPropertiesInSignature().anyMatch(b -> b.equals(standpointLabel))).toArray(OWLAxiom[]::new);
		
		AxiomType type;
		OWLEquivalentClassesAxiom equivalentClassesAxiom;
		Set<OWLClassExpression> exprs;
		Set<OWLClassExpression> exprsMinus;
		Set<OWLSubClassOfAxiom> subClassOfAxioms;
		Set<OWLAxiom> buffer; // to avoid partially translated axiom in output ontology
		
		forTBox:
		for (OWLAxiom ax : tBoxAxioms) {
			type = ax.getAxiomType();
			if (type == AxiomType.SUBCLASS_OF) {
				for (int i = 0; i < iPrecisifications; i++) {
					try {
						expr = transPos(i, ax);
					} catch (IllegalArgumentException e) {
						if (bVerbose) {
							System.out.println("[SKIP] Could not translate non-standpoint SubClassOf axiom. " + e.getMessage());
						}
						skipCount++;
						continue forTBox;
					}
					translatedAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
				}
			} else if (type == AxiomType.EQUIVALENT_CLASSES) {
				equivalentClassesAxiom = (OWLEquivalentClassesAxiom) ax;
				exprs = equivalentClassesAxiom.getClassExpressions();
				subClassOfAxioms = new HashSet<OWLSubClassOfAxiom>();
				for (OWLClassExpression e : exprs) {
					exprsMinus = new HashSet<OWLClassExpression>();
					exprsMinus.addAll(exprs);
					exprsMinus.remove(e);
					for (OWLClassExpression f : exprsMinus) {
						subClassOfAxioms.add(dataFactory.getOWLSubClassOfAxiom(e,f));
						subClassOfAxioms.add(dataFactory.getOWLSubClassOfAxiom(f,e));
					}	
				}
				buffer = new HashSet<OWLAxiom>();
				for (OWLSubClassOfAxiom a : subClassOfAxioms) {
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							expr = transPos(i, a);
						} catch (IllegalArgumentException e) {
							System.out.println("[SKIP] Could not translate non-standpoint EquivalentClasses axiom. " + e.getMessage());
							//System.out.println(this + " >> WARNING: Axiom could appear partially translated in output ontology.");
							skipCount++;
							continue forTBox;
						}
						buffer.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
					}
				}
				translatedAxioms.addAll(buffer);
			} else {
				if (bVerbose) {
					System.out.println("[SKIP] Unexpected AxiomType: " + type + ".");
				}
				skipCount++;
				continue;
			}
		}
		
		// translate normal (non-standpoint) non-annotatable TBox axioms
		tBoxAxioms = ontology.tboxAxioms(Imports.EXCLUDED).filter(a -> supportedTBoxAxiomTypes.contains(a.getAxiomType())).toArray(OWLAxiom[]::new);
		
		OWLDisjointClassesAxiom disjointClassesAxiom;
		OWLDisjointUnionAxiom disjointUnionAxiom;
		
		forNormTBox:
		for (OWLAxiom ax : tBoxAxioms) {
			type = ax.getAxiomType();
			if (type == AxiomType.DISJOINT_CLASSES) {
				disjointClassesAxiom = (OWLDisjointClassesAxiom) ax;
				exprs = disjointClassesAxiom.getClassExpressions();
				subClassOfAxioms = new HashSet<OWLSubClassOfAxiom>();
				for (OWLClassExpression e : exprs) {
					exprsMinus = new HashSet<OWLClassExpression>();
					exprsMinus.addAll(exprs);
					exprsMinus.remove(e);
					for (OWLClassExpression f : exprsMinus) {
						subClassOfAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLObjectIntersectionOf(e,f), dataFactory.getOWLNothing()));
					}
				}
				for (OWLSubClassOfAxiom a : subClassOfAxioms) {
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							expr = transPos(i, a);
						} catch (IllegalArgumentException e) {
							if (bVerbose) {
								System.out.println("[SKIP] Could not translate non-annotatable TBox axiom. " + e.getMessage());
							}
							skipCount++;
							continue forNormTBox;
						}
						translatedAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
					}
				}
			} else if (type == AxiomType.DISJOINT_UNION) {
				disjointUnionAxiom = (OWLDisjointUnionAxiom) ax;
				exprs = disjointUnionAxiom.getOWLEquivalentClassesAxiom().getClassExpressions();
				subClassOfAxioms = new HashSet<OWLSubClassOfAxiom>();
				for (OWLClassExpression e : exprs) {
					exprsMinus = new HashSet<OWLClassExpression>();
					exprsMinus.addAll(exprs);
					exprsMinus.remove(e);
					for (OWLClassExpression f : exprsMinus) {
						subClassOfAxioms.add(dataFactory.getOWLSubClassOfAxiom(e,f));
						subClassOfAxioms.add(dataFactory.getOWLSubClassOfAxiom(f,e));
					}	
				}
				
				buffer = new HashSet<OWLAxiom>();
				for (OWLSubClassOfAxiom a : subClassOfAxioms) {
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							expr = transPos(i, a);
						} catch (IllegalArgumentException e) {
							if (bVerbose) {
								System.out.println("[SKIP] Could not translate EquivalentClasses axiom of DisjointUnion axiom. " + e.getMessage());
							}
							//System.out.println(this + " >> WARNING: Axiom could appear partially translated in output ontology.");
							skipCount++;
							continue forNormTBox;
						}
						buffer.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
					}
				}
				translatedAxioms.addAll(buffer);
				
				disjointClassesAxiom = disjointUnionAxiom.getOWLDisjointClassesAxiom();
				exprs = disjointClassesAxiom.getClassExpressions();
				subClassOfAxioms = new HashSet<OWLSubClassOfAxiom>();
				for (OWLClassExpression e : exprs) {
					exprsMinus = new HashSet<OWLClassExpression>();
					exprsMinus.addAll(exprs);
					exprsMinus.remove(e);
					for (OWLClassExpression f : exprsMinus) {
						subClassOfAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLObjectIntersectionOf(e,f), dataFactory.getOWLNothing()));
					}
				}
				
				buffer = new HashSet<OWLAxiom>();
				for (OWLSubClassOfAxiom a : subClassOfAxioms) {
					for (int i = 0; i < iPrecisifications; i++) {
						try {
							expr = transPos(i, a);
						} catch (IllegalArgumentException e) {
							if (bVerbose) {
								System.out.println("[SKIP] Could not translate DisjointClasses axiom of DisjointUnion axiom. " + e.getMessage());
							}
							//System.out.println(this + " >> WARNING: Axiom could appear partially translated in output ontology.");
							skipCount++;
							continue forNormTBox;
						}
						buffer.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
					}
				}
				translatedAxioms.addAll(buffer);
			}
		}
		
		// translate normal (non-standpoint) RBox axioms
		OWLAxiom[] rBoxAxioms = ontology.axioms(Imports.EXCLUDED).filter(a -> supportedRBoxAxiomTypes.contains(a.getAxiomType())).toArray(OWLAxiom[]::new);
		OWLAxiom rBoxAxiom;
		
		forRBox:
		for (OWLAxiom ax : rBoxAxioms) {
			for (int i = 0; i < iPrecisifications; i++) {
				try {
					rBoxAxiom = transNormalRBoxAxiom(i, ax);
				} catch (IllegalArgumentException e) {
					if (bVerbose) {
						System.out.println("[SKIP] Could not translate RBox axiom. " + e.getMessage());
					}
					skipCount++;
					continue forRBox;
				}
				translatedAxioms.add(rBoxAxiom);
				//System.out.println(this + " >> Translated RBox axiom: " + rBoxAxiom.getAxiomType() + " " + rBoxAxiom);
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
					if (bVerbose) {
						System.out.println("[SKIP] Could not translate standpoint axiom. " + e.getMessage());
					}
					skipCount++;
					continue forSP;
				}
				translatedAxioms.add(dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
			}
		}
		
		// add translated axioms to outputOntology
		int axiomCount;
		ChangeApplied change = manager.addAxioms(outputOntology, translatedAxioms);
		if (change != ChangeApplied.SUCCESSFULLY) {
			axiomCount = outputOntology.getAxiomCount(Imports.EXCLUDED);
			System.err.println("[ERROR] Could not add " + (translatedAxioms.size() - axiomCount) + " translated axioms to output ontology.");
		}
		
		// add universal axioms to outputOntology
		for (int i = 0; i < iPrecisifications; i++) {
			expr = dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#M_*_" + i)));
			change = manager.addAxiom(outputOntology, dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), expr));
			if (change != ChangeApplied.SUCCESSFULLY) {
				System.err.println(this + " >> ERROR: Could not add universal axiom [" + i + "] to output ontology.");
			}
		}
		
		axiomCount = outputOntology.getAxiomCount(Imports.EXCLUDED);
		if (bVerbose) {
			System.out.println("[INFO] Input ontology contains " + inputAxiomCount + " axiom(s). Added " + axiomCount + " axiom(s) to translated ontology. Skipped " + skipCount + " axiom(s) in translation.");
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
			OWLObjectPropertyAssertionAxiom roleAssertionAxiom = (OWLObjectPropertyAssertionAxiom) axiom;
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
			throw new IllegalArgumentException("Unexpected AxiomType: " + type + ".");
		}
	}
	
	/**
	 * Translates (negated) OWLAxiom to OWLClassExpression.
	 *
	 * @param prc	integer denoting a specific precisification
	 * @param axiom	OWLAxiom
	 */
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
			throw new IllegalArgumentException("Unexpected AxiomType: " + type + ".");
		}		
	}
	
	/**
	 * Translates boolean combination of standpoint axioms or a sharpening statment to OWLClassExpression.
	 *
	 * @param prc	integer denoting a specific precisification
	 * @param s		normalized boolean combination of standpoint axioms or sharpening statement as XML String
	 */
	public OWLClassExpression trans(int prc, String s) throws IllegalArgumentException {
		OWLClassExpression result = null;
		
		String[] elements = parser.getRootAndChildElements(s);
		if (elements[0] == null) {
			throw new IllegalArgumentException("Illegal boolean combination.");
		}
		
		if (elements.length >= 2 && elements[0].equalsIgnoreCase("booleanCombination")) {
			return trans(prc, elements[1]);
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
				throw new IllegalArgumentException("Illegal boolean combination; probably not normalized (<NOT> should only occur before <SubClassOf>, <EquivalentClasses>, <Box> or <Diamond>).");
			}
			
			if (subElements[0].equalsIgnoreCase("SubClassOf")) { // NOT SubClassOf
				try {
					subClassOfAxiom = createSubClassOfAxiom(subElements[1], subElements[2]);
				} catch (OWLParserException e) {
					throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
				}
				
				result = transNeg(prc, subClassOfAxiom); // throws IllegalArgumentException
			
			} else if (subElements[0].equalsIgnoreCase("EquivalentClasses")) { // NOT EquivalentClasses, i.e. (NOT SubClassOf) OR (NOT SubClassOf)
				try {
					subClassOfAxiom = createSubClassOfAxiom(subElements[1], subElements[2]);
					subClassOfAxiom2 = createSubClassOfAxiom(subElements[2].replace("RHS","LHS").replace("rhs","lhs"), subElements[1].replace("LHS","RHS").replace("lhs","rhs"));
				} catch (OWLParserException e) {
					e.printStackTrace();
					return null;
				}
				
				result = dataFactory.getOWLObjectUnionOf(transNeg(prc, subClassOfAxiom), transNeg(prc, subClassOfAxiom2)); // throws IllegalArgumentException
				
			} else if (subElements[0].equalsIgnoreCase("Box")) {
				subSubElements = parser.getRootAndChildElements(subElements[2]);
				if (subSubElements.length != 3) {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Box> does not have exactly 2 child elements.");
				}
				
				if (subSubElements[0].equalsIgnoreCase("SubClassOf")) { // NOT Box SubClassOf, i.e. Diamond (NOT SubClassOf)
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
					} catch (OWLParserException e) {
						throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
					}
				
					disjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(transExpr(i, subElements[1]), transNeg(i, subClassOfAxiom))); // throws IllegalArgumentException
					}
				
					result = dataFactory.getOWLObjectUnionOf(disjunctSet);
					
				} else if (subSubElements[0].equalsIgnoreCase("EquivalentClasses")) { // NOT Box EquivalentClasses, i.e. Diamond ((NOT SubClassOf) OR (NOT SubClassOf))
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
						subClassOfAxiom2 = createSubClassOfAxiom(subSubElements[2].replace("RHS","LHS").replace("rhs","lhs"), subSubElements[1].replace("LHS","RHS").replace("lhs","rhs"));
					} catch (OWLParserException e) {
						throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
					}
					
					disjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(transExpr(i, subElements[1]), dataFactory.getOWLObjectUnionOf(transNeg(prc, subClassOfAxiom), transNeg(prc, subClassOfAxiom2)))); // throws IllegalArgumentException
					}
				
					result = dataFactory.getOWLObjectUnionOf(disjunctSet);
					
				} else {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Box> was not <SubClassOf> or <EquivalentClasses>.");
				}
				
			} else if (subElements[0].equalsIgnoreCase("Diamond")) {
				subSubElements = parser.getRootAndChildElements(subElements[2]);
				if (subSubElements.length != 3) {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Diamond> does not have exactly 2 child elements.");
				}
				
				if (subSubElements[0].equalsIgnoreCase("SubClassOf")) { // NOT Diamond SubClassOf, i.e. Box (NOT SubClassOf)
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
					} catch (OWLParserException e) {
						throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
					}
				
					conjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, subElements[1])).getNNF(), transNeg(i, subClassOfAxiom))); // throws IllegalArgumentException
					}
				
					result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
				
				} else if (subSubElements[0].equalsIgnoreCase("EquivalentClasses")) { // NOT Diamond EquivalentClasses, i.e. Box ((NOT SubClassOf) OR (NOT SubClassOf))
					try {
						subClassOfAxiom = createSubClassOfAxiom(subSubElements[1], subSubElements[2]);
						subClassOfAxiom2 = createSubClassOfAxiom(subSubElements[2].replace("RHS","LHS").replace("rhs","lhs"), subSubElements[1].replace("LHS","RHS").replace("lhs","rhs"));
					} catch (OWLParserException e) {
						throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
					}
					
					conjunctSet = new HashSet<OWLClassExpression>();
					for (int i = 0; i < iPrecisifications; i++) {
						conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, subElements[1])).getNNF(), transNeg(i, subClassOfAxiom), transNeg(i, subClassOfAxiom2))); // throws IllegalArgumentException
					}
				
					result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
					
				} else {
					throw new IllegalArgumentException("Illegal boolean combination; axiom element of <Diamond> was not <SubClassOf> or <EquivalentClasses>.");
				}
				
			} else {
				throw new IllegalArgumentException("Illegal boolean combination; <NOT> not followed by <SubClassOf>, <Box>, or <Diamond>.");
			}
			
		} else if (elements[0].equalsIgnoreCase("AND")) {
			if (elements.length < 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <AND> has less than 2 child elements.");
			}
			
			conjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 1; i < elements.length; i++) {
				conjunctSet.add(trans(prc, elements[i]));
			}
			
			result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
			
		} else if (elements[0].equalsIgnoreCase("OR")) {
			if (elements.length < 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <OR> has less than 2 child elements.");
			}
			
			disjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 1; i < elements.length; i++) {
				disjunctSet.add(trans(prc, elements[i]));
			}
			
			result = dataFactory.getOWLObjectUnionOf(disjunctSet);
			
		} else if (elements[0].equalsIgnoreCase("Box")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <Box> does not have exactly 2 child elements.");
			}
			
			conjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 0; i < iPrecisifications; i++) {
				conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, elements[1])).getNNF(), trans(i, elements[2]))); // throws IllegalArgumentException
			}
			
			result = dataFactory.getOWLObjectIntersectionOf(conjunctSet);
			
		} else if (elements[0].equalsIgnoreCase("Diamond")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <Diamond> does not have exactly 2 child elements.");
			}

			disjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 0; i < iPrecisifications; i++) {
				expr = trans(i, elements[2]); // throws IllegalArgumentException
				if (expr.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) { // flatten the intersections
					conjunctSet = expr.asConjunctSet();
					conjunctSet.add(transExpr(i, elements[1])); // throws IllegalArgumentException
					disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(conjunctSet));
				} else {
					disjunctSet.add(dataFactory.getOWLObjectIntersectionOf(transExpr(i, elements[1]), expr)); // throws IllegalArgumentException
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
				throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
			}
			
			result = dataFactory.getOWLObjectIntersectionOf(transPos(prc, subClassOfAxiom), transPos(prc, subClassOfAxiom2)); // throws IllegalArgumentException
			
		} else if (elements[0].equalsIgnoreCase("SubClassOf")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal boolean combination; <SubClassOf> does not have exactly 2 child elements.");
			}
			
			try {
				subClassOfAxiom = createSubClassOfAxiom(elements[1], elements[2]);
			} catch (OWLParserException e) {
				throw new IllegalArgumentException("Illegal boolean combination. " + e.getMessage());
			}
			
			result = transPos(prc, subClassOfAxiom); // throws IllegalArgumentException
		} else if (elements[0].equalsIgnoreCase("sharpening")) {
			if (elements.length != 3) {
				throw new IllegalArgumentException("Illegal sharpening statement; <sharpening> does not have exactly 2 child elements.");
			}
			
			conjunctSet = new HashSet<OWLClassExpression>();
			for (int i = 0; i < iPrecisifications; i++) {
				//conjunctSet.add(dataFactory.getOWLObjectComplementOf(transExpr(i, "<MINUS>" + elements[1] + elements[2] + "</MINUS>")).getNNF());
				conjunctSet.add(dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectComplementOf(transExpr(i, elements[1])).getNNF(), transExpr(i, elements[2]))); // throws IllegalArgumentException
			}
			
			if (conjunctSet.size() > 1) {
				result = dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectIntersectionOf(conjunctSet), dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLNothing()));
			} else { // get rid of one-conjunct intersections, and flatten union
				disjunctSet = conjunctSet.toArray(new OWLClassExpression[0])[0].asDisjunctSet();
				disjunctSet.add(dataFactory.getOWLObjectAllValuesFrom(u, dataFactory.getOWLNothing()));
				result = dataFactory.getOWLObjectUnionOf(disjunctSet);
			}
		} else if (Set.of("standpoint", "union", "intersection", "minus").contains(elements[0].toLowerCase())) {
			throw new IllegalArgumentException("Illegal boolean combination; missing modal operator before <" + elements[0] + ">.");
		} else {
			throw new IllegalArgumentException("Illegal standpoint statement; unknown operator: " + elements[0] + ".");
		}
		
		return result;
	}
	
	public OWLAxiom transNormalRBoxAxiom(int prc, OWLAxiom axiom) throws IllegalArgumentException {
		final AxiomType type = axiom.getAxiomType();
		if (type == AxiomType.ASYMMETRIC_OBJECT_PROPERTY) {
			OWLAsymmetricObjectPropertyAxiom asymmetricObjectPropertyAxiom = (OWLAsymmetricObjectPropertyAxiom) axiom;
			return dataFactory.getOWLAsymmetricObjectPropertyAxiom(rebaseToOutput(prc, asymmetricObjectPropertyAxiom.getProperty()));
		} else if (type == AxiomType.DISJOINT_OBJECT_PROPERTIES) {
			OWLDisjointObjectPropertiesAxiom disjointObjectPropertiesAxiom = (OWLDisjointObjectPropertiesAxiom) axiom;
			return dataFactory.getOWLDisjointObjectPropertiesAxiom(disjointObjectPropertiesAxiom.properties().map(r -> rebaseToOutput(prc, r)).toArray(OWLObjectPropertyExpression[]::new));
		} else if (type == AxiomType.EQUIVALENT_OBJECT_PROPERTIES) {
			OWLEquivalentObjectPropertiesAxiom equivalentObjectPropertiesAxiom = (OWLEquivalentObjectPropertiesAxiom) axiom;
			return dataFactory.getOWLEquivalentObjectPropertiesAxiom(equivalentObjectPropertiesAxiom.properties().map(r -> rebaseToOutput(prc, r)).toArray(OWLObjectPropertyExpression[]::new));
		} else if (type == AxiomType.FUNCTIONAL_OBJECT_PROPERTY) {
			OWLFunctionalObjectPropertyAxiom functionalObjectPropertyAxiom = (OWLFunctionalObjectPropertyAxiom) axiom;
			return dataFactory.getOWLFunctionalObjectPropertyAxiom(rebaseToOutput(prc, functionalObjectPropertyAxiom.getProperty()));
		} else if (type == AxiomType.INVERSE_OBJECT_PROPERTIES) {
			OWLInverseObjectPropertiesAxiom inverseObjectPropertiesAxiom = (OWLInverseObjectPropertiesAxiom) axiom;
			return dataFactory.getOWLInverseObjectPropertiesAxiom(rebaseToOutput(prc, inverseObjectPropertiesAxiom.getFirstProperty()), rebaseToOutput(prc, inverseObjectPropertiesAxiom.getSecondProperty()));
		} else if (type == AxiomType.IRREFLEXIVE_OBJECT_PROPERTY) {
			OWLIrreflexiveObjectPropertyAxiom irreflexiveObjectPropertyAxiom = (OWLIrreflexiveObjectPropertyAxiom) axiom;
			return dataFactory.getOWLIrreflexiveObjectPropertyAxiom(rebaseToOutput(prc, irreflexiveObjectPropertyAxiom.getProperty()));
		} else if (type == AxiomType.OBJECT_PROPERTY_DOMAIN) {
			OWLObjectPropertyDomainAxiom objectPropertyDomainAxiom = (OWLObjectPropertyDomainAxiom) axiom;
			return dataFactory.getOWLObjectPropertyDomainAxiom(rebaseToOutput(prc, objectPropertyDomainAxiom.getProperty()), rebaseToOutput(prc, objectPropertyDomainAxiom.getDomain()));
		} else if (type == AxiomType.OBJECT_PROPERTY_RANGE) {
			OWLObjectPropertyRangeAxiom objectPropertyRangeAxiom = (OWLObjectPropertyRangeAxiom) axiom;
			return dataFactory.getOWLObjectPropertyRangeAxiom(rebaseToOutput(prc, objectPropertyRangeAxiom.getProperty()), rebaseToOutput(prc, objectPropertyRangeAxiom.getRange()));
		} else if (type == AxiomType.REFLEXIVE_OBJECT_PROPERTY) {
			OWLReflexiveObjectPropertyAxiom reflexiveObjectPropertyAxiom = (OWLReflexiveObjectPropertyAxiom) axiom;
			return dataFactory.getOWLReflexiveObjectPropertyAxiom(rebaseToOutput(prc, reflexiveObjectPropertyAxiom.getProperty()));
		} else if (type == AxiomType.SUB_OBJECT_PROPERTY) {
			OWLSubObjectPropertyOfAxiom subObjectPropertyOfAxiom = (OWLSubObjectPropertyOfAxiom) axiom;
			return dataFactory.getOWLSubObjectPropertyOfAxiom(rebaseToOutput(prc, subObjectPropertyOfAxiom.getSubProperty()), rebaseToOutput(prc, subObjectPropertyOfAxiom.getSuperProperty()));
		} else if (type == AxiomType.SUB_PROPERTY_CHAIN_OF) {
			OWLSubPropertyChainOfAxiom subPropertyChainOfAxiom = (OWLSubPropertyChainOfAxiom) axiom;
			return dataFactory.getOWLSubPropertyChainOfAxiom(Arrays.asList(subPropertyChainOfAxiom.getPropertyChain().stream().map(r -> rebaseToOutput(prc, r)).toArray(OWLObjectPropertyExpression[]::new)), rebaseToOutput(prc, subPropertyChainOfAxiom.getSuperProperty()));
		} else if (type == AxiomType.TRANSITIVE_OBJECT_PROPERTY) {
			OWLTransitiveObjectPropertyAxiom transitiveObjectPropertyAxiom = (OWLTransitiveObjectPropertyAxiom) axiom;
			return dataFactory.getOWLTransitiveObjectPropertyAxiom(rebaseToOutput(prc, transitiveObjectPropertyAxiom.getProperty()));
		} else {
			throw new IllegalArgumentException("Unsupported RBox AxiomType: " + type + ".");
		}
	}
	
	/**
	 * Creates an OWLSubClassOfAxiom from given LHS and RHS.
	 * 
	 * @param lhs_elem	XML element of the form <LHS>...</LHS>
	 * @param rhs_elem	XML element of the form <RHS>...</RHS>
	 */
	public OWLSubClassOfAxiom createSubClassOfAxiom(String lhs_elem, String rhs_elem) throws OWLParserException {
		lhs_elem = Renderer.replaceEscapeChars(lhs_elem);
		rhs_elem = Renderer.replaceEscapeChars(rhs_elem);
		String lhs_elem_lower = lhs_elem.toLowerCase();
		String rhs_elem_lower = rhs_elem.toLowerCase();
		//System.out.println(this + " >> LHS|RHS: " + lhs_elem_lower + " | " + rhs_elem_lower);
		
		String lhs;
		String rhs;
		lhs = lhs_elem.substring(lhs_elem_lower.indexOf("<lhs>") + 5, lhs_elem_lower.indexOf("</lhs>"));
		rhs = rhs_elem.substring(rhs_elem_lower.indexOf("<rhs>") + 5, rhs_elem_lower.indexOf("</rhs>"));
		
		//System.out.println(this + " >> LHS: " + lhs);
		//System.out.println(this + " >> RHS: " + rhs);
		
		OWLClassExpression lhsClass;
		try {
			lhsClass = manchesterParser.parseClassExpression(lhs); // throws OWLParserException
		} catch (OWLParserException e) {
			if (lhs.equals("owl:Thing")) {
				lhsClass = dataFactory.getOWLThing();
			} else if (lhs.equals("owl:Nothing")) {
				lhsClass = dataFactory.getOWLNothing();
			} else {
				throw e;
			}
		}
		
		OWLClassExpression rhsClass;
		try {
			rhsClass = manchesterParser.parseClassExpression(rhs); // throws OWLParserException
		} catch (OWLParserException e) {
			if (rhs.equals("owl:Thing")) {
				rhsClass = dataFactory.getOWLThing();
			} else if (rhs.equals("owl:Nothing")) {
				rhsClass = dataFactory.getOWLNothing();
			} else {
				throw e;
			}
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
	public OWLClassExpression normalize(int prc, OWLClassExpression classExpr) throws IllegalArgumentException {
		return rebaseToOutput(prc, classExpr.getNNF()); // throws IllegalArgumentException
	}
	
	
	// PUBLIC STATIC METHODS //
	
	public static boolean isSPName(String s) {
		if (s.equals("*")) {
			return true;
		} else {
			Matcher m = sp.matcher(s);
			return m.matches();
		}
	}
	
	public static boolean isSPAxiomName(String s) {
		Matcher m = ax.matcher(s);
		return m.matches();
	}
	
	public static String removeFileExtension(String filename) {
		int i = filename.lastIndexOf(".");
		if (i > 0) {
			return filename.substring(0, i);
		} else {
			return filename;
		}
	}
	
	
	// PRIVATE METHODS //
	
	/**
	 * Recursive method extending original class names by precisification and rebasing them to output ontology.
	 */
	private OWLClassExpression rebaseToOutput(int prc, OWLClassExpression classExpr) throws IllegalArgumentException {
		if (classExpr.compareTo(dataFactory.getOWLThing()) == 0 || classExpr.compareTo(dataFactory.getOWLNothing()) == 0) {
			return classExpr;
		} 
		ClassExpressionType type = classExpr.getClassExpressionType();
		if (type == ClassExpressionType.OWL_CLASS) {
			String className = shortFormProvider.getShortForm(classExpr.asOWLClass());
			return dataFactory.getOWLClass(IRI.create(outputOntologyIRIString + "#" + className + "_" + prc));
		} else if (type == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
			return dataFactory.getOWLObjectComplementOf(rebaseToOutput(prc, Renderer.getInnerExpressionOfComplement(classExpr.nestedClassExpressions().toArray(OWLClassExpression[]::new))));
		} else if (type == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			Set<OWLClassExpression> newConjuncts = new HashSet<OWLClassExpression>();
			for (OWLClassExpression c : classExpr.asConjunctSet()) {
				newConjuncts.add(rebaseToOutput(prc, c));
			}
			return dataFactory.getOWLObjectIntersectionOf(newConjuncts);
		} else if (type == ClassExpressionType.OBJECT_UNION_OF) {
			Set<OWLClassExpression> newDisjuncts = new HashSet<OWLClassExpression>();
			for (OWLClassExpression c : classExpr.asDisjunctSet()) {
				newDisjuncts.add(rebaseToOutput(prc, c));
			}
			return dataFactory.getOWLObjectUnionOf(newDisjuncts);
		} else if (type == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
			OWLObjectAllValuesFrom objectAllValuesFrom = (OWLObjectAllValuesFrom) classExpr;
			return dataFactory.getOWLObjectAllValuesFrom(rebaseToOutput(prc, objectAllValuesFrom.getProperty()), rebaseToOutput(prc, objectAllValuesFrom.getFiller()));
		} else if (type == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) classExpr;
			return dataFactory.getOWLObjectSomeValuesFrom(rebaseToOutput(prc, objectSomeValuesFrom.getProperty()), rebaseToOutput(prc, objectSomeValuesFrom.getFiller()));
		} else if (type == ClassExpressionType.OBJECT_MIN_CARDINALITY) {
			OWLObjectMinCardinality objectMinCardinality = (OWLObjectMinCardinality) classExpr;
			return dataFactory.getOWLObjectMinCardinality(objectMinCardinality.getCardinality(), rebaseToOutput(prc, objectMinCardinality.getProperty()), rebaseToOutput(prc, objectMinCardinality.getFiller()));
		} else if (type == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
			OWLObjectMaxCardinality objectMaxCardinality = (OWLObjectMaxCardinality) classExpr;
			return dataFactory.getOWLObjectMaxCardinality(objectMaxCardinality.getCardinality(), rebaseToOutput(prc, objectMaxCardinality.getProperty()), rebaseToOutput(prc, objectMaxCardinality.getFiller()));
		} else if (type == ClassExpressionType.OBJECT_EXACT_CARDINALITY) {
			OWLObjectExactCardinality objectExactCardinality = (OWLObjectExactCardinality) classExpr;
			return dataFactory.getOWLObjectExactCardinality(objectExactCardinality.getCardinality(), rebaseToOutput(prc, objectExactCardinality.getProperty()), rebaseToOutput(prc, objectExactCardinality.getFiller()));
		} else if (type == ClassExpressionType.OBJECT_HAS_SELF) {
			OWLObjectHasSelf objectHasSelf = (OWLObjectHasSelf) classExpr;
			return dataFactory.getOWLObjectHasSelf(rebaseToOutput(prc, objectHasSelf.getProperty()));
		} else if (type == ClassExpressionType.OBJECT_HAS_VALUE) {
			OWLObjectHasValue objectHasValue = (OWLObjectHasValue) classExpr;
			return dataFactory.getOWLObjectHasValue(rebaseToOutput(prc, objectHasValue.getProperty()), rebaseToOutput(objectHasValue.getFiller()));
		} else if (type == ClassExpressionType.OBJECT_ONE_OF) {
			OWLObjectOneOf objectOneOf = (OWLObjectOneOf) classExpr;
			return dataFactory.getOWLObjectOneOf(objectOneOf.individuals().map(i -> rebaseToOutput(i)));
		} else if (type == ClassExpressionType.DATA_ALL_VALUES_FROM) {
			OWLDataAllValuesFrom dataAllValuesFrom = (OWLDataAllValuesFrom) classExpr;
			return dataFactory.getOWLDataAllValuesFrom(rebaseToOutput(dataAllValuesFrom.getProperty()), dataAllValuesFrom.getFiller());
		} else if (type == ClassExpressionType.DATA_SOME_VALUES_FROM) {
			OWLDataSomeValuesFrom dataSomeValuesFrom = (OWLDataSomeValuesFrom) classExpr;
			return dataFactory.getOWLDataSomeValuesFrom(rebaseToOutput(dataSomeValuesFrom.getProperty()), dataSomeValuesFrom.getFiller());
		} else if (type == ClassExpressionType.DATA_EXACT_CARDINALITY) {
			OWLDataExactCardinality dataExactCardinality = (OWLDataExactCardinality) classExpr;
			return dataFactory.getOWLDataExactCardinality(dataExactCardinality.getCardinality(), rebaseToOutput(dataExactCardinality.getProperty()), dataExactCardinality.getFiller());
		} else if (type == ClassExpressionType.DATA_HAS_VALUE) {
			OWLDataHasValue dataHasValue = (OWLDataHasValue) classExpr;
			return dataFactory.getOWLDataHasValue(rebaseToOutput(dataHasValue.getProperty()), dataHasValue.getFiller());
		} else if (type == ClassExpressionType.DATA_MAX_CARDINALITY) {
			OWLDataMaxCardinality dataMaxCardinality = (OWLDataMaxCardinality) classExpr;
			return dataFactory.getOWLDataMaxCardinality(dataMaxCardinality.getCardinality(), rebaseToOutput(dataMaxCardinality.getProperty()), dataMaxCardinality.getFiller());
		} else if (type == ClassExpressionType.DATA_MIN_CARDINALITY) {
			OWLDataMinCardinality dataMinCardinality = (OWLDataMinCardinality) classExpr;
			return dataFactory.getOWLDataMinCardinality(dataMinCardinality.getCardinality(), rebaseToOutput(dataMinCardinality.getProperty()), dataMinCardinality.getFiller());
		} else {
			throw new IllegalArgumentException("Unexpected ClassExpressionType: " + type + ".");
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
	
	private OWLObjectPropertyExpression rebaseToOutput(int prc, OWLObjectPropertyExpression expr) {
		OWLObjectProperty role = expr.getNamedProperty();
		String roleName = shortFormProvider.getShortForm(role);
		OWLObjectPropertyExpression rebasedRole = dataFactory.getOWLObjectProperty(IRI.create(outputOntologyIRIString + "#" + roleName + "_" + prc));
		if (expr.getSimplified().compareTo(role.getInverseProperty()) != 0) { // role name
			return rebasedRole;
		} else { // inverse role
			return rebasedRole.getInverseProperty();
		}
	}
	
	private OWLDataPropertyExpression rebaseToOutput(OWLDataPropertyExpression expr) {
		String dataPropertyName = shortFormProvider.getShortForm(expr.asOWLDataProperty());
		return dataFactory.getOWLDataProperty(IRI.create(outputOntologyIRIString + "#" + dataPropertyName));
	}
	
}