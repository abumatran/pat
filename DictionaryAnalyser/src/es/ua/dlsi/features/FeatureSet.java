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

package es.ua.dlsi.features;

import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.querying.RankedCandidate;
import weka.core.DenseInstance;
import weka.core.Instance;

/**
 *
 * @author Miquel Esplà Gomis
 */
public class FeatureSet {
    private String stem;
    private String paradigm;
    private double proportion_of_entries_in_dictionary;
    private double proportion_of_inflections;
    private double proportion_of_inflections_occurring;

    public FeatureSet(String stem, String paradigm, double
            proportion_of_entries_in_dictionary, double
            proportion_of_inflections, double proportion_of_inflections_occurring) {
        this.stem=stem;
        this.paradigm=paradigm;
        this.proportion_of_entries_in_dictionary = proportion_of_entries_in_dictionary;
        this.proportion_of_inflections = proportion_of_inflections;
        this.proportion_of_inflections_occurring = proportion_of_inflections_occurring;
    }
    
    public FeatureSet(RankedCandidate rc, double
            proportion_of_entries_in_dictionary, double
            proportion_of_inflections, double proportion_of_inflections_occurring) {
        StringBuilder pars=new StringBuilder();
        for(Candidate c: rc.getCandidates()){
            pars.append(c.getParadigm());
            pars.append("|");
        }
        pars.delete(pars.length()-1, pars.length()-1);
        this.stem=rc.getCandidates().iterator().next().getStem();
        this.paradigm=pars.toString();
        this.proportion_of_entries_in_dictionary = proportion_of_entries_in_dictionary;
        this.proportion_of_inflections = proportion_of_inflections;
        this.proportion_of_inflections_occurring = proportion_of_inflections_occurring;
    }

    public String getStem() {
        return stem;
    }

    public String getParadigm() {
        return paradigm;
    }

    public double getProportionOfEntriesInDictionary() {
        return proportion_of_entries_in_dictionary;
    }

    public double getProportionOfInflections() {
        return proportion_of_inflections;
    }

    public double getProportionOFInflectionsOccurring() {
        return proportion_of_inflections_occurring;
    }
    
    public Instance toWekaInstance(){
        double[] features=new double[3];
        features[0]=proportion_of_entries_in_dictionary;
        features[1]=proportion_of_inflections;
        features[2]=proportion_of_inflections_occurring;
        
        return new DenseInstance(1, features);
    }
}
