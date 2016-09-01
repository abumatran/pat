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

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.E;
import dics.elements.dtd.Pardef;
import dics.elements.dtd.Section;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.querying.RankedCandidate;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.ModificableInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Miquel Esplà Gomis
 */
public class FeatureExtractor {
    
    private Dictionary dic;
    
    private Dix2suffixtree d2s;
    
    private Vocabulary vocabulary;
    
    private String pathjavaobjects;
    
    private Map<String,ModificableInteger> entities_per_paradigm;
    
    private int max_entries_per_paradigm;
    
    private int max_inflections;
    
    public FeatureExtractor(Dictionary dic, Vocabulary vocabulary, Dix2suffixtree d2s,
            String pathjavaobjects){
        this.dic=dic;
        this.vocabulary=vocabulary;
        this.pathjavaobjects=pathjavaobjects;
        this.d2s=d2s;
        
        this.max_entries_per_paradigm=0;
        this.entities_per_paradigm=new HashMap<String, ModificableInteger>();
        for(Section s: dic.sections){
            for(E element: s.elements){
                Candidate c=DicEntry.GetStemParadigm(element);
                if(c!=null){
                    if(!this.entities_per_paradigm.containsKey(c.getParadigm())){
                        this.entities_per_paradigm.put(c.getParadigm(),new ModificableInteger());
                    }
                    this.entities_per_paradigm.get(c.getParadigm()).increment();
                }
            }
        }
        for(ModificableInteger mi: this.entities_per_paradigm.values()){
            if(this.max_entries_per_paradigm<mi.getValue()){
                this.max_entries_per_paradigm=mi.getValue();
            }
        }
        
        this.max_inflections=0;
        for(Pardef pardef: this.dic.pardefs.elements){
            Paradigm par=new Paradigm(pardef, dic);
            int num_inflections=par.GetSurfaceFroms("").size();
            if(num_inflections>max_inflections) {
                max_inflections=num_inflections;
            }
        }
    }
    
    public FeatureSet GetFeatureSet(String stem, String paradigmname, boolean notclosedcats){
        int total_entries=0;
        if(entities_per_paradigm.containsKey(paradigmname)){
            total_entries+=entities_per_paradigm.get(
                    paradigmname).getValue();
        }
        double prop_entries_paradigm=((double)total_entries/max_entries_per_paradigm);
        Candidate c=new Candidate(stem, paradigmname);
        Set<String> possible_surfaceforms=c.GetSurfaceForms(dic);
        //Proportion of surface forms over the maximum possible
        //numer of surface forms
        double prop_surfaceforms_dic=(double)possible_surfaceforms.size()/this.max_inflections;
        int appearing=0;
        for(String sf: possible_surfaceforms){
            if(vocabulary.Contains(sf)){
                appearing++;
            }
        }
        //Proportion of surface forms appearing in the corpus
        double prop_occurring_forms=(double)appearing/possible_surfaceforms.size();
        
        return new FeatureSet(stem, paradigmname, prop_entries_paradigm,
                prop_surfaceforms_dic, prop_occurring_forms);
    }
    
    public FeatureSet GetFeatureSet(RankedCandidate rc, boolean notclosedcats){
        int total_entries=0;
        for(Candidate candidate: rc.getCandidates()){
            if(entities_per_paradigm.containsKey(candidate.getParadigm())){
                total_entries+=entities_per_paradigm.get(
                        candidate.getParadigm()).getValue();
            }
        }
        double prop_entries_paradigm=((double)total_entries/max_entries_per_paradigm);
        Set<String> possible_surfaceforms=rc.getSurfaceForms(dic);
        //Proportion of surface forms over the maximum possible
        //numer of surface forms
        double prop_surfaceforms_dic=(double)possible_surfaceforms.size()/this.max_inflections;
        int appearing=0;
        for(String sf: possible_surfaceforms){
            if(vocabulary.Contains(sf)){
                appearing++;
            }
        }
        //Proportion of surface forms appearing in the corpus
        double prop_occurring_forms=(double)appearing/possible_surfaceforms.size();
        
        return new FeatureSet(rc, prop_entries_paradigm, prop_surfaceforms_dic,
                prop_occurring_forms);
    }
}
