#! /bin/bash

# Getting the CSV with every word in the Spanish dictionary and the corresponding paradigm, getting the position of this word in the sorted list of candidates when no bilingual information is provided and when the bilingual dictionary is used and the Catalan POS category is konwn. The format of the output is:
# stem;paradigm;position_in_the_candidates_list_when_NOT_using_bilingual_information;position_in_the_candidates_list_when_using_bilingual_information;
echo "List of words in the Spanish dictionary and the position of the right candidate in the sorted list of candidates when knowing the lexical category of the translation in Catalan and when it is not known."
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.suffixtree.CheckCorrectCandidatePositionLeaveOneOutParadigm -l ../../apertium-es-ca/apertium-es-ca.ca.dix -r ../../apertium-es-ca/apertium-es-ca.es.dix -b ../../apertium-es-ca/apertium-es-ca.es-ca.dix --remove-closedcats --remove-1entrypars -v ../../vocabulary.txt
