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

package es.ua.dlsi.experiments.suffixtree;

import dics.elements.dtd.*;
import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.bilingual.LexicalCategoryRelationship;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.NotInListException;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 * @author miquel
 */
public class CheckCorrectCandidatePositionLeaveOneOutLexCat {
    
    public static String GetCounterParadigmFromLexForm(String lemma,
            List<String> lexinfo, Dictionary biling_dic, boolean reverse){
        for(Section sec : biling_dic.sections) {
            for (E e : sec.elements) {
                if(!e.toString().contains("<g>") && !e.toString().contains("<b>")){
                    for(DixElement de: e.children){
                        if(de instanceof I){
                            I i=(I)de;
                            String newstem=i.getValueNoTags();
                            if(newstem.equals(lemma)){
                                List<String> lstmp=new LinkedList<String>();
                                boolean possible=true;
                                int counter=0;
                                for(DixElement lexinfode: i.children){
                                    if(lexinfode instanceof S){
                                        String symbol=((S)lexinfode).name;
                                        if(!lexinfo.get(counter).equals(symbol)){
                                            possible=false;
                                            break;
                                        }
                                        counter++;
                                    }
                                }
                                if(possible){
                                    return lexinfo.get(0);
                                }
                            }
                        } else if(de instanceof P){
                            if(reverse){
                                String newstem=((P)de).l.getValueNoTags();
                                if(newstem.equals(lemma)){
                                    List<String> lstmp=new LinkedList<String>();
                                    boolean possible=true;
                                    int counter=0;
                                    for(DixElement lexinfode: ((P)de).l.children){
                                        if(lexinfode instanceof S){
                                            String symbol=((S)lexinfode).name;
                                            if(!lexinfo.get(counter).equals(symbol)){
                                                possible=false;
                                                break;
                                            }
                                            counter++;
                                        }
                                    }
                                    if(possible){
                                        for(DixElement lexinfode: ((P)de).r.children){
                                            if(lexinfode instanceof S){
                                                String symbol=((S)lexinfode).name;
                                                return symbol;
                                            }
                                        }
                                    }
                                }
                            }
                            else{
                                String newstem=((P)de).r.getValueNoTags();
                                if(newstem.equals(lemma)){
                                    List<String> lstmp=new LinkedList<String>();
                                    boolean possible=true;
                                    int counter=0;
                                    for(DixElement lexinfode: ((P)de).r.children){
                                        if(lexinfode instanceof S){
                                            String symbol=((S)lexinfode).name;
                                            if(!lexinfo.get(counter).equals(symbol)){
                                                possible=false;
                                                break;
                                            }
                                            counter++;
                                        }
                                    }
                                    if(possible){
                                        for(DixElement lexinfode: ((P)de).l.children){
                                            if(lexinfode instanceof S){
                                                String symbol=((S)lexinfode).name;
                                                return symbol;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option oleftdic = parser.addStringOption('l',"right-dic");
        CmdLineParser.Option orightdic = parser.addStringOption('r',"left-dic");
        CmdLineParser.Option obilingdic = parser.addStringOption('b',"biling-dic");
        CmdLineParser.Option oremove1entry = parser.addBooleanOption("remove-1entrypars");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option oreverse = parser.addBooleanOption("reverse");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        CmdLineParser.Option opathjavaobjects = parser.addStringOption('p',"path-objects");

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

        String rightdic=(String)parser.getOptionValue(orightdic,null);
        String leftdic=(String)parser.getOptionValue(oleftdic,null);
        String bilingdic=(String)parser.getOptionValue(obilingdic,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String pathjavaobjects=(String)parser.getOptionValue(opathjavaobjects,null);
        boolean remove1entry=(Boolean)parser.getOptionValue(oremove1entry,false);
        boolean reverse=(Boolean)parser.getOptionValue(oreverse,false);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);

        if(rightdic==null){
            System.err.println("Error: right dictionary must be defined by using the parameter -r");
            System.exit(-1);
        }
        
        if(leftdic==null){
            System.err.println("Error: left dictionary must be defined by using the parameter -l");
            System.exit(-1);
        }
        
        if(bilingdic==null){
            System.err.println("Error: bilingual dictionary must be defined by using the parameter -b");
            System.exit(-1);
        }
        
        if(vocabularypath==null){
            System.err.println("Error: path to the vocabulary file must be defined by using the parameter -v");
            System.exit(-1);
        }
        
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

        DictionaryReader dicReader = new DictionaryReader(leftdic);
        Dictionary dic_left=dicReader.readDic();
        dicReader = new DictionaryReader(rightdic);
        Dictionary dic_right=dicReader.readDic();
        Dictionary biling_dic;
        dicReader = new DictionaryReader(bilingdic);
        biling_dic = dicReader.readDic();
        Dix2suffixtree d2s;
        Dictionary dic;
        if(reverse){
            dic=dic_left;
        }
        else{
            dic=dic_right;
        }
        d2s=new Dix2suffixtree(dic);
        if(d2s==null){
            System.err.println("There was a problem when creating the suffix tree. Check the dictionaries provided.");
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
                    if(candidate.getStem()!=null && candidate.getParadigm()!=null){
                        
                        String stem=candidate.getStem();
                        String bestsurfaceform;
                        List<String> bestlexinfo;
                        Pardef p= dic.pardefs.getParadigmDefinition(candidate.getParadigm());
                        Paradigm paradigm=new Paradigm(p, dic);
                        //If indicated, entries generating forms from a closed category may be discarded
                        if(!notclosedcats || !paradigm.isClosedCategory()){
                            //Choosing the most frequent surface form in the vocabulary
                            bestsurfaceform=vocabulary.GetMostFrequentSurfaceForm(stem, paradigm);
                            //The lexical information of one of the suffixes generating the surface form is taken randomly
                            //If no one of the surface forms appear in the vocabulary:
                            if(bestsurfaceform==null){
                                System.err.println("Warning: no occurrence for word with stem "
                                        +stem+" and paradigm "+paradigm.getName());
                                //Random form
                                bestsurfaceform=stem+paradigm.getSuffixes().iterator().next().getSuffix();
                            }
                            bestlexinfo=candidate.GetExpansion(dic).get(bestsurfaceform).iterator().next().getLexInfo();
                            
                            String lemma=candidate.GetLemma(dic);
                            //If the lemma cannot be found, the system stops working
                            if(candidate.GetLemma(dic) ==null){
                                System.err.println("Error: lemma cannot be generated for stem "+stem+
                                        " and paradigm "+paradigm.getName());
                                System.exit(-1);
                            }
                            String counterpar;
                            if(reverse) {
                                counterpar=GetCounterParadigmFromLexForm(lemma,
                                        bestlexinfo, biling_dic, reverse);
                            }
                            else {
                                counterpar=GetCounterParadigmFromLexForm(lemma,
                                        bestlexinfo, biling_dic, reverse);
                            }

                            LexicalCategoryRelationship normalizedcorrelation=
                                    new LexicalCategoryRelationship(biling_dic,
                                    reverse, remove1entry, notclosedcats);
                            SortedSetOfCandidates candidates=d2s.CheckNewWordCatLex(
                                    bestsurfaceform, vocabulary, pathjavaobjects,
                                    normalizedcorrelation.get(counterpar), notclosedcats);
                            SortedSetOfCandidates candidates2=d2s.CheckNewWordCatLex(
                                    bestsurfaceform, vocabulary, pathjavaobjects,
                                    null, notclosedcats);
                            System.out.println(candidates.getCandidates().size());
                            System.out.println(candidates2.getCandidates().size());
                            try{
                                int nobilingpos=candidates2.GetCandidatePosition(candidate);
                                int bilingpos=candidates.GetCandidatePosition(candidate);
                            
                                pw.println(stem+";"+paradigm.getName()+";"+nobilingpos+";"+bilingpos);
                                s.elements.add(i, e);
                            }catch(NotInListException ex){
                                System.err.println("Warning: candidate "+candidate.toString()+" is not in the sorted list of candidates.");
                            }
                        }
                    }
                    else{
                        System.err.println("Closed category: "+e.toString());
                    }
                }
            }
        }
        pw.close();
    }
}
