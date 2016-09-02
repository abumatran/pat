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

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.DixElement;
import dics.elements.dtd.P;
import dics.elements.dtd.Pardef;
import es.ua.dlsi.lexicalinformation.ClosedCategories;
import es.ua.dlsi.suffixtree.Node;
import java.io.Serializable;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Class that represents a candidate to be a pair stem/suffix-set.
 * Class that candidate stem/suffix-set which could be a part of the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class Candidate implements Serializable, Comparable<Object> {
    /**Stem of the lexical form*/
    private String stem;
    
    /**Paradigm to which the entry belongs*/
    private String paradigm;
    
    /**Expansion of the combination of the stem and the paradigm*/
    private Map<String,Set<Suffix>> expansion;
    
    /**Lemma of the candidate**/
    private String lemma;
    
    /**Lemma of the candidate**/
    private Node reftotree;
    
    /**
     * Overloaded constructor of the class.
     * @param stem Stem to add to the new object.
     * @param paradigm Paradigm to add to the new object.
     */
    public Candidate(String stem, String paradigm){
        this.stem=stem;
        this.paradigm=paradigm;
        this.lemma=null;
        this.expansion=null;
    }

    /**
     * Method that returns the name of the paradigm.
     * @return Returns the name of the paradigm
     */
    public String getParadigm() {
        return paradigm;
    }

    /**
     * Method that sets the name of the paradigm.
     * @param paradigm Name of the paradigm to be set.
     */
    public void setParadigm(String paradigm) {
        this.paradigm = paradigm;
    }

    /**
     * Method that returns the stem of the candidate.
     * @return Returns the stem of the candidate
     */
    public String getStem() {
        return stem;
    }

    /**
     * Method that sets the stem of the candidate.
     * @param stem Stem of the candidate to be set.
     */
    public void setStem(String stem) {
        this.stem = stem;
    }

    /**
     * Method that returns the collection of suffixes from the paradigm.
     * @param dic Dictionary from which the suffixes will be read
     * @return Returns the collection of suffixes from the paradigm
     */
    public Set<Suffix> getSuffixes(Dictionary dic) {
        Collection<Set<Suffix>> s=GetExpansion(dic).values();
        Set<Suffix> exit=new LinkedHashSet<Suffix>();
        for(Set<Suffix> suffixes: s){
            exit.addAll(suffixes);
        }
        return exit;
    }
    
    /**
     * Method that returns the list of surface froms obtained when expanding the
     * stem/suffix-set.
     * @param dic Dictionary from which the paradigm will be read to produce the surface forms.
     * @return Returns the surface forms produced by the combination of the stem and the paradigm.
     */
    public Set<String> GetSurfaceForms(Dictionary dic){
        return GetExpansion(dic).keySet();
    }
    
    /**
     * Method that indicates if the candidate belongs to a closed cathegory.
     * @param dic Dictionary from which the lexical information of the candidate will be read.
     * @return Returns <code>true</code> if the candidate belongs to a closed cathegory and <code>false</code> otherwhise.
     */
    public boolean isClosedCategoryParadigm(Dictionary dic){
        Pardef p=dic.pardefs.getParadigmDefinition(this.paradigm);
        if(p==null) {
            return false;
        }
        else {
            return ClosedCategories.isClosedCategoryParadigm(
                    dic.pardefs.getParadigmDefinition(this.paradigm));
        }
    }

    /**
     * Method that compares this object to another one and returns <code>true</code> if they are equal.
     * @param obj Object with which this object will be compared.
     * @return Returns <code>true</code> if they are equal and <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Candidate other = (Candidate) obj;
        if ((this.stem == null) ? (other.stem != null) : !this.stem.equals(other.stem)) {
            return false;
        }
        if ((this.paradigm == null) ? (other.paradigm != null) : !this.paradigm.equals(other.paradigm)) {
            return false;
        }
        return true;
    }

    /**
     * Method that produces a hash code for the object.
     * @return The hash code produced.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.stem != null ? this.stem.hashCode() : 0);
        hash = 67 * hash + (this.paradigm != null ? this.paradigm.hashCode() : 0);
        return hash;
    }
    
    /**
     * Method that compares two Candidates. This method compares two candidates
     * defining which is bigger than the other one. For the comparison, first the
     * stems are compared: if the stems are different, the method returns the
     * result of applying the method <code>compareTo()</code> of the String
     * objects of the stems.  It is worth noting that if one of the stems is NULL,
     * we consider that it is lower than any string. If the stems are equal,
     * then the paradigm names are compared in the same way than stems.
     * @return Returns -1 if the current object is lower than the new one, 0 if
     * they are equal, and 1 otherwise
     */
    @Override
    public int compareTo(Object obj) throws ClassCastException  
    {
        final Candidate other = (Candidate) obj;
        if (this.stem == null){
            if(other.stem == null){
                if (this.paradigm == null){
                    if(other.paradigm == null){
                        return 0;
                    }
                    else{
                        return -1;
                    }
                }
                else{
                    if(other.paradigm == null){
                        return 1;
                    }
                    else{
                        return this.paradigm.compareTo(other.paradigm);
                    }
                }
            }
            else{
                return -1;
            }
        }
        else{
            if(other.stem == null){
                return 1;
            }
            else{
                return this.stem.compareTo(other.stem);
            }
        }
   } 
    
    /**
     * Method that produces the whole expansion of the candidate.
     * The method expands the combination of the stem and the paradigm, producing
     * a map with all the surface forms possible as a key and the set of possible 
     * suffixes that can generate that surface form as a value.
     * @param dic Dictionary from which the the paradigm is read.
     * @return Returns a map with all the surface forms possible as a key and the
     * set of possible suffixes that can generate that surface form as a value.
     */
    public Map<String,Set<Suffix>> GetExpansion(Dictionary dic){
        if(this.expansion==null){
            Paradigm p=new Paradigm(this.paradigm, dic);
            lemma=stem+p.GetLemmaSuffix();
            this.expansion=new HashMap<String, Set<Suffix>>();
            for(Suffix s: p.getSuffixes()){
                StringBuilder sb=new StringBuilder(this.stem);
                sb.append(s.getSuffix());
                String sf=sb.toString();
                if(!this.expansion.containsKey(sf)) {
                    this.expansion.put(sf,new LinkedHashSet<Suffix>());
                }
                this.expansion.get(sf).add(s);
            }
        }
        return this.expansion;
    }
    
    /**
     * Method that returns the lemma of the candidate
     * @param dic Dictionary form which the lemma is generated
     * @return Returns the lema of the candidate
     */
    public String GetLemma(Dictionary dic){
        if(lemma==null){
            for(DixElement parchild: dic.pardefs.getParadigmDefinition(
                    this.paradigm).elements.get(0).children){
                if(parchild instanceof P){
                    this.lemma=(this.stem+((P)parchild).r.getValueNoTags());
                    break;
                }
            }
        }
        return lemma;
    }
    
    /**
     * Method that returns a <code>String</code> representation of the candidate.
     * This method returns a representation of the candidate as a <code>String</code>
     * with the format "stem|paradigm"
     * @return Returns a representation of the candidate as a <code>String</code>
     * with the format "stem|paradigm"
     */
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder(this.stem);
        sb.append("|");
        sb.append(this.paradigm);
        return sb.toString();
    }

    public Node getReftotree() {
        return reftotree;
    }

    public void setReftotree(Node reftotree) {
        this.reftotree = reftotree;
    }
    
    public JSONObject toJSON(Dictionary dic){
        JSONObject json = new JSONObject();
        
        
        JSONArray surfaceFormList = new JSONArray();
        
        Map<String,Set<Suffix>> expanded=this.GetExpansion(dic);
        for (Map.Entry<String,Set<Suffix>> entry: expanded.entrySet()){
            for (Suffix suf: entry.getValue()){
                JSONObject surfaceForm = new JSONObject();
                JSONArray lexInfoList = new JSONArray();

                for(String lex: suf.getLexInfo()){
                    lexInfoList.add(lex);
                }
                
                surfaceForm.put("tags",lexInfoList);
                
                surfaceForm.put("surfaceform", entry.getKey());

                
                surfaceFormList.add(surfaceForm);
            }
        }
        
        json.put("expansion", surfaceFormList);
        json.put("stem",this.getStem());
        json.put("paradigm",this.getParadigm());
        json.put("lemma",this.GetLemma(dic));
        
        return json;
    }
}
