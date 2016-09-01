#! /bin/bash

# Building the suffix tree for a dictionary and getting, for every word, the list of all the compatible paradigms. The output format is:
# [correct_stem|correct_paradigm];candidate_stem2|candidate_paradigm2;...candidate_stemN|candidate_paradigmN;  
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.Hw1 -i instancesfile.txt > tree-from-instances.dot
dot -Tps tree-from-instances.dot > tree-from-instances.ps
