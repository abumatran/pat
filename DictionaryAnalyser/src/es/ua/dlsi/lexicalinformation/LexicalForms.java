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

package es.ua.dlsi.lexicalinformation;

import dics.elements.dtd.*;
import dics.elements.dtd.Dictionary;
import es.ua.dlsi.utils.Pair;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class that represents the lexical forms in an Apertium monolingual dictionary.
 * This class represents the collection of lexical forms in an Apertium monolingual
 * dictionary. A lexical form in Apertium is a the lemma of a word and a set of
 * lexical information such as gender, nubmer, lexical cathegory, etc.
 * @author Miquel Esplà i Gomis
 */
public class LexicalForms {

    /**
     * Class that represents the lexical information of a given lexical form.
     * This class contains the lexical information of a given lexical form, and
     * some additional information, namely, the name of the paradigm (if defined)
     * and the <code>E</code> entry from the dictionary.
     */
    public class LexicalInfo{
        public List<String> lexicaltags;
        public String paradigm;
        public E entry;

        public LexicalInfo(List<String> lexicaltags, String paradigm, E entry){
            this.lexicaltags=lexicaltags;
            this.paradigm=paradigm;
            this.entry=entry;
        }
    }
    
    /**
     * Map with the a set of lexical forms in a dictionary.
     * This map has, as the key, a stem, and the value contains the list of all
     * the possible collections of lexical information combinations for the
     * corresponding entries in the dictionary.
     */
    Map<String,List<LexicalInfo>> lexforms;

    /**
     * Constructor of the class.
     * This constructor takes a monolingual dictionary and builds the map of
     * lexical forms for the class.
     * @param dic Monoliingual dictionary from which the lexical forms will be
     * obtained.
     */
    public LexicalForms(Dictionary dic){
        lexforms=new LinkedHashMap<String, List<LexicalInfo>>();
        for(Section sec: dic.sections){
            for(E e: sec.elements){
                if(!e.isMultiWord()){
                    String stem="";
                    for (DixElement de: e.children){
                        if(de instanceof I){
                            if(!((I)de).getValueNoTags().equals("")) {
                                stem=((I)de).getValueNoTags();
                            }
                        }
                        else if(de instanceof P)
                        {
                            if(!((P)de).r.getValueNoTags().equals("")) {
                                stem=((P)de).r.getValueNoTags();
                            }
                            List<String> lstmp=new LinkedList<String>();
                            boolean anyfound=false;
                            for(DixElement lexinfode: ((P)de).r.children){
                                if(lexinfode instanceof S){
                                    lstmp.add(((S)lexinfode).name);
                                    anyfound=true;
                                }
                            }
                            if(anyfound){
                                if(lexforms.containsKey(stem)) {
                                    lexforms.get(stem).add(new LexicalInfo(lstmp, "", e));
                                }
                                else{
                                    List<LexicalInfo> l=new LinkedList<LexicalInfo>();
                                    l.add(new LexicalInfo(lstmp,"", e));
                                    lexforms.put(stem, l);
                                }
                            }
                        }
                        else if(de instanceof Par)
                        {
                            List<E> parElements=dic.pardefs.getParadigmDefinition(((Par)de).name).elements;
                            if(parElements.size()>0){
                                for(E pare: parElements){
                                    for(DixElement parde: pare.children){
                                        if(parde instanceof P){
                                            StringBuilder sb=new StringBuilder(stem);
                                            if(!((P)parde).r.getValueNoTags().equals("")) {
                                                sb.append(((P)parde).r.getValueNoTags());
                                            }
                                            List<String> lstmp=new LinkedList<String>();
                                            for(DixElement lexinfode: ((P)parde).r.children){
                                                if(lexinfode instanceof S){
                                                    lstmp.add(((S)lexinfode).name);
                                                }
                                            }
                                            if(lexforms.containsKey(sb.toString())) {
                                                lexforms.get(sb.toString()).add(new LexicalInfo(lstmp,((Par)de).name, e));
                                            }
                                            else{
                                                List<LexicalInfo> l=new LinkedList<LexicalInfo>();
                                                l.add(new LexicalInfo(lstmp,((Par)de).name, e));
                                                lexforms.put(sb.toString(), l);
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
    }
    
    
    /**
     * Method that extracts the set of lexical tags and returns them in a list.
     * This method extracts, from a list of an item <code>I</code> from a dictionary
     * a list of lexical information tags and returns them as a linked list. If
     * it is specified that the item should not belong to a closed category and
     * it is so, the class returns a null pointer.
     * @param dixelement element from the dictionary from which the lexical information is exctracted
     * @param onlyifnotclosed flag that makes the method to check whether the class is closed or not
     * @return returns the list of lexical cathegories
     */
    public static List<String> GetLexicalInfo(DixElement dixelement, boolean onlyifnotclosed){
        ArrayList<DixElement> children;
        if(dixelement instanceof I) {
            children=((I)dixelement).children;
        }
        else if(dixelement instanceof L) {
            children=((L)dixelement).children;
        }
        else{
            try{
                children=((R)dixelement).children;
            } catch(ClassCastException ex){
                System.err.print("Wrong class "+dixelement.getClass().getName()+": it should be a I, L, or R object");
                ex.printStackTrace(System.err);
                children=null;
                System.exit(-1);
            }
        }
        List<String> lstmp=new LinkedList<String>();
        for(DixElement lexinfode: children){
            if(lexinfode instanceof S){
                String symbol=((S)lexinfode).name;
                if(onlyifnotclosed && ClosedCategories.isClosedCat(symbol)){
                    return null;
                }
                lstmp.add(symbol);
            }
        }
        return lstmp;
    }
    
    /**
     * Method that obtains name of the paradigm to which a lexical form belongs
     * from a set of candidates.
     * This method returns the paradigm to which a given lexical form belongs 
     * from a set of candidates.
     * @param stem Stem of the lexical form.
     * @param lexinfo Lexical information of the lexical form.
     * @param possibleparadigms Set of candidate paradigms.
     * @return Returns the winner paradigm.
     */
    public String GetParadigmFromLexForm(String stem,List<String> lexinfo,
            Set<String> possibleparadigms){
        List<LexicalInfo> candidates=lexforms.get(stem);
        if(lexinfo==null){
            System.err.println("Error: lexical information cannot be null");
        }
        if(candidates!=null) {
            for(LexicalInfo p: candidates){
                if(possibleparadigms.contains(p.paradigm)){
                    int i;
                    if(lexinfo.size()>0){
                        for(i=0;i<lexinfo.size();i++){
                            if(p.lexicaltags.size()>=lexinfo.size()){
                                if(!lexinfo.get(i).equals("ND") &&
                                        !lexinfo.get(i).equals("GD") &&
                                        !lexinfo.get(i).equals(p.lexicaltags.get(i))) {
                                    break;
                                }
                            }
                        }
                        if(i==lexinfo.size()) {
                            return p.paradigm;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Method that obtains the name of the first paradigm matching with a given
     * lexical form.
     * This method returns the name of the first paradigm in the dictionary that
     * is able to generate a given lexical form.
     * @param stem Stem of the lexical form.
     * @param lexinfo Lexical information of the lexical form.
     * @return Returns the name of the first paradigm generating the lexical form.
     */
    public String GetParadigmFromLexForm(String stem,List<String> lexinfo){
        List<LexicalInfo> candidates=lexforms.get(stem);
        if(lexinfo==null){
            System.err.println("Error: lexical information cannot be null");
        }
        if(candidates!=null) {
            for(LexicalInfo p: candidates){
                int i;
                if(lexinfo.size()>0){
                    for(i=0;i<lexinfo.size();i++){
                        if(p.lexicaltags.size()>=lexinfo.size()){
                            if(!lexinfo.get(i).equals("ND") &&
                                    !lexinfo.get(i).equals("GD") &&
                                    !lexinfo.get(i).equals(p.lexicaltags.get(i))) {
                                break;
                            }
                        }
                    }
                    if(i==lexinfo.size()) {
                        return p.paradigm;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Method that returns the set of paradigms and entries for a given lexical
     * form.
     * @param stem Stem of the lexical form.
     * @param lexinfo Lexical information of the lexical form.
     * @return Returns a set of pairs name of the paradigm/xml entry
     */
    public Set<Pair<String,E>> GetParsAndEntriesFromLexForm(String stem,List<String> lexinfo){
        Set<Pair<String,E>> result=new LinkedHashSet<Pair<String,E>>();
        List<LexicalInfo> candidates=lexforms.get(stem);
        if(lexinfo==null){
            System.err.println("Error: lexical information cannot be null");
        }
        if(candidates!=null){
            for(LexicalInfo p: candidates){
                int i;
                if(lexinfo.size()>0){
                    for(i=0;i<lexinfo.size();i++){
                        if(p.lexicaltags.size()>=lexinfo.size()){
                            if(!lexinfo.get(i).equals("ND") &&
                                    !lexinfo.get(i).equals("GD") &&
                                    !lexinfo.get(i).equals(p.lexicaltags.get(i))) {
                                break;
                            }
                        }
                    }
                    if(i==lexinfo.size() && !p.paradigm.equals("")) {
                        result.add(new Pair<String, E>(p.paradigm,p.entry));
                    }
                }
            }
        }
        if(result.isEmpty()) {
            return null;
        }
        else {
            return result;
        }
    }

    /**
     * Method that obtains the name of all the paradigms matching with a given
     * lexical form.
     * This method returns the name of all the paradigms in the dictionary that
     * are able to generate a given lexical form.
     * @param stem Stem of the lexical form.
     * @param lexinfo Lexical information of the lexical form.
     * @return Returns the name of all the paradigms generating the lexical form.
     */
    public Set<String> GetParadigmsFromLexForm(String stem,List<String> lexinfo){
        Set<String> result=new LinkedHashSet<String>();
        List<LexicalInfo> candidates=lexforms.get(stem);
        if(lexinfo==null){
            System.err.println("Error: lexical information cannot be null");
        }
        if(candidates!=null){
            for(LexicalInfo p: candidates){
                int i;
                if(lexinfo.size()>0){
                    if(p.lexicaltags.size()>=lexinfo.size()){
                        for(i=0;i<lexinfo.size();i++){
                                if(!lexinfo.get(i).equals("ND") &&
                                        !lexinfo.get(i).equals("GD") &&
                                        !lexinfo.get(i).equals(p.lexicaltags.get(i))) {
                                break;
                            }
                        }
                        if(i==lexinfo.size() && !p.paradigm.equals("")) {
                            result.add(p.paradigm);
                        }
                    }
                }
            }
        }
        if(result.isEmpty()) {
            return null;
        }
        else {
            return result;
        }
    }

    /**
     * Method that prints the collection of lexical forms.
     */
    public void Print(){
        for(Entry<String,List<LexicalInfo>> e: lexforms.entrySet()){
            for(LexicalInfo p: e.getValue()){
                System.out.print(e.getKey());
                System.out.print(" ");
                System.out.print(p.paradigm);
                System.out.print(": ");
                for(String s: p.lexicaltags){
                    System.out.print(s);
                    System.out.print(", ");
                }
                System.out.println();
            }
        }
    }
}
