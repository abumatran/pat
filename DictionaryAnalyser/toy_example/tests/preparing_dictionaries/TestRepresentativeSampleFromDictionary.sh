#! /bin/bash

# Testing the method for removing some words from the dictionary
echo "Cleaning a set of words from the dictionary. The output is writen in the file clean_apertium-es-ca.es.dix:"
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.monolingual.RepresentativeSampleOfEntries -d ../../apertium-es-ca/apertium-es-ca.es.dix -c ../../vocabulary.txt -f 0,5 -t 1
