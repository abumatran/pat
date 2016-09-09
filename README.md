# PAT -- Paradigm association tool

The PAT tool is useful for producing ranked (paradigm, lemma) pairs of candidates for an input list of OOVs and an existing Apertium dix. The tool can not suggest new paradigms, just associations of OOVs to already existing paradigms.

There are three main steps necessary to produce the ranked output:

1. producing a frequency list of tokens from a corpus / Wikipedia dump
2. generating candidates of (paradigm, lemma) pairs from a list of OOVs
3. ranking the candidates by using the corpus information

These three steps are described in the following three subsections.

## Resources

To be able to rank the (paradigm, lemma) candidates for an OOV, we need corpus frequency information. This information can be obtained through various sources. If you do not have a frequency list like the Italian example file available from [Resources/Wikipedia/ranking.ita](Resources/Wikipedia/ranking.ita), you can find instructions on how to obtain one from a Wikipedia dump on [Resources/Wikipedia/](Resources/Wikipedia/).

## DictionaryAnalyser

This tool is aimed at analysing a dictionary to build the collection of candidate stem/paradigm pairs to represent a new surface form to be added to the dictionary. This is done by means of a generalised suffix tree that splits the surface form in all the possible combinations stem/suffix, and then tries to identify all the paradigms that generate this suffix. Once this is done, all the possible inflected forms that can be generated by every candidate suffix/paradigm pair are generated, which are used by the Ranker tool to obtain a ranking of the most likely candidates.

To compile the tool, it is only necessary to have the tool ```ant``` and run the command ```ant jar```. This will create a directory ```dist``` containing the binary ```DictionaryAnalyser.jar```.

This tool can be used either to check a single surface form:

```java -cp dist/DictionaryAnalyser.jar es.ua.dlsi.experiments.monolingual.CandidatesForWord -d <DICTIONARY_FILE> -s <SURFACE_FORM>```

 r for a list of surface forms (for example, the list of words that could not be translated in a given text) stored in a text file (one word per line):

```java -cp dist/DictionaryAnalyser.jar es.ua.dlsi.experiments.monolingual.CandidatesForWord -d <DICTIONARY_FILE> --surfaceforms-list <FILE>```

The additional option  ```-v``` can be used to specify the part-of-speech categories that are accepted for the candidates proposed. To do so, the user must specify a comma-separated list of valid lexical categories.


## BuildDataset

Tool for building the datasets needed for training/testing the Ranker. This tool takes an Apertium dictionary in format .dix, expands all the entries in the dictionary, and detects, for each of the surface forms generated, which candidate pairs stem/paradigm could describe the surface form. The entries used to build the datasets can be filtered by defining a collection of valid lexical categories.

Two datasets can be created with this tool: a training set and a test set, both with the same structure. The size of each dataset is defined by setting the ratio of entries that will be used to build the training set (if the ratio is 1.0 no test set will be created). 

As mentioned above, each dataset consists of a list of JSON objects (one per line) that represent an evaluation instance. Every evaluation instance contains:
 - the surface form of the word being evaluated;
 - the gold candidate (the pair stem/paradigm defined in the dictionary); and
 - all the possible stem/paradigm candidates in the dictionary that could describe the word.

Every candidate in the JSON file consists of:
 - a stem;
 - a paradigm;
 - a lemma (the representative form for this pair stem/lemma); 
 - a list of surface forms that could be generated by combining the stem and the lemma; every surface form is provided togheter with the collection of lexical information (labels) in the corresponding paradigm.

To compile the tool, it is only necessary to have the tool ```ant``` and run the command ```ant jar```. This will create a directory ```dist``` containing the binary ```BuildDataset.jar```.

To tool is run as follows:
```
java -jar dist/BuildDataset.jar -p <TRAINING_RATIO> -c <WORD_FREQUENCY_IN_CORPUS> -d <DIX_DICTIONARY> -v <LIST_OF_LEXICAL_CATEGORIES> --training-output <OUTPUT_FILE_FOR_TRAINING_SET> --test-output <OUTPUT_FILE_FOR_TRAINING_SET>
```
Where options correspond to:
 - ```-p``` a value between 0 and 1 that defines the ratio of the entries to be used to build the training set (if the value is set to 0, no test set is generated)
 - ```-c``` a file containing a list of word frequencies such those provided in the directory Resources
 - ```-d``` Apertium dictionary in format .dix
 - ```-v``` Comma-separated list of valid lexical categories
 - ```--training-output``` File where the training dataset will be written
 - ```--test-output``` File where the test dataset will be written

## Ranker

For now the ranking is performed by following the simple heuristic from ([http://link.springer.com/article/10.1007/s10579-016-9360-9](Esplà-Gomis et al. 2016)).

This ranker combines the first step output (corpus frequency calculation, [Resources/Wikipedia](Resources/Wikipedia)) and the second step output (candidate generation, [BuildDataset/](BuildDataset/)) of the tool.

An example run of the tool, if generated candidates for Italian can be found on ```BuildDataset/example_ita_candidates.gz``` and the Italian Wikipedia token frequencies on ```Resources/Wikipedia/ranking.ita```, is this:

```
$ python Ranker/rank.py BuildDataset/example_ita_candidates.gz Resources/Wikipedia/ranking.ita Ranker/example_ita_candidates.out.gz
Corpus loaded
Processed 10
Processed 20
Processed 30
Processed 40
Processed 50
Processed 60
Processed 70
Processed 80
Processed 90
Processed 100
Starting writing output
Finished writing output
Mean reciprocal rank (MRR) on the given dataset is 0.526463052849
The distribution of the first position with a correct candidate is [(1, 48), (3, 4), (4, 2), (8, 8), (12, 1), (17, 6), (19, 17), (23, 6), (26, 1), (27, 1), (31, 1), (35, 1), (41, 2), (47, 1), (68, 1)]
```

The output of the ranker can be observed in the output file ```Ranker/example_ita_candidates.out.gz```. It is a gzipped json file which can be loaded in the Apertium paradigm association frontend.
