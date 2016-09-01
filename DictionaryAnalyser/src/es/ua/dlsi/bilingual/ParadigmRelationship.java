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

package es.ua.dlsi.bilingual;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.DixElement;
import dics.elements.dtd.E;
import dics.elements.dtd.I;
import dics.elements.dtd.P;
import dics.elements.dtd.Section;
import es.ua.dlsi.lexicalinformation.LexicalForms;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that represents the information about the relationship between paradigms
 * in two languages. This class keeps a table with the normalised frequency with
 * which, given a bilingual dictionary with languages A and B, a paradigm in
 * language A coocurs with another paradigm in language B.
 * @author Miquel Esplà Gomis
 */
public class ParadigmRelationship extends BilingualRelationship{
    
    /**
     * Constructor that computes, for every paradigm in one of the languages of
     * a bilingual dictionary, the proportion of times that is related with the
     * paradigms in the other language.
     * This method computes, for every paradigm in one of the languages
     * of a bilingual dictionary, the total proportion of coocurences with other
     * paradigms in the other language.
     * @param biling_dic Bilingual dictionary object
     * @param dic_left Left-language dictionary object from which the left-side
     * paradigms will be read for matching every bilingual entry
     * @param dic_right Right-language dictionary object from which the right-side
     * paradigms will be read for matching every bilingual entry
     * @param reverse If this variable is set to <code>true</code>, the process
     * will be performed from right to left instead of from left to right
     * @param remove1entry If this variable is set to <code>true</code>, the
     * paradigms that only occur once in the dictionary are removed
     * @param notclosedcats If this variable is set to <code>true</code>, the
     * entries belonging to a closed category are ignored
     * @return Returns map with a key corresponding to the name of a paradigm
     * from the left language, and a value which is another map, with the key
     * corresponding to the value of a paradigm of the right side langauge, and
     * the value the proportion of times that these two paradigms coocur (if the
     * reverse flag is activated, the structure is inverted)
     */
    public ParadigmRelationship(Dictionary biling_dic, Dictionary dic_left,
            Dictionary dic_right, boolean reverse, boolean remove1entry,
            boolean notclosedcats){
        LexicalForms lf_left=new LexicalForms(dic_left);
        LexicalForms lf_right=new LexicalForms(dic_right);
        GetParadigmsRelationship(biling_dic, lf_left, lf_right, reverse,
                remove1entry, notclosedcats);
    }
    
    /**
     * Constructor that computes, for every paradigm in one of the languages of
     * a bilingual dictionary, the proportion of times that is related with the
     * paradigms in the other language.
     * This method computes computes, for every paradigm in one of the languages
     * of a bilingual dictionary, the total number of coocurences in an entry of
     * the bilingual dictionary of a word belonging to this paradigm and the
     * paradigms of the other language.
     * @param biling_dic Bilingual dictionary object
     * @param dic_left Left-language dictionary object
     * @param dic_right Right-language dictionary object
     * @param reverse If this variable is set to <code>true</code>, the process
     * will be performed from right to left instead of from left to right
     * @param remove1entry If this variable is set to <code>true</code>, the
     * paradigms that only occur once in the dictionary are removed
     * @param notclosedcats If this variable is set to <code>true</code>, the
     * entries belonging to a closed category are ignored
     * @return Returns map with a key corresponding to the name of a paradigm
     * from the left language, and a value which is another map, with the key
     * corresponding to the value of a paradigm of the right side langauge, and
     * the value the proportion of times that these two paradigms coocur (if the
     * reverse flag is activated, the structure is inverted)
     */
    public ParadigmRelationship(Dictionary biling_dic, LexicalForms lf_left,
            LexicalForms lf_right, boolean reverse, boolean remove1entry,
            boolean notclosedcats){
        GetParadigmsRelationship(biling_dic, lf_left, lf_right, reverse,
                remove1entry, notclosedcats);
    }
    
    /**
     * Method that computes, for every paradigm in one of the languages of a
     * bilingual dictionary, the proportion of times that is related with the
     * paradigms in the other language.
     * This method computes computes, for every paradigm in one of the languages
     * of a bilingual dictionary, the total number of coocurences in an entry of
     * the bilingual dictionary of a word belonging to this paradigm and the
     * paradigms of the other language.
     * @param biling_dic Bilingual dictionary object
     * @param dic_left Left-language dictionary object
     * @param dic_right Right-language dictionary object
     * @param reverse If this variable is set to <code>true</code>, the process
     * will be performed from right to left instead of from left to right
     * @param remove1entry If this variable is set to <code>true</code>, the
     * paradigms that only occur once in the dictionary are removed
     * @param notclosedcats If this variable is set to <code>true</code>, the
     * entries belonging to a closed category are ignored
     * @return Returns map with a key corresponding to the name of a paradigm
     * from the left language, and a value which is another map, with the key
     * corresponding to the value of a paradigm of the right side langauge, and
     * the value the proportion of times that these two paradigms coocur (if the
     * reverse flag is activated, the structure is inverted)
     */
    private void GetParadigmsRelationship(Dictionary biling_dic,
            LexicalForms lf_left, LexicalForms lf_right, boolean reverse, 
            boolean remove1entry, boolean notclosedcats){
        Map<String,Map<String,Double>> correlation=new HashMap<String, Map<String,Double>>();
        //Main bulce checking all the entries in the bilingual dictionary
        for(Section sec : biling_dic.sections) {
            for (E e : sec.elements) {
                //Checking that the entry is not a multiword
                if(!e.isMultiWord()){
                    for(DixElement de: e.children){
                        //If the element is equivalent left-right
                        if(de instanceof I){
                            I i=(I)de;
                            String stem=i.getValueNoTags();
                            List<String> lstmp=LexicalForms.GetLexicalInfo(i,notclosedcats);
                            //The process continues only if the entrie does not belong to a closed category
                            if(lstmp!=null){
                                //Getting the list of paradigms which sahre the same parading than this lexical form
                                Set<String> lpar=lf_left.GetParadigmsFromLexForm(stem, lstmp);
                                if(lpar!=null){
                                    //Getting the list of paradigms which sahre the same parading than this lexical form
                                    Set<String> rpar=lf_right.GetParadigmsFromLexForm(stem, lstmp);
                                    if(rpar!=null){
                                        //If the corelation is to be computed from right to left side
                                        if(reverse){
                                            //Filling the table
                                            for(String rparadigm: rpar){
                                                if(correlation.containsKey(rparadigm)){
                                                    Map<String,Double> submap=correlation.get(rparadigm);
                                                    for(String lparadigm: lpar){
                                                        if(submap.containsKey(lparadigm)){
                                                            double n=submap.get(lparadigm)+(1.0/(double)lpar.size());
                                                            submap.put(lparadigm, n);
                                                        }
                                                        else {
                                                            submap.put(lparadigm, 1.0/(double)lpar.size());
                                                        }
                                                    }
                                                }
                                                else{
                                                    Map<String,Double> submap=new HashMap<String, Double>();
                                                    for(String lparadigm: lpar){
                                                        submap.put(lparadigm, 1.0/(double)lpar.size());
                                                    }
                                                    correlation.put(rparadigm, submap);
                                                }
                                            }
                                        //If the corelation is to be computed from left to right side
                                        }else{
                                            //Filling the table
                                            for(String lparadigm: lpar){
                                                if(correlation.containsKey(lparadigm)){
                                                    for(String rparadigm: rpar){
                                                        Map<String,Double> submap=correlation.get(lparadigm);
                                                        if(submap.containsKey(rparadigm)){
                                                            double n=submap.get(rparadigm)+(1.0/(double)lpar.size());
                                                            submap.put(rparadigm, n);
                                                        }
                                                        else {
                                                            submap.put(rparadigm, (1.0/(double)lpar.size()));
                                                        }
                                                    }
                                                }
                                                else{
                                                    for(String rparadigm: rpar){
                                                        Map<String,Double> submap=new HashMap<String, Double>();
                                                        submap.put(rparadigm,(1.0/(double)lpar.size()));
                                                        correlation.put(lparadigm, submap);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //If the element is not equivalent left-right (is a pair)
                        else if(de instanceof P){
                            P p=(P)de;
                            String lstem=p.l.getValueNoTags();
                            List<String> lstmp=LexicalForms.GetLexicalInfo(p.l, notclosedcats);
                            if(lstmp!=null){
                                Set<String> lpar=lf_left.GetParadigmsFromLexForm(lstem, lstmp);
                                if(lpar!=null){
                                    String rstem=p.r.getValueNoTags();
                                    lstmp=LexicalForms.GetLexicalInfo(p.r, notclosedcats);
                                    Set<String> rpar=lf_right.GetParadigmsFromLexForm(rstem, lstmp);
                                    if(rpar!=null){
                                        //If the corelation is to be computed from right to left side
                                        if(reverse){
                                            //Runing through the paradigms related in the rigth side
                                            for(String rparadigm: rpar){
                                                if(correlation.containsKey(rparadigm)){
                                                    Map<String,Double> submap=correlation.get(rparadigm);
                                                    for(String lparadigm: lpar){
                                                        if(submap.containsKey(lparadigm)){
                                                            double n=submap.get(lparadigm)+(1.0/(double)lpar.size());
                                                            submap.put(lparadigm, n);
                                                        }
                                                        else {
                                                            submap.put(lparadigm, 1.0/(double)lpar.size());
                                                        }
                                                    }
                                                }
                                                else{
                                                    Map<String,Double> submap=new HashMap<String, Double>();
                                                    for(String lparadigm: lpar){
                                                        submap.put(lparadigm, 1.0/(double)lpar.size());
                                                    }
                                                    correlation.put(rparadigm, submap);
                                                }
                                            }
                                        //If the corelation is to be computed from left to right side
                                        }else{
                                            for(String lparadigm: lpar){
                                                if(correlation.containsKey(lparadigm)){
                                                    for(String rparadigm: rpar){
                                                        Map<String,Double> submap=correlation.get(lparadigm);
                                                        if(submap.containsKey(rparadigm)){
                                                            double n=submap.get(rparadigm)+(1.0/(double)lpar.size());
                                                            submap.put(rparadigm, n);
                                                        }
                                                        else {
                                                            submap.put(rparadigm, (1.0/(double)lpar.size()));
                                                        }
                                                    }
                                                }
                                                else{
                                                    for(String rparadigm: rpar){
                                                        Map<String,Double> submap=new HashMap<String, Double>();
                                                        submap.put(rparadigm,(1.0/(double)lpar.size()));
                                                        correlation.put(lparadigm, submap);
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
        }
        //Normalising
        for(String s: correlation.keySet()){
            Map<String,Double> tmpmap=new LinkedHashMap<String, Double>();
            double total=0;
            for(Map.Entry<String,Double> e: correlation.get(s).entrySet()) {
                total+=e.getValue();
            }
            if(!remove1entry || total>1){
                for(Map.Entry<String,Double> e: correlation.get(s).entrySet()) {
                    tmpmap.put(e.getKey(),(e.getValue())/(double)total);
                }
            }
            this.bilingual_relationship.put(s, tmpmap);
        }
    }
}
