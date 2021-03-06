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

package es.ua.dlsi.monolingual;

import dics.elements.dtd.*;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.lexicalinformation.ClosedCategories;
import es.ua.dlsi.utils.Pair;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class which represents a paradigm from a dictionary of Apertium
 * @author Miquel Esplà i Gomis
 */
public class Paradigm implements Serializable{
    /** Name of the paradigm */
    private String name;
    
    /** Suffix that is added to the lemma to produce the surface form */
    private String lemma_suffix;

    /** List of suffixes generated by the paradigm */
    private Set<Suffix> suffixes;
    
    private boolean multiword;
    
    /**
     * Overloaded constructor of the class
     * @param par Pardef object from which the paradigm is read
     * @param dic Dictionary from which the paradigm is read
     */
    public Paradigm(String parname, Dictionary dic){
        this(dic.pardefs.getParadigmDefinition(parname),dic);
    }

    /**
     * Overloaded constructor of the class
     * @param par Pardef object from which the paradigm is read
     * @param dic Dictionary from which the paradigm is read
     */
    public Paradigm(Pardef par, Dictionary dic){
        this.name=par.name;
        this.multiword=false;
        suffixes=new LinkedHashSet<Suffix>();
        
        Set<Pair<StringBuilder,List<String>>> lstmp=
                new LinkedHashSet<Pair<StringBuilder,List<String>>>();
        
        lstmp.add(new Pair<StringBuilder,List<String>>(new StringBuilder(),new LinkedList<String>()));
        
        Set<Pair<String,List<String>>> sufftmp=BuildSuffixes(par.elements,lstmp,dic);
        
        for(Pair<String,List<String>> s: sufftmp){
            Suffix suf=new Suffix(s.getFirst(),s.getSecond());
            if(suf.getSuffix().contains(" "))
                this.multiword=true;
            this.suffixes.add(suf);
        }
        
        for(DixElement parchild: dic.pardefs.getParadigmDefinition(
                name).elements.get(0).children){
            if(parchild instanceof P){
                lemma_suffix=((P)parchild).r.getValueNoTags();
                break;
            }
        }
    }

    /**
     * Method that generates all the possible suffixes
     * @param elements List of elements from which the suffixes are read
     * @param currentLexicalForms List of paradigms pre-generated
     * @param dic Dictionary from which the paradigm is read
     * @return Returns the list of suffixes generated by the paradigm
     */
    private Set<Pair<String,List<String>>> BuildSuffixes(List<E> elements,
            Set<Pair<StringBuilder,List<String>>> currentLexicalForms, Dictionary dic)
    {
        //
         List<Pair<String,List<String>>> localList=new LinkedList<Pair<String,List<String>>>();
         for(E element: elements)
         {
             Set<Pair<StringBuilder,List<String>>> listgeneratedByElement=
                     new LinkedHashSet<Pair<StringBuilder,List<String>>>();
             for (DixElement e: element.children)
             {
                 if (e instanceof P){
                     if(listgeneratedByElement.isEmpty()) {
                         listgeneratedByElement.add(new Pair(new StringBuilder(""),new LinkedList<String>()));
                     }
                     for(Pair<StringBuilder,List<String>> b: listgeneratedByElement){
                        b.getFirst().append(((P)e).l.getValueNoTags());
                        for(DixElement subde: ((P)e).r.children){
                            if(subde instanceof S) {
                                b.getSecond().add(((S)subde).name);
                            }
                        }
                     }
                 }
                 else if (e instanceof Par)
                 {
                     List<E> parElements=dic.pardefs.getParadigmDefinition(((Par)e).name).elements;
                     Set<Pair<String,List<String>>> resultList=BuildSuffixes(parElements,
                             listgeneratedByElement, dic);
                     listgeneratedByElement.clear();
                     for(Pair<String,List<String>> r: resultList) {
                         listgeneratedByElement.add(new Pair(new StringBuilder(r.getFirst()),
                         r.getSecond()));
                     }
                 }
             }
             for(Pair<StringBuilder,List<String>> b: listgeneratedByElement) {
                 localList.add(new Pair(b.getFirst().toString(),b.getSecond()));
             }
         }

         //Combine lists
         Set<Pair<String,List<String>>> finalList = new LinkedHashSet<Pair<String,List<String>>>();
         for(Pair<StringBuilder,List<String>> lexHead: currentLexicalForms) {
            for(Pair<String,List<String>> lexTail: localList){
                List<String> newlist=new LinkedList<String>(lexHead.getSecond());
                newlist.addAll(lexTail.getSecond());
                finalList.add(new Pair<String,List<String>>(lexHead.getFirst()+
                        lexTail.getFirst(),newlist));
            }
        }

         return finalList;
    }

    /**
     * Method that returns the name of the paradigm
     * @return Returns the name of the paradigm
     */
    public String getName() {
        return name;
    }

    /**
     * Method that returns the list of suffixes generated by the paradigm
     * @return Returns the list of suffixes generated by the paradigm
     */
    public Set<Suffix> getSuffixes(){
        return this.suffixes;
    }
    
    /**
     * Method that indicates if the paradigm belongs to a closed category.
     * @return Returns <code>true</code> if the paradigm belongs to a closed category and <code>false</code> otherwise.
     */
    public boolean isClosedCategory(){
        if(this.suffixes.isEmpty()) {
            return false;
        }
        else{
            List<String> lexical_info=this.suffixes.iterator().next().getLexInfo();
            if(lexical_info.isEmpty()) {
                return false;
            }
            else{
                String lexical_category=lexical_info.get(0);
                return ClosedCategories.isClosedCat(lexical_category);
            }
        }
    }
    
    /**
     * Method that generates the set of surface forms from combining the suffixes
     * of the paradigm with a stem.
     * Method that generates the set of surface forms from combining the suffixes
     * of the paradigm with a stem.
     * @param stem Stem to be combined with the paradigm.
     * @return Returns a set of surface forms in form of Strings.
     */
    public Set<String> GetSurfaceFroms(String stem){
        Set<String> sfset=new LinkedHashSet<String>();
        
        for(Suffix suf: suffixes){
            StringBuilder sb=new StringBuilder(stem);
            sb.append(suf.getSuffix());
            sfset.add(sb.toString());
        }
        return sfset;
    }
    
    /**
     * Method that returns the suffix that is added to a stem to generate the lemma.
     * @return Returns the suffix for generating the lemma of a candidate
     */
    public String GetLemmaSuffix(){
        return this.lemma_suffix;
    }
    
    /**
     * Method that returns the suffix that is added to a stem to generate the lemma.
     * @return Returns the suffix for generating the lemma of a candidate
     */
    public int GetNumberOfEntries(Dictionary dic, boolean accept_multiword){
        int number=0;
        for(Section sec: dic.sections){
            for(E e: sec.elements){
                if(!e.isMultiWord() || accept_multiword){
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    if(candidate!=null){
                        if(this.name.equals(candidate.getParadigm())){
                            number++;
                        }
                    }
                }
            }
        }
        return number;
    }
    
    /**
     * Method that returns the entries related to a given paradigm.
     * @param dic Dictionary to be used
     * @param accept_multiword Do we accept multiword entries?
     * @return Returns the entries related to a given paradigm
     */
    public Set<Candidate> GetRelatedEntries(Dictionary dic, boolean accept_multiword){
        Set<Candidate> candidates=new HashSet<Candidate>();
        for(Section sec: dic.sections){
            for(E e: sec.elements){
                if(!e.isMultiWord() || accept_multiword){
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    if(candidate!=null){
                        if(this.name.equals(candidate.getParadigm())){
                            candidates.add(candidate);
                        }
                    }
                }
            }
        }
        return candidates;
    }
    
    /**
     * Method that returns the value of the flag "multiword"
     * @return Returns <code>true</code> if the paradigm produces at lest one
     * multiword suffix, and <code>false</code> otherwise.
     */
    public boolean isMultiword(){
        return this.multiword;
    }
}
