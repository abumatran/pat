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

package es.ua.dlsi.id3;

import es.ua.dlsi.monolingual.EquivalentCandidates;
import java.util.List;

/**
 * Class that represents an instance in the data for building a ID3 tree. This
 * class contains the data in one of the instances used for building a ID3 tree.
 * Every instance is asigned to a class (a candidate, in this case), a
 * probability, and an a collection of surface forms which can be generated
 * with the class.
 * @author Miquel Esplà Gomis
 */
public class Instance {
    /** 
     * Array of boolean indicating which of the whole surface forms are generated
     * by the instance.
     */
    private List<Boolean> attributes;

    /**
     * Cnaidate (or set of equivalent candidates) asigned to the instance.
     */
    private EquivalentCandidates instance_class;
    
    /**
     * Probability of the instance.
     */
    double probability;
    
    /**
     * Overloaded constructor of the class using default probability 1.
     * @param att_list Boolean array representing which of the surface forms are
     * actually generated.
     * @param cls Set of equivalent candidates asigned to the instance
     */
    public Instance(List<Boolean> att_list,EquivalentCandidates cls){
        attributes=att_list;
        instance_class=cls;
        probability=1;
    }
    
    /**
     * Overloaded constructor of the class.
     * @param att_list Boolean array representing which of the surface forms are
     * actually generated.
     * @param cls Set of equivalent candidates asigned to the instance
     * @param probability Probability of the instance
     */
    public Instance(List<Boolean> att_list,EquivalentCandidates cls, double probability){
        attributes=att_list;
        instance_class=cls;
        if(probability==0.0){
            this.probability=Entropy.minimum_probability;
        }
        else{
            this.probability=probability;
        }
    }

    /**
     * Method that returns the list of attributes.
     * @return Returns the list of attributes.
     */
    public List<Boolean> getAttributes() {
        return attributes;
    }

    /**
     * Method that returns the set of equivalent canidates asigned to the instance.
     * @return Returns the set of equivalent canidates asigned to the instance.
     */
    public EquivalentCandidates getInstanceClass(){
        return instance_class;
    }
    
    /**
     * Method that returns the probability of the instance.
     * @return Returns the probability of the instance.
     */
    public double GetProbability(){
        return this.probability;
    }
    
    /**
     * Method that sets a new probability for the instance.
     * @param prob New probability for the instance.
     */
    public void SetProbability(double prob){
        this.probability=prob;
    }
}
