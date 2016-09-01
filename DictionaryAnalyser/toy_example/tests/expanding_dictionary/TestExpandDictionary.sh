#! /bin/bash

# Expanding a dictionary to get all the possible surface/lexical forms and the combinations stem/paradigm that generates them. Output format:
# paradigm+stem;surface_form
echo "Expanindg the whole dictionary to get all the surface forms that can be generated/recognised by it:"
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.paradigms.ExpandAllDic -d ../../apertium-es-ca/apertium-es-ca.es.dix --no-closedcats

echo "Expanindg the whole dictionary to get all the lexical forms that can be generated/recognised by it:"
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.paradigms.ExpandAllDic -d ../../apertium-es-ca/apertium-es-ca.es.dix --no-closedcats -l
