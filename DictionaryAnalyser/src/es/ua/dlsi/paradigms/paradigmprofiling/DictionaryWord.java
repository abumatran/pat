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

package es.ua.dlsi.paradigms.paradigmprofiling;

import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.querying.Vocabulary;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that contains a word in the dictionary, it is, a stem related to a paradigm
 * from which all the possible inflections can be obtained.
 * @author Miquel Esplà i Gomis
 */
public class DictionaryWord implements Comparable<DictionaryWord> {

    /** Stem of the word */
    private String stem;

    /** Profile of the word, consisting in a list of inflections and the number
     * of occurrences in a monolingual corpus */
    private Map<String,Integer> profile;

    /**
     * Overloaded constructor of the class
     * @param stem Stem of the word
     */
    public DictionaryWord(String stem){
        this.stem=stem;
    }

    /**
     * Method that returns the profile of a stem and a paradigm
     * @param stem Stem of the word
     * @param paradigm Paradigm which generates the inflections
     * @param wordlist Word list in which the inflections should be searched
     * @return Returns the normalized profile of a word
     */
    public Map<String,Integer> GetNormalizedProfile(String stem, Paradigm paradigm,
            Vocabulary wordlist){
        Map<String,Integer> hits=new HashMap<String, Integer>();
        for(Suffix s: paradigm.getSuffixes()){
            if(wordlist.Contains(stem+s.getSuffix())) {
                hits.put(s.getSuffix(), 1);
            }
            else {
                hits.put(s.getSuffix(), 0);
            }
        }
        return hits;
    }

    /**
     * Method that computes the profile of a word and saves it in the variable {@link #profile}
     * @param paradigm Paradigm which generates the inflections
     * @param wordlist Word list in which the inflections should be searched
     */
    public void setProfile(Paradigm paradigm, Vocabulary wordlist){
        this.profile=GetNormalizedProfile(stem, paradigm, wordlist);
    }

    /**
     * Method that returns a saved profile
     * @return A saved profile
     */
    public Map<String, Integer> getProfile() {
        return profile;
    }

    /**
     * Method that returns a profile
     * @return Returns a profile
     */
    public String getStem() {
        return stem;
    }

    /**
     * Method that sets the stem of the object
     * @param stem New stem to be set
     */
    public void setStem(String stem) {
        this.stem = stem;
    }

    /**
     * Method that returns the percentage of inflections of the word appearing in the monolingual corpus
     * @return Returns the percentage of inflections of the word appearing in the monolingual corpus
     */
    public double getPercent(){
        int total=0;
        if(this.profile==null) {
            return 0;
        }
        else{
            for(int i: this.profile.values()) {
                total+=i;
            }
            return (double)total/this.profile.values().size();
        }
    }

    /*@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DictionaryWord other = (DictionaryWord) obj;
        if ((this.stem == null) ? (other.stem != null) : !this.stem.equals(other.stem)) {
            return false;
        }
        if (this.profile != other.profile && (this.profile == null || !this.profile.equals(other.profile))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.stem != null ? this.stem.hashCode() : 0);
        hash = 13 * hash + (this.profile != null ? this.profile.hashCode() : 0);
        return hash;
    }*/

    /**
     * Method that produces a hash code for the class taking into account only
     * the stem of the word.
     * @return Returns a hash code for the class taking into account only the
     * stem of the word
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.stem != null ? this.stem.hashCode() : 0);
        return hash;
    }

    /**
     * Method that checks whether two <code>DictionaryWord</code> objects are
     * equal or not. Method that checks whether two <code>DictionaryWord</code>
     * objects are equal or not by comparing the stem
     * @param obj The object with which the current object is to be compared
     * @return Returns <code>true</code> if the two objects are equal and <code>
     * false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DictionaryWord other = (DictionaryWord) obj;
        if ((this.stem == null) ? (other.stem != null) : !this.stem.equals(other.stem)) {
            return false;
        }
        return true;
    }

    /**
     * Method that compares two <code>DictionaryWord</code> objects. Method that
     * compares the stems of two <code>DictionaryWord</code> objects 
     * @param obj The object with which the current object is to be compared
     * @return Returns returns -1 if the stem of the boject is > than the stem of
     * <code>obj</code>, 0 if they are equal and -1 otherwise
     */
    public int compareTo(DictionaryWord o) {
        return stem.compareTo(o.stem);
    }
}
