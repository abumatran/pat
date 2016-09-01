#! /bin/bash

# Getting the CSV with every word in the Spanish dictionary and the corresponding paradigm, getting the position of this word in the sorted list of candidates when no bilingual information is provided and when the bilingual dictionary is used and the Catalan POS category is konwn. The format of the output is:
# stem;paradigm;position_in_the_candidates_list_when_NOT_using_bilingual_information;position_in_the_candidates_list_when_using_bilingual_information;
echo "List of words in the Spanish dictionary and the position of the right candidate in the sorted list of candidates when knowing the lexical category of the translation in Catalan and when it is not known."
#java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.CheckCorrectCandidatePositionLeaveOneOut -d ../../apertium-es-ca/apertium-es-ca.es.dix --remove-closedcats --remove-1entrypars -v ../../vocabulary.txt --tree-output ./treeall.dot
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.CheckCorrectCandidatePositionLeaveOneOut -d ../../apertium-es-ca/apertium-es-ca.es.dix --remove-closedcats -v ../../vocabulary.txt --tree-output ./treeall.dot
exit
#Printing trees
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w lo -o tree.dot
dot -Tps tree.dot > lo.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w todo -o tree.dot
dot -Tps tree.dot > todo.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w siervo -o tree.dot
dot -Tps tree.dot > siervo.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w BBC -o tree.dot
dot -Tps tree.dot > BBC.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w UVic -o tree.dot
dot -Tps tree.dot > UVic.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w orador -o tree.dot
dot -Tps tree.dot > orador.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w acusador -o tree.dot
dot -Tps tree.dot > acusador.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w administrador -o tree.dot
dot -Tps tree.dot > administrador.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w prestamista -o tree.dot
dot -Tps tree.dot > prestamista.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w Ferrari -o tree.dot
dot -Tps tree.dot > Ferrari.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w Amara -o tree.dot
dot -Tps tree.dot > Amara.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w Antiguo -o tree.dot
dot -Tps tree.dot > Antiguo.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w Núria -o tree.dot
dot -Tps tree.dot > Núria.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w Pau -o tree.dot
dot -Tps tree.dot > Pau.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w lesbiana -o tree.dot
dot -Tps tree.dot > lesbiana.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w clarisas -o tree.dot
dot -Tps tree.dot > clarisas.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w casta -o tree.dot
dot -Tps tree.dot > casta.ps
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.id3.SuffixTreeCandidatesAsInstances -d ../../apertium-es-ca/apertium-es-ca.es.dix --not-closedcats -w bajo -o tree.dot
dot -Tps tree.dot > bajo.ps

rm tree.dot
