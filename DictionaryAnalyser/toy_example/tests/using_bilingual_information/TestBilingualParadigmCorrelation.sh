#! /bin/bash

# Checking the correlation between the correlation between the paradigms in the Spanish dictionary into the Catalan dictionary. Closed categories and words which appear in a dictionary which is not assigned to any other word are removed.
echo "Correspondence between Spanish paradigms and Catalan paradigms:" 
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.bilingual.AnalyseBilingParamRelationship -l ../../apertium-es-ca/apertium-es-ca.ca.dix -r ../../apertium-es-ca/apertium-es-ca.es.dix -b ../../apertium-es-ca/apertium-es-ca.es-ca.dix --remove-closedcats --remove-1entrypars

# Checking the correlation between the correlation between the paradigms in the Catalan dictionary into the Spanish dictionary. Closed categories and words which appear in a dictionary which is not assigned to any other word are removed. 
echo "Correspondence between Catalan paradigms and Spanish paradigms:"
java -cp ../../../dist/DictionaryAnalyser.jar es.ua.dlsi.bilingual.AnalyseBilingParamRelationship -l ../../apertium-es-ca/apertium-es-ca.ca.dix -r ../../apertium-es-ca/apertium-es-ca.es.dix -b ../../apertium-es-ca/apertium-es-ca.es-ca.dix --remove-closedcats --remove-1entrypars --reverse
