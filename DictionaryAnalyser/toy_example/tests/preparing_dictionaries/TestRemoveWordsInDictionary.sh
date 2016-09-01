#! /bin/bash

# Testing the method for removing some words from the dictionary
echo "Cleaning a set of words from the dictionary. The output is writen in the file clean_apertium-es-ca.es.dix:"
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.monolingual.RemoveWordsFromDic -d ../../apertium-es-ca/apertium-es-ca.es.dix -w words_to_remove.txt -o clean_apertium-es-ca.es.dix
