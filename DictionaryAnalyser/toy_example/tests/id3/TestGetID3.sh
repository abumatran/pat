#! /bin/bash

# Building the suffix tree for a dictionary and getting, for every word, the list of all the compatible paradigms. The output format is:
# [correct_stem|correct_paradigm];candidate_stem2|candidate_paradigm2;...candidate_stemN|candidate_paradigmN;  
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w lo --table instancesfile.txt -o tree.dot
dot -Tps tree.dot > tree.ps
