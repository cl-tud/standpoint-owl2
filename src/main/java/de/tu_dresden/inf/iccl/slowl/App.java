package de.tu_dresden.inf.iccl.slowl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.model.parameters.Imports;

public class App {
	
	private static File input;
	private static String inputOntologyIRIString;
	private static File mostRecentOutput;
	
	private static boolean bTrans = true;
	private static boolean bDump = false;
	
	private static Set<File> tempFiles = new HashSet<File>();
	
    public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("----------< SP OWL >----------");
			System.err.println("[ERROR] No input ontology given.");
			System.err.println("[INFO] Use option -help for a list of available options.");
			System.out.println("------------------------------");
			return;
		}
		
		Options options = new Options();
		options.addOption(Option.builder("t").longOpt("translate").hasArg(false).desc("translates ontology; usable together with other options").build());
		options.addOption(Option.builder("i").longOpt("import").numberOfArgs(2).desc("imports an ontology and annotates all its annotatable axioms with a box operator of the specified standpoint name").build());
		options.addOption(Option.builder("q").longOpt("query").hasArg().desc("adds the negated query to the input ontology as a boolean combination").build());
		options.addOption(Option.builder("d").longOpt("dump").hasArg(false).desc("writes the output ontology to standard output instead of new file").build());
		
		if (args[0].equals("-h") || args[0].equals("-help")) {
			Collection<Option> optionList = options.getOptions();
			System.out.println("----------< SP OWL >----------");
			System.out.println("[HELP] Available options:");
			for (Option o : optionList) {
				System.out.printf("\t %-2s \t %-10s \t %s\n", "-" + o.getOpt(), "-" + o.getLongOpt(), o.getDescription());
			}
			System.out.println("------------------------------");
			return;
		}
		
		try {
			input = new File(args[0]);
		} catch (SecurityException e) {
			System.out.println("----------< SP OWL >----------");
			System.err.println("[ERROR] Could not read file. " + e.getMessage());
			System.out.println("------------------------------");
			return;
		}
		if (!input.canRead()) {
			System.out.println("----------< SP OWL >----------");
			System.err.println("[ERROR] File \"" + input.getPath() + "\" does not exist.");
			System.out.println("------------------------------");
			return;
		}
		
		mostRecentOutput = input;
		
		if (args.length == 1) {
			System.out.println("----------< SP OWL >----------");
			translateFile();
			System.out.println("------------------------------");
		} else if (args.length == 2) {
			if (args[1].equals("-d") || args[1].equals("-dump")) {
				bDump = true;
			}
			if (!bDump) {
				System.out.println("----------< SP OWL >----------");
			}
			translateFile();
			if (!bDump) {
				System.out.println("------------------------------");
			}
		} else if (args.length > 2) {
			CommandLineParser parser = new DefaultParser();
			
			CommandLine cmd;
			try {
				cmd = parser.parse(options, Arrays.copyOfRange(args, 1, args.length));
			} catch (ParseException e) {
				System.out.println("----------< SP OWL >----------");
				System.err.println("[ERROR] Could not parse command line arguments. " + e.getMessage());
				System.out.println("------------------------------");
				return;
			}
			
			if (cmd.getArgs().length != 0) {
				System.out.println("----------< SP OWL >----------");
				System.err.println("[ERROR] Unrecognized options or arguments given: " + Arrays.deepToString(cmd.getArgs()) + ".");
				System.out.println("------------------------------");
				return;
			}
			
			if (cmd.hasOption("d")) {
				bDump = true;
			}
			
			if (!bDump) {
				System.out.println("----------< SP OWL >----------");
			}
			
			if (cmd.hasOption("i")) {
				String[] values = cmd.getOptionValues("i");
				if (values.length != 2) {
					System.err.println("[ERROR] Option -i requires two arguments: ontology IRI and standpoint name.");
					return;
				}
				importOntology(values[0], values[1]);
			}
			
			if (cmd.hasOption("q")) {
				String value = cmd.getOptionValue("q");
				if (value == null) {
					System.err.println("[ERROR] Option -q requires one argument: either a query expression or a file path.");
					return;
				}
				addQuery(value);
			}
			
			if (cmd.hasOption("t") && bTrans) {
				translateFile();
			}
			
			if (bDump) {
				if (!cmd.hasOption("t")) {
					dumpMostRecentOutput();
				}
				removeTempFiles();
			}
			
			if (!bDump) {
				System.out.println("------------------------------");
			}
		}
    }
	
	
	// PRIVATE METHODS //

	private static void translateFile() {		
		Translator translator;
		try {
			translator = new Translator(mostRecentOutput, !bDump);
		} catch (OWLOntologyCreationException e) {
			System.err.println("[ERROR] Could not create ontology from file. " + e.getMessage());
			return;
		}
		
		Instant startTrans = Instant.now();
		translator.translateOntology();
		Instant endTrans = Instant.now();
		
		if (bDump) {
			translator.dumpOutputOntology();
		} else {
			translator.saveOutputOntology();
			
			long transTime = Duration.between(startTrans, endTrans).toMillis();
			
			System.out.println("------------------------------");
			System.out.println("Time (translation): " + transTime + " ms");
		}
	}
	
	private static void importOntology(String iriString, String s) {
		if (!Translator.isSPName(s)) {
			System.err.println("[ERROR] Illegal standpoint name: \"" + s + "\".");
			bTrans = false;
			return;
		}
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		
		OWLOntology ontology;
		try {
			ontology = man.loadOntologyFromOntologyDocument(mostRecentOutput);
		} catch (OWLOntologyCreationException e) {
			System.err.println("[ERROR] Could not load ontology from file. " + e.getMessage());
			bTrans = false;
			return;
		}
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final String outputOntologyIRIString = ontologyIRIString + "_import";
		final IRI outputOntologyIRI = IRI.create(outputOntologyIRIString);
		
		OWLOntology outputOntology;
		try {
			outputOntology = man.createOntology(outputOntologyIRI, Set.of(ontology));
		} catch (OWLOntologyCreationException e) {
			System.err.println("[ERROR] Could not create import output ontology. " + e.getMessage());
			bTrans = false;
			return;
		}
		
		File outputFile = new File(input.getParent().replace(File.separator, "/") + "/" + Translator.removeFileExtension(input.getName()) + "_import.owl");
		tempFiles.add(outputFile);
		
		OWLOntology importedOntology;
		if (!bDump) {
			System.out.println("[INFO] Importing ontology from \"" + iriString + "\".");
		}
		
		// test if argument is a file path
		boolean isPath = true;
		try {
			Paths.get(iriString);
		} catch (InvalidPathException | NullPointerException e) {
			isPath = false;
		}
		
		if (isPath) {
			try {
				importedOntology = man.loadOntologyFromOntologyDocument(new File(iriString));
			} catch (OWLOntologyCreationException e) {
				System.err.println("[ERROR] Could not load import ontology. " + e.getMessage());
				bTrans = false;
				return;
			}
		} else { // argument should be some ontology IRI
			try {
				importedOntology = man.loadOntology(IRI.create(iriString));
			} catch (OWLOntologyCreationException e) {
				System.err.println("[ERROR] Could not load import ontology. " + e.getMessage());
				bTrans = false;
				return;
			}
		}
		
		final String annotationString = "<standpointAxiom>\n" +
										"  <Box>\n" +
										"    <Standpoint name=\"" + s + "\"/>\n" +
										"  </Box>\n" +
										"</standpointAxiom>";

		final OWLAnnotationValue annotationValue = df.getOWLLiteral(annotationString);
		final OWLAnnotationProperty spLabel = df.getOWLAnnotationProperty(IRI.create(ontologyIRIString + "#standpointLabel"));
		final OWLAnnotation spAnnotation = df.getOWLAnnotation(spLabel, annotationValue);
		
		/* Test if imported ontology already contains standpointLabel annotations.
		 * These will not be considered during the translation, since (generally) the base of the imported ontology
		 * is different from the one we are importing it to.
		 */
		if (!bDump && importedOntology.annotationPropertiesInSignature(Imports.INCLUDED).map(a -> a.toStringID()).anyMatch(a -> a.endsWith("#standpointLabel"))) {
			System.out.println("[WARNING] Imported ontology already contains standpointLabel annotations. These will not be considered in the translation.");
		}
		
		OWLAxiom[] unannotatableAxioms = importedOntology.axioms().filter(a -> !Translator.supportedSPAxiomTypes.contains(a.getAxiomType())).toArray(OWLAxiom[]::new);
		OWLAxiom[] annotatableAxioms   = importedOntology.axioms().filter(a ->  Translator.supportedSPAxiomTypes.contains(a.getAxiomType())).toArray(OWLAxiom[]::new);
		
		Set<OWLAxiom> annotatedAxioms = new HashSet<OWLAxiom>();
		Set<OWLAnnotation> annotations;
		for (OWLAxiom a : annotatableAxioms) {
			annotations = a.getAnnotations();
			annotations.add(spAnnotation);
			annotatedAxioms.add(a.getAnnotatedAxiom(annotations));
		}
		
		ChangeApplied c = outputOntology.addAxioms(unannotatableAxioms);
		if (c != ChangeApplied.SUCCESSFULLY) {
			System.err.println("[ERROR] Could not add unannotatable axioms to import output ontology.");
			bTrans = false;
			return;
		}
		
		c = outputOntology.addAxioms(annotatedAxioms);
		if (c != ChangeApplied.SUCCESSFULLY) {
			System.err.println("[ERROR] Could not add standpoint axioms to import output ontology.");
			bTrans = false;
			return;
		}
		
		if (outputOntology.isEmpty() && !bDump) {
			System.out.println("[WARNING] Import output ontology is empty.");
		}
		
		try {
			man.saveOntology(outputOntology, new OWLXMLDocumentFormat(), new FileOutputStream(outputFile));
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Output file " + outputFile + " not found.");
			bTrans = false;
			return;
		} catch (OWLOntologyStorageException e) {
			System.err.println("[ERROR] Could not save import output ontology. " + e.getMessage());
			bTrans = false;
			return;
		}
		
		mostRecentOutput = outputFile;
		if (!bDump) {
			System.out.println("[INFO] Saved import output ontology as \"" + outputFile + "\".");
		}
	}
	
	private static void addQuery(String query) {		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		
		OWLOntology ontology;
		try {
			ontology = man.loadOntologyFromOntologyDocument(mostRecentOutput);
		} catch (OWLOntologyCreationException e) {
			System.err.println("[ERROR] Could not load ontology from file. " + e.getMessage());
			bTrans = false;
			return;
		}
		
		final String ontologyIRIString = SPParser.getOntologyIRIString(ontology);
		final IRI ontologyIRI = IRI.create(ontologyIRIString);		
		
		File outputFile = new File(input.getParent().replace(File.separator, "/") + "/" + Translator.removeFileExtension(input.getName()) + "_query.owl");
		tempFiles.add(outputFile);
		
		final String queryLower = query.toLowerCase();
		final OWLAnnotationProperty spLabel = df.getOWLAnnotationProperty(IRI.create(ontologyIRIString + "#standpointLabel"));
		
		boolean isPath = true;
		try {
			Paths.get(query);
		} catch (InvalidPathException | NullPointerException e) {
			isPath = false;
		}
		
		Pattern simpleQuery = Pattern.compile("\\[[a-zA-Z0-9\\s]+\\]\\(.*|\\<[a-zA-Z0-9\\s]+\\>\\(.*");
		
		String boolComb;
		if ((queryLower.startsWith("<box>") && queryLower.endsWith("</box>")) || (queryLower.startsWith("<diamond>") && queryLower.endsWith("</diamond>"))) {
			boolComb = "<booleanCombination>\n<NOT>\n" + query + "</NOT>\n</booleanCombination>";
		} else if (simpleQuery.matcher(query).matches()) {
			QueryParser qParser = new QueryParser(query);
			try {
				boolComb = "<booleanCombination>\n<NOT>\n" + qParser.parse() + "</NOT>\n</booleanCombination>";
			} catch (java.text.ParseException e) {
				System.err.println("[ERROR] Could not parse simple query expression. " + e.getMessage());
				bTrans = false;
				return;
			}
		} else if (isPath) {
			List<String> lines;
			try {
				lines = Files.readAllLines(Paths.get(query));
			} catch (IOException e) {
				System.err.println("[ERROR] Could not read query from file. " + e.getMessage());
				bTrans = false;
				return;
			}
			boolComb = "<booleanCombination>\n<NOT>\n";
			for (String s : lines) {
				boolComb = boolComb + s;
			}
			boolComb = boolComb + "</NOT>\n</booleanCombination>";
		} else {
			System.err.println("[ERROR] Illegal query expression. Use either an XML query, a simple query, or a file containing an XML query.");
			return;
		}
		
		final OWLAnnotationValue annotationValue = df.getOWLLiteral(boolComb);
		final OWLAnnotation spAnnotation = df.getOWLAnnotation(spLabel, annotationValue);
		
		ChangeApplied c = man.applyChange(new AddOntologyAnnotation(ontology, spAnnotation));
		if (c != ChangeApplied.SUCCESSFULLY) {
			System.err.println("[ERROR] Could not add negated query to output ontology.");
			bTrans = false;
			return;
		}
		
		if (ontology.isEmpty() && !bDump) {
			System.out.println("[WARNING] Query output ontology is empty.");
		}
		
		try {
			man.saveOntology(ontology, new OWLXMLDocumentFormat(), new FileOutputStream(outputFile));
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Output file " + outputFile + " not found.");
			bTrans = false;
			return;
		} catch (OWLOntologyStorageException e) {
			System.err.println("[ERROR] Could not save query output ontology. " + e.getMessage());
			bTrans = false;
			return;
		}
		
		mostRecentOutput = outputFile;
		if (!bDump) {
			System.out.println("[INFO] Saved query output ontology as \"" + outputFile + "\".");
		}
	}
	
	private static void dumpMostRecentOutput() {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		
		OWLOntology ontology;
		try {
			ontology = man.loadOntologyFromOntologyDocument(mostRecentOutput);
		} catch (OWLOntologyCreationException e) {
			System.err.println("[ERROR] Could not load ontology from file. " + e.getMessage());
			bTrans = false;
			return;
		}
		
		try {
			man.saveOntology(ontology, new OWLXMLDocumentFormat(), System.out);
		} catch (OWLOntologyStorageException e) {
			System.err.println("[ERROR] Could not dump most recent output ontology. " + e.getMessage());
			return;
		}
	}
	
	private static void removeTempFiles() {
		for (File f : tempFiles) {
			try {
				f.deleteOnExit();
			} catch (SecurityException e) {
				System.err.println("[ERROR] Could not mark file \"" + f + "\" for deletion." + e.getMessage());
				continue;
			}
		}
	}
}