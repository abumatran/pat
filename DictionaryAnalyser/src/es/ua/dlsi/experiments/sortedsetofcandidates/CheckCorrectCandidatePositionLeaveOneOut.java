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

package es.ua.dlsi.experiments.sortedsetofcandidates;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.E;
import dics.elements.dtd.Pardef;
import dics.elements.dtd.Section;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.paradigms.paradigmprofiling.ParadigmProfiler;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.NotInListException;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class CheckCorrectCandidatePositionLeaveOneOut {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        //Monolingual dictionary from which the paradigms and words will be read
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        //If this flag is enabled, the paradigms with an only entry are discarded
        CmdLineParser.Option oremove1entry = parser.addBooleanOption("remove-1entrypars");
        //Path to the output file
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        //If this flag is enabled, the closed categories are not taken into account for the experiment
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        //Vocabulary containing the list of words and the number of occurencies in the monolingual dictionary
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        //Path to the directory containing the profile objects
        CmdLineParser.Option opathjavaobjects = parser.addStringOption('p',"path-objects");

        //Reading the parameters
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
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String pathjavaobjects=(String)parser.getOptionValue(opathjavaobjects,null);
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

        //Loop that goes all over the entries of the dictionary
        for(Section s: dic.sections){
            for(int i=0;i<s.elements.size();i++){
                E e=s.elements.remove(i);
                //If the entry is a multiword is discarded
                if(e.isMultiWord()){
                    System.err.println("Multiword: "+e.toString());
                }
                else{
                    //Getting the stema and paradign of the entry
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    ParadigmProfiler pp=new ParadigmProfiler(new Paradigm(
                            dic.pardefs.getParadigmDefinition(candidate.getParadigm()), dic),dic);
                    if(!remove1entry || pp.NumberOfWords()>1){
                        if(candidate.getStem()!=null && candidate.getParadigm()!=null){
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
                                SortedSetOfCandidates candidates=d2s.CheckNewWord(
                                        bestsurfaceform, vocabulary, pathjavaobjects,
                                        null, notclosedcats);
                                int numberofquestions=0;
                                Set<String> surfaceforms=candidate.GetSurfaceForms(dic);
                                String formtoask;
                                try{
                                    int pos=candidates.GetCandidatePosition(candidate);
                                    while((formtoask=candidates.getNextSurfaceFormToAsk())!=null){
                                        if(surfaceforms.contains(formtoask)) {
                                            candidates.AcceptForm(formtoask);
                                        }
                                        else {
                                            candidates.RejectForm(formtoask);
                                        }
                                        numberofquestions++;
                                    }

                                    //Printing the output
                                    pw.println(stem+";"+paradigm.getName()+";"+pos+";"+numberofquestions);
                                    pw.flush();
                                    s.elements.add(i, e);
                                }catch(NotInListException ex){
                                    System.err.println("Candidate "+candidate.toString()+" is not in the list of candidates.");
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
            }
        }
        pw.close();
    }
}
