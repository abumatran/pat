# The PAT ranker

For now the ranking is performed by following the simple heuristic from Espla et al.

This ranker combines the first (corpus frequency calculation) and the second step (candidate generation) of the tool.

An example run of the tool, if exemplary generated candidates for Italian can be found on ```.../DictionaryAnalyser/example_ita_candidates.gz``` and the Italian Wikipedia token frequencies on ```../Resources/Wikipedia/ranking.ita```, is this:

```
$ python rank.py ../DictionaryAnalyser/example_ita_candidates.gz ../Resources/Wikipedia/ranking.ita example_ita_candidates.out.gz
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

The output of the ranker can be observed in the output file ```example_ita_candidates.out.gz```. It is a gzipped json file which can be loaded in the Apertium paradigm association frontend.
