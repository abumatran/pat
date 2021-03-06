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

package es.ua.dlsi.querying;

import dics.elements.dtd.Dictionary;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.paradigms.paradigmprofiling.ParadigmProfiler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a set of surface forms generated by the concatenation
 * of a stem to a set of suffixes coming from a paradigm
 * @author Miquel Esplà i Gomis
 */
public class ScoredSurfaceFormsSet{

    /** A set of surface forms and the corresponding score fro the inflection */
    private Map<String,Double> surfaceforms;
    
    public ScoredSurfaceFormsSet(ScoredSurfaceFormsSet sfs){
        surfaceforms=new HashMap<String, Double>();
        for(Map.Entry<String,Double> entry: sfs.surfaceforms.entrySet()){
            surfaceforms.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Overloaded constructor of the class
     * @param steam The new stem for the class
     * @param paradigm The paradigm which contains the suffixes used to generate the surface forms
     * @param plf The set of lexical forms assigned to the paradigm in the dictionary
     */
    public ScoredSurfaceFormsSet(String stem, String paradigm, Dictionary dic, ParadigmProfiler plf){
        surfaceforms=new HashMap<String, Double>();
        //Surface forms are generated and the probability for each inflection is obtained
        Set<Suffix> suffix_set=(new Candidate(stem, paradigm)).getSuffixes(dic);
        
        for(Suffix s: suffix_set){
            StringBuilder sb=new StringBuilder(stem);
            sb.append(s.getSuffix());
            surfaceforms.put(sb.toString(),plf.getAveragedProfile().get(s.getSuffix()));
        }
    }

    /**
     * Method that returns the value of {@link #surfaceforms}
     * @return Returns the value of {@link #surfaceforms}
     */
    public Set<String> getSurfaceForms() {
        return surfaceforms.keySet();
    }

    /**
     * Method that returns the score of a concrete surface form
     * @param surfaceform Surface form for which the score should be returned
     * @return Returns the score of a concrete surface form
     */
    public double getSurfaceFormScore(String surfaceform){
        return this.surfaceforms.get(surfaceform);
    }
    
    /**
     * Method that compares two scored surface forms sets. Method that compares
     * two scored surface forms sets by checking if they share the elements.
     * @param o The object with which the current object is to be compared.
     * @return Returns <code>true</code> if the two sets contain the same surface
     * word forms, and <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o){
        if(o.getClass()!=this.getClass()){
            return false;
        }
        else{
            ScoredSurfaceFormsSet sfs=(ScoredSurfaceFormsSet)o;
            return (this.surfaceforms.keySet().equals(sfs.surfaceforms.keySet()));
        }
    }

    /**
     * Method that computes a hash score for the object based on the list of
     * surface forms. Method that computes a hashs score for the object based on
     * the list of surface forms
     * @return Returns a hash score for the object based on the list of surface
     * forms
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.surfaceforms != null ? this.surfaceforms.hashCode() : 0);
        return hash;
    }
    
    /**
     * Method that checks if a given surface word form is in the list. Method
     * that checks if a given surface word form is in the list.
     * @param sf Surface word form to be searched in the list
     * @return Returns <code>true</code> if the surface word form occurs in the
     * list and <code>false</code> otherwise
     */
    public boolean contains(String sf){
        return this.surfaceforms.keySet().contains(sf);
    }
    
    /**
     * Method that removes a surface word form from the set of surface forms.
     * Method that removes a surface word form from the set of surface forms.
     * @param form The word form to be removed from the list
     */
    public void RemoveForm(String form){
        this.surfaceforms.remove(form);
    }
}
