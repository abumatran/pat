#! /bin/bash

# Building the suffix tree for a dictionary and getting, for every word, the list of all the compatible paradigms. The output format is:
# [correct_stem|correct_paradigm];candidate_stem2|candidate_paradigm2;...candidate_stemN|candidate_paradigmN;  
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.suffixtree.GetSuffixTree -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats
