# Standpoint-Reasoning Plugin for Protégé

**NOTE: The plugin has been discontinued for now. Please use the command-line tool from the main branch.**

## Description
A reasoner plugin for Protégé 5.0 implementing standpoint reasoning.
The goal is to write a reasoner plugin which acts as a wrapper for a usual DL reasoner (e.g. HermiT). It should parse special annotation properties simulating the syntax of standpoint logic, translate them to standard SROIQ,
call the existing reasoner and return the results (similar to the FuzzyOWL2 plugin).

## Installation
To build and install the plugin, JDK 1.8 or above (https://www.oracle.com/java/technologies/downloads/) and Apache Maven (https://maven.apache.org/index.html) are required.
Make sure that the system environment variable `PROTEGE_HOME` is set to your installation of Protégé 5.0.

Now to build and install the plugin, open the command prompt as administrator in the standpoint-reasoning directory and execute
	```sh
	mvn install
	```
If the build was successful, you should be able to see `slowl-0.1.0-SNAPSHOT.jar` in the plugins folder of your Protégé installation. The new SLOWL reasoner (name not final) should now be available in the Reasoner menu when running Protégé.
	
