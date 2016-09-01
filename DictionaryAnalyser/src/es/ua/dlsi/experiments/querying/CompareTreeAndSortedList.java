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

package es.ua.dlsi.experiments.querying;

import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.id3.InstanceCollection;
import es.ua.dlsi.id3.NotInTreeException;
import es.ua.dlsi.id3.Tree;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.EquivalentCandidates;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.paradigms.paradigmprofiling.ParadigmProfiler;
import es.ua.dlsi.querying.RankedCandidate;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.NotInListException;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.*;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class CompareTreeAndSortedList {
    
    public static int NumberQuestionsScoredTree(SortedSetOfCandidates
            scored_candidates, Paradigm paradigm, String stem, Dictionary dic, 
            Candidate candidate, PrintWriter treepw){
        int numberofquestions=-1;
        Set<String> possiblesurfaceforms=new LinkedHashSet<String>();
        //the key of this map is the set of surface forms and the value is the set of paradigms generating them
        Set<EquivalentCandidates> sf_candidate=
                new LinkedHashSet<EquivalentCandidates>();
        for(RankedCandidate qc: scored_candidates.getCandidates()){
            possiblesurfaceforms.addAll(qc.getSurfaceForms(dic));
            sf_candidate.add(qc);
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

            numberofquestions=tree.QuestionsToParadigm(candidate);

        }catch(NotInTreeException ex){
            System.out.println("Error: correct candidate for "+
                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
        }
        return numberofquestions;
    }
    
    public static int NumberQuestionsNonScoredTree(Set<Candidate> candidates, 
            Paradigm paradigm, String stem, Dictionary dic, Candidate candidate,
            PrintWriter treepw,  boolean notclosedcats){
        
        int numberofquestions=-1;
        
        if(candidates.isEmpty()){
            System.err.println("Warning: no candidates for candidate "+stem+"/"+paradigm.getName());
        }
        else{
            Set<String> possiblesurfaceforms=new LinkedHashSet<String>();
            //the key of this map is the set of surface forms and the value is the set of paradigms generating them
            Set<EquivalentCandidates> sf_candidate=new LinkedHashSet<EquivalentCandidates>();
            for(Candidate c: candidates){
                if(!notclosedcats || ! c.isClosedCategoryParadigm(dic)){
                    boolean added=false;
                    for(EquivalentCandidates ec: sf_candidate){
                        if(c.GetSurfaceForms(dic).equals(ec.getSurfaceForms(dic))){
                            ec.addCandidate(c);
                            added=true;
                            break;
                        }
                    }
                    if(!added){
                        sf_candidate.add(new EquivalentCandidates(c));
                        possiblesurfaceforms.addAll(c.GetSurfaceForms(dic));
                    }
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
                numberofquestions=tree.QuestionsToParadigm(candidate);

            }catch(NotInTreeException ex){
                System.out.println("Error: correct candidate for "+
                        stem+";"+paradigm.getName()+" is not in the ID3 tree.");
            }
        }
        
        return numberofquestions;
    }
    
    public static int NumberQuestionsSortedList(SortedSetOfCandidates scored_candidates,
            Paradigm paradigm, String stem, Dictionary dic, Candidate candidate){
        
        int numberofquestions_score=-1;
        try{
            numberofquestions_score=scored_candidates.QuestionsToParadigm(candidate, dic);
        }catch(NotInListException ex){
            System.out.println("Error: correct candidate for "+
                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
        }
        return numberofquestions_score;
    }
    
    
    public static void ComputeQuestions(Candidate candidate, Dictionary dic,
            Vocabulary vocabulary, boolean remove1entry, boolean notclosedcats,
            Dix2suffixtree d2s, String plf_tmp, PrintWriter pw, PrintWriter
                    scoredtreepw, PrintWriter nonscoredtreepw){
        
        Pardef pardef=dic.pardefs.getParadigmDefinition(candidate.getParadigm());
        if(pardef!=null){
            ParadigmProfiler pp=new ParadigmProfiler(
                    new Paradigm(pardef, dic),dic);
            if(!remove1entry || pp.NumberOfWords()>1){
                String stem=candidate.getStem();
                String bestsurfaceform;

                Pardef p= dic.pardefs.getParadigmDefinition(candidate.getParadigm());
                Paradigm paradigm=new Paradigm(p, dic);

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


                //If indicated, entries generating forms from a closed category may be discarded
                if(!notclosedcats || !paradigm.isClosedCategory()){
                    //Sete of scored candidates
                    SortedSetOfCandidates scored_candidates=d2s.CheckNewWord(
                            bestsurfaceform, vocabulary, plf_tmp, null, notclosedcats);
                    //Getting scored candidates
                    if(scored_candidates.GetNumberOfDifferentCandidates()==0){
                        System.err.println("Warning: no candidates for candidate "+stem+"/"+paradigm.getName());
                    }
                    else{
                        Set<Candidate> candidates=new LinkedHashSet<Candidate>();
                        candidates.addAll(d2s.getSuffixTree().SegmentWord(bestsurfaceform));

                        //If indicated, entries generating forms from a closed category may be discarded
                        if(!notclosedcats || !paradigm.isClosedCategory()){
                            //Printing the output
                            pw.print(bestsurfaceform);
                            pw.print(";");
                            pw.print(stem);
                            pw.print(";");
                            pw.print(paradigm.getName());
                            pw.print(";");
                            pw.print(scored_candidates.getCandidates().size());
                            pw.print(";");
                            pw.print(PositionOfBestCandidateInSortedList(
                                    scored_candidates, paradigm,
                                    stem, candidate));
                            pw.print(";");
                            pw.print(NumberQuestionsScoredTree(
                                    scored_candidates, paradigm,
                                    stem, dic, candidate, scoredtreepw));
                            pw.print(";");
                            pw.print(NumberQuestionsNonScoredTree(
                                    candidates, paradigm, stem, dic,
                                    candidate, nonscoredtreepw,
                                    notclosedcats));
                            pw.print(";");
                            pw.print(NumberQuestionsSortedList(
                                    scored_candidates, paradigm,
                                    stem, dic, candidate));
                            pw.println();
                            pw.flush();
                        }
                    }
                }
                else{
                    System.err.println("Closed category: "+paradigm.getName());
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
    
    public static int PositionOfBestCandidateInSortedList(SortedSetOfCandidates
            scored_candidates, Paradigm paradigm, String stem, Candidate candidate){
        
        int position=-1;
        
        try{
            position=scored_candidates.GetCandidatePosition(candidate);
        }catch(NotInListException ex){
            System.out.println("Error: correct candidate for "+
                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
        }
        return position;
    }
        
    
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
        CmdLineParser.Option otest_set = parser.addStringOption('t',"testset");

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
        String test_set_path=(String)parser.getOptionValue(otest_set,null);
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
        PrintWriter scoredtreepw=null;
        if(treeoutput!=null){
            try{
                scoredtreepw=new PrintWriter(treeoutput+".scored");
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file for the tree '"+treeoutput+".scored'.");
                scoredtreepw=new PrintWriter(System.out);
            }
        }
        
        //Preparing output stream
        PrintWriter nonscoredtreepw=null;
        if(treeoutput!=null){
            try{
                nonscoredtreepw=new PrintWriter(treeoutput);
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file for the tree '"+treeoutput+".nonscored'.");
                nonscoredtreepw=new PrintWriter(System.out);
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

        //If no testset is defined, the experiment is performed in a leave-one-out
        //fashion on all the entries of the dictionary
        if(test_set_path==null){
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
                            ComputeQuestions(candidate, dic, vocabulary,
                                    remove1entry, notclosedcats, d2s, plf_tmp,
                                    pw, scoredtreepw, nonscoredtreepw);
                        }else{
                            System.err.println("Entry "+e.toString()+" does not contain any paradigm");
                        }
                    }
                    s.elements.add(i, e);
                }
            }
        }
        //If testset is defined, the experiment is performed only on it
        else{
            try{
                BufferedReader testsetreader=new BufferedReader(new FileReader(test_set_path));
                String line;
                while((line=testsetreader.readLine())!=null){
                    String[] candidate_splitten=line.split("\\|");
                    Candidate candidate=new Candidate(candidate_splitten[0], candidate_splitten[1]);
                    E e=null;
                    int pos=0;
                    Section s=null;
                    for(Section sec: dic.sections){
                        for(int i=0;i<sec.elements.size();i++){
                            e=sec.elements.get(i);
                            if(!e.isMultiWord()){
                                //Getting the stema nd paradign of the entry
                                Candidate candidate_tmp=DicEntry.GetStemParadigm(e);
                                if(candidate.equals(candidate_tmp)){
                                    sec.elements.remove(i);
                                    s=sec;
                                    pos=i;
                                    break;
                                }
                            }
                        }
                    }
                    if(candidate!=null && s!=null){
                        ComputeQuestions(candidate, dic, vocabulary,
                                    remove1entry, notclosedcats, d2s, plf_tmp,
                                    pw, scoredtreepw, nonscoredtreepw);
                        s.elements.add(pos, e);
                    }
                    else{
                        System.err.println("Candidate "+candidate+" could not be found in the dictionary");
                    }
                }
            }
            catch(FileNotFoundException fnfe){
                System.err.println("Testset file "+test_set_path+" could not be found.");
                System.exit(-1);
            }
            catch(IOException ioe){
                System.err.println("Testset file "+test_set_path+" could not be found.");
                System.exit(-1);
            }
        }
        pw.close();
        if(scoredtreepw!=null) {
            scoredtreepw.close();
        }
        if(nonscoredtreepw!=null) {
            nonscoredtreepw.close();
        }
    }
}
