/**************************************************************************
 DictionaryAnalyser - Package based in DixTools and created to provide a set
               of tools that ease the addition of new entries to dictionaries
               and helps to analyse the dictionaries.

 Copyright (C) 2011-2012 Universitat d'Alacant [www.ua.es]

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package es.ua.dlsi.experiments.lexicalinformation;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.E;
import dics.elements.dtd.Section;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.lexicalinformation.AnalysedExampleSentence;
import es.ua.dlsi.lexicalinformation.Corpus;
import es.ua.dlsi.lexicalinformation.LMScorerBerkeley;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import es.ua.dlsi.utils.ModificableInteger;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class GessingLexicalInformation {
public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option ocorpus = parser.addStringOption('c',"corpus");
        CmdLineParser.Option oapertiummode = parser.addStringOption('m',"apertium-mode");
        CmdLineParser.Option olangmodel = parser.addStringOption('l',"lm");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option onotclosedcategories = parser.addBooleanOption("not-closedcats");

        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        String dicpath=(String)parser.getOptionValue(odicpath,null);
        String corpus_path=(String)parser.getOptionValue(ocorpus,null);
        String apertiummode=(String)parser.getOptionValue(oapertiummode,null);
        String langmodel=(String)parser.getOptionValue(olangmodel,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);
        
        if(dicpath==null){
            System.err.println("Error: undefined path to the dictionary file to be processed."
                    + " Use option -d to define it.");
            System.exit(-1);
        }
        
        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        System.err.print("Building suffix tree... ");
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        System.err.println("Suffix tree built!");
        System.err.print("Loading corpus... ");
        
        if(corpus_path==null){
            System.err.println("Error: undefined path to the corpus file from which"
                    + " the examples are to be read. Use option -c to define it.");
            System.exit(-1);
        }
        Corpus corpus=new Corpus(corpus_path);
        
        System.err.println("Corpus loaded!");
        
        if(langmodel==null){
            System.err.println("Error: undefined path to the ARPA language model"
                    + " file. Use option -l to define it.");
            System.exit(-1);
        }
        LMScorerBerkeley scorer=new LMScorerBerkeley(langmodel);
        
        PrintWriter pw;
        try{
            pw=new PrintWriter(output);
        }
        catch(FileNotFoundException ex){
            System.err.println("The output file could not be open; the output "
                    + "will be redirected to the standard output");
            pw=new PrintWriter(System.out);
        }
        catch(NullPointerException ex){
            System.err.println("Undefined output file : the output will be "
                    + "redirected to the standard output");
            pw=new PrintWriter(System.out);
        }
        
        //We walk over the sections of the dictionary to visit all the elements
        for(Section section: dic.sections){
            for(E element: section.elements){
                if(!element.isMultiWord()){
                    //We obtain the candidate stem/paradigm for a given element
                    Candidate elem_stem_paradigm=DicEntry.GetStemParadigm(element);
                    if(elem_stem_paradigm!=null){//If it has a paradigm
                        //Set<String> examples=new LinkedHashSet<String>();
                        //Checking if closed categories are accepted. If not, checking if paradigm belongs to a closed category
                        if(!notclosedcategories || !elem_stem_paradigm.isClosedCategoryParadigm(dic)){
                            System.err.println("Checking candidate "+elem_stem_paradigm);
                            //We obtan all the possible surface forms / lexical forms of the element
                            Set<String> surfaceforms=elem_stem_paradigm.GetSurfaceForms(dic);
                            Set<Candidate> candidates=new LinkedHashSet<Candidate>();
                            for(String form: surfaceforms) {
                                candidates.addAll(d2s.getSuffixTree().SegmentWord(form));
                            }
                            //List of candidates with the number of examples in which they won
                            Map<Candidate,ModificableInteger> candidate_wins=new HashMap<Candidate, ModificableInteger>();
                            for(Candidate c: candidates) {
                                candidate_wins.put(c, new ModificableInteger(0));
                            }
                            candidates.remove(elem_stem_paradigm);
                            Set<Candidate> equal_candidates=new LinkedHashSet<Candidate>();
                            equal_candidates.add(elem_stem_paradigm);

                            for(Candidate candidate: candidates){
                                Set<String> expandedcandidate=candidate.GetSurfaceForms(dic);
                                if(surfaceforms.equals(expandedcandidate)) {
                                    equal_candidates.add(candidate);
                                }
                            }
                            //Checking the examples in the corpus for each surface form
                            for(String form: surfaceforms){
                                Set<String> new_examples=corpus.GetAllExamples(form);
                                for(String example: new_examples){
                                    //We analyse each example using a possible lexical form in the candidates
                                    AnalysedExampleSentence analysed_example=new
                                            AnalysedExampleSentence(example, form, apertiummode);
                                    double best_score=Double.POSITIVE_INFINITY;
                                    Set<Candidate> winning_candidates=null;
                                    //We check which is the winning candidate for a given example
                                    for(Candidate c: equal_candidates){
                                        Set<Suffix> possibleanalisys=c.GetExpansion(dic).get(form);
                                        for(Suffix analisys: possibleanalisys){
                                            //System.err.println(AnalysedExampleSentence.GetAllTags(analysed_example.
                                            //        GetAnalisysWithGivenOption(analisys.toString())));
                                            String option=AnalysedExampleSentence.GetAllTags(analysed_example.
                                                    GetAnalisysWithGivenOption(analisys.toString()));
                                            double score=scorer.Score(option);
                                            if(best_score>score){
                                                best_score=score;
                                                winning_candidates=new LinkedHashSet<Candidate>();
                                                winning_candidates.add(c);
                                            }
                                            //Candidates obtaining the same result are considered winners
                                            else if(best_score==score) {
                                                winning_candidates.add(c);
                                            }
                                        }
                                    }
                                    for(Candidate c: winning_candidates) {
                                        candidate_wins.get(c).increment();
                                    }
                                }
                            }
                            Candidate best_candidate=null;
                            int best_score=0;
                            for(Entry<Candidate,ModificableInteger> result: candidate_wins.entrySet()){
                                if(result.getValue().getValue()>best_score){
                                    best_score=result.getValue().getValue();
                                    best_candidate=result.getKey();
                                }
                            }
                            if(best_candidate!=null) {
                                pw.println(elem_stem_paradigm.getStem()+"|"+
                                        elem_stem_paradigm.getParadigm()+":"+
                                        best_candidate.getStem()+"|"+
                                        best_candidate.getParadigm());
                            }
                        }
                    }
                }
            }
        }
    }
}
