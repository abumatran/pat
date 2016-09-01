Example for Slovene. 

Text extraction from Wikipedia dumps

~~~~
$ python3 WikiExtractor.py --infn slwiki-latest-pages-articles.xml.bz2 >/dev/null && mv wiki.txt slwiki.txt
~~~~

Naive sentence division + Apertium-based tokenization

~~~~
$ sed 's/\([[:alpha:]]\)\([.][ ]\)/\1\2\n/g' slwiki.txt |\
apertium-destxt | lt-proc -a slv.automorf.bin | python3 anmor2tok.py > slwiki.tok
~~~~

Truecasing (using Moses tools)
~~~~
$ perl train-truecaser.pl --model truecaser.sl --corpus slwiki.tok
$ perl truecaser.pl --model truecaser.sl <slwiki.tok >slwiki.true
~~~~

Frequency lists
~~~~
$ tr ' ' '\n' < slwiki.true | sort |uniq -c | sort -nr |grep "[[:alpha:]]" >ranking.slv
~~~~
