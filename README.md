# Standpoint-SROIQ Translator

## Description
Command-line tool allowing translation of OWL2 ontologies with standpoint annotations to standard OWL2.
It can parse special annotation properties simulating the syntax of standpoint logic, and translates them to standard OWL2 syntax.
A separate SROIQ reasoner (e.g. HermiT) can then be used on the translated ontology.

## Installation
To build and install the app, JDK 9 or above (https://www.oracle.com/java/technologies/downloads/) and Apache Maven (https://maven.apache.org/index.html) are required.

Open the command prompt in the standpoint-reasoning directory and execute `mvn clean install`.
If the build was successful, you should be able to see `slowl-0.1.0-SNAPSHOT.jar` in the folder `standpoint-reasoning/target`.

## Usage
To translate an ontology file, execute
	```
	java -jar ./target/slowl-0.1.0-SNAPSHOT.jar <filepath>
	```
The output ontology will be saved in the same directory as the input file.

You can use the command line option `-i` to import an ontology to the input file, and annotate all axioms for which standpoint operators are supported by a box operator with a specified standpoint name, e.g.
	```
	java -jar ./target/slowl-0.1.0-SNAPSHOT.jar ../Example.owl -i "https://protege.stanford.edu/ontologies/pizza/pizza.owl" pizza
	```
	
Use the command line option `-q` together with a query expression (XML, simple query, or file with XML), to add the negated query to the input ontology.
The query can then be checked by translating the resulting ontology and checking it for inconsistency; if the translated ontology is inconsistent, the query is true.
The syntax for a simple query is the following:
	```
	"[sp_name](class_expression sub/eq class_expression)"
	```
or
	```
	"<sp_name>(class_expression sub/eq class_expression)"
	```
for a box or diamond operator, and a subclass or equivalent classes axiom, respectively.

To translate an ontology right after adding an import or a query, use the flag `-t`.

The flag `-d` causes the output to be dumped to the command line, rather than being saved to a file. It can be used together with the other options.
