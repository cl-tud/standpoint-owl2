# Standpoint-Reasoning Plugin for Protégé

## Description
A reasoner plugin for Protégé 5.0 implementing standpoint reasoning.
The goal is to write a reasoner plugin which acts as a wrapper for a usual DL reasoner (e.g. HermiT). It should parse special annotation properties simulating the syntax of standpoint logic, translate them to standard SROIQ,
call the existing reasoner and return the results (similar to the FuzzyOWL2 plugin).
