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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class CompareTreeHMMAndSortedList {
    
    public static int NumberQuestionsScoredTree(SortedSetOfCandidates
            scored_candidates, Paradigm paradigm, String stem, Dictionary dic, 
            Candidate candidate, PrintWriter treepw, Map<Candidate, Double>
                    map_of_scored_candidates){
        int numberofquestions=-1;
        Set<String> possiblesurfaceforms=new LinkedHashSet<String>();
        //the key of this map is the set of surface forms and the value is the set of paradigms generating them
        Set<EquivalentCandidates> sf_candidate=
                new LinkedHashSet<EquivalentCandidates>();
        for(RankedCandidate qc: scored_candidates.getCandidates()){
            possiblesurfaceforms.addAll(qc.getSurfaceForms(dic));
            double total_score=0.0;
            //double max_score=0.0;
            for(Candidate c: qc.getCandidates()){
                double score=map_of_scored_candidates.get(c);
                //if(score>max_score)
                //    max_score=score;
                total_score+=score;
            }
            sf_candidate.add(new RankedCandidate(qc.getCandidates(), total_score, null));
            //sf_candidate.add(new RankedCandidate(qc.getCandidates(), max_score, null));
        }
        
        /*StringBuilder sb=new StringBuilder();
        for(EquivalentCandidates ec: sf_candidate){
            for(Candidate c: ec.getCandidates()){
                sb.append(c);
                sb.append(",");
            }
            sb.append(((RankedCandidate)ec).getScore());
            sb.append("\t");
        }
        System.err.println(sb.toString());*/

        InstanceCollection records;

        // read in all our data
        records=new InstanceCollection();
        records.buildInstances(possiblesurfaceforms,
                sf_candidate,dic);

        Tree tree=new Tree(records);
        if(treepw!=null){
            tree.Print(treepw);
            treepw.flush();
        }

        try{

            numberofquestions=tree.QuestionsToParadigm(candidate);

        }catch(NotInTreeException ex){
            System.out.println("Error: correct candidate for "+
                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
        }
        return numberofquestions;
    }
    
    /*public static int PositionOfCorrectCandidateInEquivParadigm(
            RankedCandidate candidate, Paradigm paradigm, String stem, Dictionary
            dic, PrintWriter treepw, Map<Candidate, Double> map_of_scored_candidates){
        int numberofquestions=-1;
        Set<String> possiblesurfaceforms=new LinkedHashSet<String>();
        //the key of this map is the set of surface forms and the value is the set of paradigms generating them
        Set<EquivalentCandidates> sf_candidate=
                new LinkedHashSet<EquivalentCandidates>();
        for(RankedCandidate qc: scored_candidates.getCandidates()){
            possiblesurfaceforms.addAll(qc.getSurfaceForms(dic));
            double total_score=0.0;
            //double max_score=0.0;
            for(Candidate c: qc.getCandidates()){
                double score=map_of_scored_candidates.get(c);
                //if(score>max_score)
                //    max_score=score;
                total_score+=score;
            }
            sf_candidate.add(new RankedCandidate(qc.getCandidates(), total_score, null));
            //sf_candidate.add(new RankedCandidate(qc.getCandidates(), max_score, null));
        }
        

        InstanceCollection records;

        // read in all our data
        records=new InstanceCollection();
        records.buildInstances(possiblesurfaceforms,
                sf_candidate,dic);

        Tree tree=new Tree(records);
        if(treepw!=null){
            tree.Print(treepw);
            treepw.flush();
        }

        try{

            numberofquestions=tree.QuestionsToParadigm(candidate);

        }catch(NotInTreeException ex){
            System.out.println("Error: correct candidate for "+
                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
        }
        return numberofquestions;
    }*/
    
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
            if(treepw!=null){
                tree.Print(treepw);
                treepw.flush();
            }

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
    
    
    public static void ComputeQuestions(String surfaceform, Candidate candidate,
            Dictionary dic, Vocabulary vocabulary, boolean remove1entry,
            boolean notclosedcats, Dix2suffixtree d2s, String plf_tmp,
            PrintWriter pw, PrintWriter scoredtreepw, PrintWriter nonscoredtreepw,
            Map<Candidate, Double> map_of_scored_candidates){
        
        Pardef pardef=dic.pardefs.getParadigmDefinition(candidate.getParadigm());
        if(pardef!=null){
            ParadigmProfiler pp=new ParadigmProfiler(
                    new Paradigm(pardef, dic),dic);
            if(!remove1entry || pp.NumberOfWords()>1){
                String stem=candidate.getStem();

                Pardef p= dic.pardefs.getParadigmDefinition(candidate.getParadigm());
                Paradigm paradigm=new Paradigm(p, dic);

                //If indicated, entries generating forms from a closed category may be discarded
                if(!notclosedcats || !paradigm.isClosedCategory()){
                    //Sete of scored candidates
                    SortedSetOfCandidates scored_candidates=d2s.CheckNewWord(
                            surfaceform, vocabulary, plf_tmp, null, notclosedcats);
                    //Getting scored candidates
                    if(scored_candidates.GetNumberOfDifferentCandidates()==0){
                        System.err.println("Warning: no candidates for candidate "+stem+"/"+paradigm.getName());
                    }
                    else{
                        Set<Candidate> candidates=map_of_scored_candidates.keySet();
                        //If indicated, entries generating forms from a closed category may be discarded
                        if(!notclosedcats || !paradigm.isClosedCategory()){
                            //Printing the output
                            pw.print(surfaceform);
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
                            pw.print(PositionOfBestCandidateInExtendedSortedList(
                                    scored_candidates, paradigm,
                                    stem, candidate));
                            pw.print(";");
                            pw.print(NumberQuestionsScoredTree(
                                    scored_candidates, paradigm,
                                    stem, dic, candidate, scoredtreepw,
                                    map_of_scored_candidates));
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
            System.err.println("Error: correct candidate for "+
                    stem+";"+paradigm.getName()+" is not in the ID3 tree.");
        }
        return position;
    }
    
    public static int PositionOfBestCandidateInExtendedSortedList(SortedSetOfCandidates
            scored_candidates, Paradigm paradigm, String stem, Candidate candidate){
        
        int position=-1;
        
        try{
            position=scored_candidates.GetCandidatePositionInExtendedList(candidate);
        }catch(NotInListException ex){
            System.err.println("Error: correct candidate for "+
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

        //Preparing output stream for results
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
        
        //Preparing output stream for scored tree
        PrintWriter scoredtreepw=null;
        if(treeoutput!=null){
            try{
                scoredtreepw=new PrintWriter(treeoutput+".scored");
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file for the tree '"+treeoutput+".scored'.");
                scoredtreepw=new PrintWriter(System.out);
            }
        }
        
        //Preparing output stream for non-scored tree
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
        
        try{
            BufferedReader testsetreader=new BufferedReader(new FileReader(test_set_path));
            String line;
            while((line=testsetreader.readLine())!=null){
                String[] fields=line.split("\t");
                String word=fields[0];
                String solution=fields[1];
                String[] scores=fields[2].split(",");
                Map<String, String> scored_elements=new HashMap<String, String>();
                for (String s: scores){
                    String paradigm=s.split(":")[0];
                    scored_elements.put(paradigm, s);
                }

                Map<Candidate, Double> map_of_scored_candidates=new
                        HashMap<Candidate, Double>();
                
                Set<Candidate> candidates=new LinkedHashSet<Candidate>();
                candidates.addAll(d2s.getSuffixTree().SegmentWord(word));
                
                Candidate correct_candidate=null;
                for(Candidate c: candidates){
                    String suffix=word.substring(c.getStem().length());
                    String scored_candidate=null;
                    if(scored_elements.containsKey(c.getParadigm()+"|"+suffix)){
                        scored_candidate=scored_elements.get(c.getParadigm()+"|"+suffix);
                        scored_elements.remove(c.getParadigm()+"|"+suffix);
                        if(solution.equals(c.getParadigm()+"|"+suffix)){
                            correct_candidate=c;
                        }
                    }
                    else if(scored_elements.containsKey(c.getParadigm())){
                        scored_candidate=scored_elements.get(c.getParadigm());
                        scored_elements.remove(c.getParadigm());
                        if(!solution.contains("|") && solution.equals(c.getParadigm())){
                            correct_candidate=c;
                        }
                    }
                    else{
                        System.err.println("CANDIDATE NOT FOUND: "+c+"|"+suffix);
                        System.exit(-1);
                    }
                    if(scored_candidate!=null){
                        String prob=scored_candidate.split(":")[1];
                        if(prob.equals("-nan") || prob.equals("nan"))
                            map_of_scored_candidates.put(c, 0.0);
                        else
                            map_of_scored_candidates.put(c, Double.parseDouble(prob));
                    }
                }
                if(!scored_elements.isEmpty())
                    System.err.println("Remaining candidates in scored"
                            + "candidate list: "+scored_elements.size());
                if(correct_candidate==null){
                    System.err.println("No gold candidate found!");
                    System.exit(-1);
                }
                ComputeQuestions(word, correct_candidate, dic, vocabulary, remove1entry,
                        notclosedcats, d2s, plf_tmp, pw, scoredtreepw,
                        nonscoredtreepw, map_of_scored_candidates);
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
        pw.close();
        if(scoredtreepw!=null) {
            scoredtreepw.close();
        }
        if(nonscoredtreepw!=null) {
            nonscoredtreepw.close();
        }
    }
}
