#!/usr/bin/python
import sys
import gzip
from math import sqrt,exp
import argparse
import json
from random import shuffle

def mrr(distr,size):
  mrr=0.
  all=0
  for k in distr:
    mrr+=distr[k]/float(k)
  return mrr/size

if __name__=='__main__':
  parser=argparse.ArgumentParser(description='Ranker of the Paradigm Association Tool (PAT).\nThe tool ranks (paradigm,lemma) candidates for each OOV.')
  parser.add_argument('candidates',help='path to the output of the candidate generator')
  parser.add_argument('corpus',help='path to the corpus token frequencies')
  parser.add_argument('output',help='path to the gzipped json output file')
  args=parser.parse_args()
  corpus=dict([(b,int(a)) for a,b in [e.decode('utf8').strip().split(' ') for e in open(args.corpus)]])
  sys.stderr.write('Corpus loaded\n')
  counter=0
  ranks={}
  for entry_json in gzip.open(args.candidates):
    counter+=1
    entry=json.loads(entry_json)
    surface=entry['surfaceword']
    try:
      correct=entry['correct_candidate']
    except:
      correct=None
    output={'surface_form':surface}
    output_candidates=[]
    for candidate in entry['candidates']:
      forms=set([e['surfaceform'] for e in candidate['expansion']])
      attested=[form for form in forms if form in corpus]
      sum_attested=sum([corpus[form] for form in attested])
      num_attested=len(attested)
      num_forms=len(forms)
      output_candidates.append({'lemma':candidate['lemma'],'paradigm':candidate['paradigm'],'expanded':candidate['expansion'],'probability':num_attested/sqrt(num_forms)})
    shuffle(output_candidates)
    output['candidates']=sorted(output_candidates,key=lambda x:-x['probability'])
    probs=[exp(e['probability']) for e in output['candidates']]
    sum_probs=sum(probs)
    for index,candidate in enumerate(output['candidates']):
      candidate['probability']=probs[index]/sum_probs
    if correct!=None:
      found=False
      for index,candidate in enumerate(output['candidates']):
        if correct['paradigm']==candidate['paradigm'] and correct['lemma']==candidate['lemma']:
          ranks[index+1]=ranks.get(index+1,0)+1
          break
    if counter%10==0:
      sys.stderr.write('Processed '+str(counter)+'\n')
  sys.stderr.write('Starting writing output\n')
  gzip.open(args.output,'w').write(json.dumps(output))
  sys.stderr.write('Finished writing output\n')
  if correct!=None:
    sys.stderr.write('Mean reciprocal rank (MRR) on the given dataset is '+str(mrr(ranks,counter))+'\n')
    sys.stderr.write('The distribution of the first position with a correct candidate is '+str(sorted(ranks.items()))+'\n')
