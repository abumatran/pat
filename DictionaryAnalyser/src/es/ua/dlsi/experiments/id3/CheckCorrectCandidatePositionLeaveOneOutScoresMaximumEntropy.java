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

package es.ua.dlsi.experiments.id3;

import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.features.FeatureExtractor;
import es.ua.dlsi.features.FeatureSet;
import es.ua.dlsi.id3.InstanceCollection;
import es.ua.dlsi.id3.NotInTreeException;
import es.ua.dlsi.id3.Tree;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.EquivalentCandidates;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.paradigms.paradigmprofiling.ParadigmProfiler;
import es.ua.dlsi.querying.RankedCandidate;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.*;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.pmml.consumer.PMMLClassifier;
import weka.core.pmml.PMMLFactory;
import weka.core.pmml.PMMLModel;

/**
 *
 * @author miquel
 */
public class CheckCorrectCandidatePositionLeaveOneOutScoresMaximumEntropy {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option oremove1entry = parser.addBooleanOption("remove-1entrypars");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option otreeoutput = parser.addStringOption("tree-output");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        CmdLineParser.Option oplf_tmp = parser.addStringOption('p',"plf-tmp-path");
        CmdLineParser.Option olrm = parser.addStringOption('m',"linear-regression-model");

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

        String dictionary=(String)parser.getOptionValue(odictionary,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String treeoutput=(String)parser.getOptionValue(otreeoutput,null);
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String plf_tmp=(String)parser.getOptionValue(oplf_tmp,null);
        String lrm=(String)parser.getOptionValue(olrm,null);
        boolean remove1entry=(Boolean)parser.getOptionValue(oremove1entry,false);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);

        //Preparing output stream
        PrintWriter pw;
        if(output!=null){
            try{
                pw=new PrintWriter(output);
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file '"+output+"'.");
                pw=new PrintWriter(System.out);
            }
        } else{
            System.err.println("Warning: output file not defined. Output redirected to standard output.");
            pw=new PrintWriter(System.out);
        }
        
        //Preparing output stream
        PrintWriter treepw=null;
        if(treeoutput!=null){
            try{
                treepw=new PrintWriter(treeoutput);
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file for the tree '"+treeoutput+"'.");
                treepw=new PrintWriter(System.out);
            }
        }

        //Reading the vocabulary
        Vocabulary vocabulary=null;
        try {
            vocabulary=new Vocabulary(vocabularypath);
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: File '"+vocabularypath+"' could not be found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error while reading file '"+vocabularypath+"' could not be found.");
            System.exit(-1);
        }

        //Reading the dictionary and generating the set of lexical forms
        DictionaryReader dicReader = new DictionaryReader(dictionary);
        Dictionary dic=dicReader.readDic();
        
        //Building the suffix tree
        Dix2suffixtree d2s;
        d2s=new Dix2suffixtree(dic);
        
        FeatureExtractor featextractor=new FeatureExtractor(dic, vocabulary, d2s, plf_tmp);
        
        LinearRegression lrmodel=null;
        try {
            PMMLModel pmmlModel = PMMLFactory.getPMMLModel(lrm);
            if (pmmlModel instanceof PMMLClassifier) {
                Classifier classifier = ((PMMLClassifier)pmmlModel);
                lrmodel=(LinearRegression)classifier;
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }

        //Loop that goes all over the entries of the dictionary
        for(Section s: dic.sections){
            for(int i=0;i<s.elements.size();i++){
                E e=s.elements.remove(i);
                //If the entry is a multiword is discarded
                if(e.isMultiWord()){
                    System.err.println("Multiword: "+e.toString());
                }
                else{
                    //Getting the stema nd paradign of the entry
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    if(candidate!=null){
                        Pardef pardef=dic.pardefs.getParadigmDefinition(candidate.getParadigm());
                        if(pardef!=null){
                            ParadigmProfiler pp=new ParadigmProfiler(
                                    new Paradigm(pardef, dic),dic);
                            if(!remove1entry || pp.NumberOfWords()>1){
                                String stem=candidate.getStem();
                                String bestsurfaceform;
                                Pardef p= dic.pardefs.getParadigmDefinition(candidate.getParadigm());
                                Paradigm paradigm=new Paradigm(p, dic);
                                
                                //If indicated, entries generating forms from a closed category may be discarded
                                if(!notclosedcats || !paradigm.isClosedCategory()){
                                    //Choosing the most frequent surface form in the vocabulary
                                    bestsurfaceform=vocabulary.GetMostFrequentSurfaceForm(stem, paradigm);
                                    //If no one of the surface forms appear in the vocabulary:
                                    if(bestsurfaceform==null){
                                        System.err.println("Warning: no occurrence for word with stem "
                                                +stem+" and paradigm "+paradigm.getName());
                                        //Random form
                                        bestsurfaceform=stem+paradigm.getSuffixes().iterator().next().getSuffix();
                                    }
                                    //If the lemma cannot be found, the system stops working
                                    if(candidate.GetLemma(dic) ==null){
                                        System.err.println("Error: lemma cannot be generated for stem "+stem+
                                                " and paradigm "+paradigm.getName());
                                        System.exit(-1);
                                    }
                                    //Generating the list of candidates for the most common surface form
                                    //Set<Candidate> candidates=d2s.getSuffixTree().
                                    //        SegmentWord(bestsurfaceform);
                                    SortedSetOfCandidates candidates=d2s.CheckNewWord(
                                            bestsurfaceform, vocabulary, plf_tmp, null,
                                            notclosedcats);
                                    if(candidates.GetNumberOfDifferentCandidates()==0){
                                        String newsurfaceform;
                                        for(Suffix suf: paradigm.getSuffixes()){
                                            newsurfaceform=stem+suf;
                                            if(!newsurfaceform.equals(bestsurfaceform)){
                                                candidates=d2s.CheckNewWord(
                                                        newsurfaceform, vocabulary, null, null,
                                                        notclosedcats);
                                                if(candidates.GetNumberOfDifferentCandidates()>0){
                                                    bestsurfaceform=newsurfaceform;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if(candidates.GetNumberOfDifferentCandidates()==0){
                                        System.err.println("Warning: no candidates for candidate "+stem+"/"+paradigm.getName());
                                    }
                                    else{
                                        Set<String> possiblesurfaceforms=new LinkedHashSet<String>();
                                        //the key of this map is the set of surface forms and the value is the set of paradigms generating them
                                        Set<EquivalentCandidates> sf_candidate=
                                                new LinkedHashSet<EquivalentCandidates>();
                                        for(RankedCandidate qc: candidates.getCandidates()){
                                            possiblesurfaceforms.addAll(qc.getSurfaceForms(dic));
                                            sf_candidate.add(qc);
                                        }
                                        
                                        for(EquivalentCandidates ec: sf_candidate){
                                            RankedCandidate qc=(RankedCandidate)ec;
                                            FeatureSet featset=featextractor.GetFeatureSet(qc, notclosedcats);
                                            try {
                                                double probability=lrmodel.classifyInstance(featset.toWekaInstance());
                                                qc.setScore(probability);
                                            } catch (Exception ex) {
                                                ex.printStackTrace(System.err);
                                            }
                                        }

                                        InstanceCollection records;

                                        // read in all our data
                                        records=new InstanceCollection();
                                        records.buildInstances(possiblesurfaceforms,
                                                sf_candidate,dic);

                                        Tree tree=new Tree(records);
                                        tree.Print(treepw);
                                        treepw.flush();

                                        try{

                                            int numberofquestions=tree.QuestionsToParadigm(candidate);

                                            //Printing the output
                                            pw.println(bestsurfaceform+";"+stem+";"+paradigm.getName()+";"+numberofquestions);
                                            pw.flush();
                                            s.elements.add(i, e);
                                        }catch(NotInTreeException ex){
                                            System.out.println("Error: correct candidate for "+
                                                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
                                        }
                                    }
                                }
                                else{
                                    System.err.println("Closed category: "+e.toString());
                                }
                            }
                            else{
                                System.err.println("Candidate "+candidate.toString()+" not processed: it is the only word in the paradigm");
                            }
                        }
                        else{
                            System.err.println("Paradigm "+candidate.getParadigm()+" does not appear in the dictionary");
                        }
                    }
                    else{
                        System.err.println("Entry "+e.toString()+" does not contain any paradigm");
                    }
                }
            }
        }
        pw.close();
        if(treepw!=null) {
            treepw.close();
        }
    }
}
