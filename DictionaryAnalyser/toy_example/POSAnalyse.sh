#! /bin/bash

cat $1 | lt-proc apertium-es-ca/es-ca.automorf.bin | apertium-tagger -g apertium-es-ca/es-ca.prob
