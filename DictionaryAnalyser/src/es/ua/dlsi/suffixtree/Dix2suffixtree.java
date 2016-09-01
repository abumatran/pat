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

package es.ua.dlsi.suffixtree;

import dics.elements.dtd.*;
import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.lexicalinformation.ClosedCategories;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.EquivalentCandidates;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.paradigms.paradigmprofiling.DictionaryWord;
import es.ua.dlsi.paradigms.paradigmprofiling.ParadigmProfiler;
import es.ua.dlsi.querying.*;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import java.io.*;
import java.util.*;

/**
 * Class That implements the methods for building the suffix tree corresponding
 * to a given monolingual dictionary.
 * This class contains the methods for building a suffix tree for all the suffixes
 * in all the paradigms of a given Apertium monolingual dictionary.
 * @author Miquel Esplà Gomis
 */
public class Dix2suffixtree {

    /** Dictionary from which the suffix tree is built. */
    public Dictionary dic;

    /** Suffix tree object. */
    private SuffixTree st;

    /**
     * Overloaded constructor for the class using a Dictionary object.
     * This method builds the suffix tree from a Dictionary object.
     * @param dic Dictionary object from which the suffix tree is built.
     */
    public Dix2suffixtree(Dictionary dic) {
        this.dic = dic;
        st=new SuffixTree();
        for (Section section : dic.sections) {
            BuildSuffixTree(section.elements);
        }
    }

    /**
     * Overloaded constructor for the class using a Dictionary object.
     * This method builds the suffix tree from a Dictionary object.
     * @param fileName Dictionary object from which the suffix tree is built.
     */
    public Dix2suffixtree(String fileName) {
        DictionaryReader dicReader = new DictionaryReader(fileName);
        this.dic = dicReader.readDic();
        st=new SuffixTree();
        for (Section section : dic.sections) {
            BuildSuffixTree(section.elements);
        }
    }

    /**
     * Method that builds a suffix tree from the list of elements in a given
     * dictionary. Method that builds a suffix tree from the list of elements in
     * a given dictionary.
     * @param elements List of elements in a section of the dictionary from which
     * the suffix tree is built.
     */
    private void BuildSuffixTree(List<E> elements){
        for(E element: elements){
            if(!element.isMultiWord() && !ClosedCategories.isClosedCategory(element, dic)){
                Candidate c=DicEntry.GetStemParadigm(element);
                if(c!=null){
                    Paradigm par=new Paradigm(c.getParadigm(), dic);
                    if(!par.isMultiword()){
                        Set<Suffix> suffixes=par.getSuffixes();
                        if(suffixes.size()>0){
                            for(Suffix suffix: suffixes)
                                st.AddWord(suffix.getSuffix(), 0, c.getParadigm());
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that returns the sufix tree in the class.
     * @return Returns the <code>SuffixTree</code> stored in the class.
     */
    public SuffixTree getSuffixTree(){
        return st;
    }

    /**
     * Method that returns a list of ordered <code>Candidate</code> objects with
     * all the possible pairs stem/paradigm that could generate a given form.
     * This method uses the sufix tree computed in order to obtain all the possible
     * pairs stem/paradigm that could generate a given surface form. It uses a
     * set of criteria (aparition of inflected word forms in a corpus, number of
     * possible inflected word forms, etc.) to give a score to each candidate in
     * the list which is used to sort them from more likely to less likely.
     * @param word Surface form for which the candidates are checked
     * @param wordlist List of frequencies of words in a corpus. It is used to
     * compute the scores for the candidates.
     * @param stored_plf_prefix Path to a folder containing possibly pre-computed
     * <code>ParadigmProfiler</code> objects containing the scores for the
     * candidates (it can be set to <code>null</code>).
     * @param relation Information comming from a bilingual dictionary that 
     * indicates the possible corelation between a given paradigm in a language
     * and the paradigms in the current candidates (it can be set to 
     * <code>null</code>).
     * @param remove_closed_cats Flag to discard candidates belonging to closed
     * categories.
     * @return Returns a list of sorted candidates stem/paradigm for a given
     * surface form.
     */
    public SortedSetOfCandidates CheckNewWord(String word, Vocabulary wordlist,
            String stored_plf_prefix, Map<String,Double> relation,
            boolean remove_closed_cats) {
        SortedSetOfCandidates candidates=new SortedSetOfCandidates();
        if(st!=null){
            Set<Candidate> result=st.SegmentWord(word);
            
            for(Candidate c: result){
                if(!remove_closed_cats || !c.isClosedCategoryParadigm(dic)){
                    Paradigm par=new Paradigm(dic.pardefs.getParadigmDefinition(c.getParadigm()), dic);
                    if(par.getSuffixes().size()>0){
                        ParadigmProfiler plf=null;
                        if(stored_plf_prefix!=null){
                            ObjectInputStream plf_ois;
                            try{
                                plf_ois = new ObjectInputStream(new FileInputStream(stored_plf_prefix+par.getName().replace("/", ".")+".obj"));
                                Object obj = plf_ois.readObject();
                                if (obj instanceof ParadigmProfiler) {
                                    plf=(ParadigmProfiler)obj;
                                }
                                plf_ois.close();
                            }catch(Exception e){
                                //e.printStackTrace(System.err);
                                plf=new ParadigmProfiler(par,dic);
                                plf.BuildProfiles(wordlist);
                                ObjectOutputStream outputStream;
                                try {
                                    outputStream = new ObjectOutputStream(new FileOutputStream(stored_plf_prefix + par.getName().replace("/", ".") + ".obj"));
                                    outputStream.writeObject(plf);
                                    outputStream.close();
                                } catch (IOException ex) {
                                }
                            }
                        }
                        else{
                            plf=new ParadigmProfiler(par,dic);
                            plf.BuildProfiles(wordlist);
                        }
                        ScoredSurfaceFormsSet sfs=new ScoredSurfaceFormsSet(c.getStem(), par.getName(), dic, plf);
                        boolean added=false;

                        for(EquivalentCandidates qc: candidates.getCandidates()){
                            if(qc.getSurfaceForms(dic).equals(sfs.getSurfaceForms())){
                                qc.addCandidate(c);
                                added=true;
                                break;
                            }
                        }
                        if(!added){
                            DictionaryWord lf=new DictionaryWord(c.getStem());
                            lf.setProfile(par, wordlist);
                            plf.AddWord(lf);
                            double correl;
                            if(relation==null) {
                                correl=1;
                            }
                            else{
                                if(relation.containsKey(c.getParadigm())) {
                                    correl=relation.get(c.getParadigm());
                                }
                                else {
                                    correl=1.0/(double)(relation.size()*10);
                                }
                            }
                            //System.out.println(plf.getPercentil(lf)+"\t"+plf.getSurfaceFormAparition(lf, 0.1)+"\t"+paradigm.getFirst()+"\t"+paradigm.getSecond());
                            candidates.addCandidate(c,plf.getSurfaceFormAparition(lf, 0.1)*correl, sfs);
                        }
                    }
                }
            }
        }
        return candidates;
    }
    
    /**
     * Method that returns a list of ordered <code>Candidate</code> objects with
     * all the possible pairs stem/paradigm that could generate a given form.
     * This method uses the sufix tree computed in order to obtain all the possible
     * pairs stem/paradigm that could generate a given surface form. It uses a
     * set of criteria (aparition of inflected word forms in a corpus, number of
     * possible inflected word forms, etc.) to give a score to each candidate in
     * the list which is used to sort them from more likely to less likely.
     * @param string Surface form for which the candidates are checked
     * @param wordlist List of frequencies of words in a corpus. It is used to
     * compute the scores for the candidates.
     * @param stored_plf_prefix Path to a folder containing possibly pre-computed
     * <code>ParadigmProfiler</code> objects containing the scores for the
     * candidates (it can be set to <code>null</code>).
     * @param relation Information comming from a bilingual dictionary that 
     * indicates the possible corelation between a given lexical category in a
     * language and the categories of the paradigms in the current candidates (it
     * can be set to <code>null</code>).
     * @param remove_closed_cats Flag to discard candidates belonging to closed
     * categories.
     * @return Returns a list of sorted candidates stem/paradigm for a given
     * surface form.
     */
    public SortedSetOfCandidates CheckNewWordCatLex(String string, Vocabulary wordlist,
            String stored_plf_prefix, Map<String,Double> relation, boolean remove_closed_cats) {
        SortedSetOfCandidates candidates=new SortedSetOfCandidates();
        if(st!=null){
            Set<Candidate> result=st.SegmentWord(string);
            for(Candidate c: result){
                if(!remove_closed_cats || ! c.isClosedCategoryParadigm(dic)){
                    Paradigm par=new Paradigm(dic.pardefs.getParadigmDefinition(c.getParadigm()), dic);
                    if(par.getSuffixes().size()>0){
                        ParadigmProfiler plf=null;
                        boolean added=false;
                        Set<String> surfaceforms=c.GetSurfaceForms(dic);
                        for(EquivalentCandidates qc: candidates.getCandidates()){
                            if(qc.getSurfaceForms(dic).equals(surfaceforms)){
                                qc.addCandidate(c);
                                added=true;
                            }
                        }
                        if(!added){
                            if(stored_plf_prefix!=null){
                                ObjectInputStream plf_ois;
                                try{
                                    plf_ois = new ObjectInputStream(new FileInputStream(stored_plf_prefix+par.getName().replace("/", ".")+".obj"));
                                    Object obj = plf_ois.readObject();
                                    if (obj instanceof ParadigmProfiler) {
                                        plf=(ParadigmProfiler)obj;
                                    }
                                    plf_ois.close();
                                }catch(Exception e){
                                    //e.printStackTrace(System.err);
                                    plf=new ParadigmProfiler(par,dic);
                                    plf.BuildProfiles(wordlist);
                                    ObjectOutputStream outputStream;
                                    try {
                                        outputStream = new ObjectOutputStream(new FileOutputStream(stored_plf_prefix + par.getName().replace("/", ".") + ".obj"));
                                        outputStream.writeObject(plf);
                                        outputStream.close();
                                    } catch (IOException ex) {
                                    }
                                }
                            }
                            else{
                                plf=new ParadigmProfiler(par,dic);
                                plf.BuildProfiles(wordlist);
                            }
                            DictionaryWord lf=new DictionaryWord(c.getStem());
                            lf.setProfile(par, wordlist);
                            plf.AddWord(lf);
                            double correl;
                            if(relation==null) {
                                correl=1;
                            }
                            else{
                                if(relation.containsKey(par.getSuffixes().iterator().next().getLexInfo().get(0))) {
                                    correl=relation.get(par.getSuffixes().iterator().next().getLexInfo().get(0));
                                }
                                else {
                                    correl=1.0/(double)(relation.size()*10);
                                }
                            }
                            ScoredSurfaceFormsSet sfs=new ScoredSurfaceFormsSet(c.getStem(), par.getName(), dic, plf);
                            //System.out.println(plf.getPercentil(lf)+"\t"+plf.getSurfaceFormAparition(lf, 0.1)+"\t"+paradigm.getFirst()+"\t"+paradigm.getSecond());
                            candidates.addCandidate(c,plf.getSurfaceFormAparition(lf, 0.1)*correl, sfs);
                        }
                    }
                }
            }
        }
        return candidates;
    }
}
