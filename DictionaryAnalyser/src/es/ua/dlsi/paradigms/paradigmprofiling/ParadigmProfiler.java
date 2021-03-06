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

import dics.elements.dtd.*;
import dics.elements.dtd.Dictionary;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.querying.Vocabulary;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class contains the set of words assigned to a paradigm in a dictionary.
 * In fact, it contains a paradigm and the set of stems belonguing to each word
 * in the dictinoary assigned to this paradigm. In this way, it is possible to
 * generate all the words which the paradigm can generate in the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class ParadigmProfiler implements Serializable{
    /**
     * Class which implements a comparator between instances of {@link DictionaryWord}.
     * It is used to compare these objects using the score assigned to them.
     */
    protected class LexicalFormComparator implements Comparator{
        @Override
        public int compare(Object o1, Object o2){
            DictionaryWord lf1=(DictionaryWord)o1;
            DictionaryWord lf2=(DictionaryWord)o2;

            if(lf1.getPercent()>lf2.getPercent()) {
                return 1;
            }
            else if(lf1.getPercent()<lf2.getPercent()) {
                return -1;
            }
            else {
                return 0;
            }
        }
    }

    /** A paradigm in the dictionary */
    private Paradigm paradigm;

    /** Set of words related with the paradigm */
    private SortedSet<DictionaryWord> words;

    /**
     * Averaged profile of the paradigm.
     * The averaged paradigm of the profile is obtained by concatenating, for a
     * suffix in a paradigm, all the possible stems related to the paradigm and
     * looking for them in a dictinoary. This map contains each of the suffixes
     * and the average number of inflections found in a corpus.
     */
    private Map<String,Double> averagedprofile;

    /**
     * Overloaded constructor of the class. Overloaded constructor of the class
     * @param paradigm Paradigm for which the object is created
     */
    public ParadigmProfiler(Paradigm paradigm, Dictionary dic) {
        this.paradigm=paradigm;
        this.words=new TreeSet<DictionaryWord>();
        this.averagedprofile=null;
        
        for(Section s: dic.sections){
            for(E element: s.elements){
                ContentElement root=null;
                for(DixElement delement: element.children){
                    if(delement instanceof I) {
                        root=(I)delement;
                    }
                    else if (delement instanceof P){
                        root=((P)delement).l;
                    }
                    else if(delement instanceof Par){
                        if(root!=null){
                            if(((Par)delement).name.equals(paradigm.getName())){
                                DictionaryWord lftmp=new DictionaryWord(root.getValueNoTags());
                                this.words.add(lftmp);
                                //System.out.println(lftmp.getSteam()+" "+((Par)delement).name+" "+this.words.size());
                            }
                            else{
                                String expanded=Expand(root.getValueNoTags(),
                                    dic.pardefs.getParadigmDefinition(((Par)delement).name).elements,
                                    dic, paradigm);
                                if(expanded!=null){
                                    DictionaryWord lftmp=new DictionaryWord(expanded);
                                    this.words.add(lftmp);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that returns the paradigm of the object. Method that returns the
     * paradigm of the object
     * @return Returns the paradigm of the object
     */
    public Paradigm getParadigm(){
        return this.paradigm;
    }

    /**
     * Method that adds a word to the list {@link #words}. Method that adds a
     * word to the list {@link #words}
     * @param lf The new word to be added
     */
    public void AddWord(DictionaryWord lf){
        words.add(lf);
    }

    /*public double getPercentil(DictionaryWord lexform){
        return (double)words.subSet(words.first(),lexform).size()/(words.size()-1);
    }*/

    /*public double getSurfaceFormAparition(DictionaryWord lexform){
        Map<String,Integer> profile=lexform.getProfile();
        int total=0;
        for(int appears: profile.values()){
            total+=appears;
        }
        return (double)total/profile.size();
    }*/

    /**
     * Method that returns the number of inflections of a word which appear in
     * the corpus. Those words which are not frequent for the paradigm are not
     * taken into account.
     * @param lexform The dictionary word from which the inflections are created
     * @param threshold The threshold of frequency of inflection
     * @return Returns an index of inflections appearing in the dictionary
     */
    public double getSurfaceFormAparition(DictionaryWord lexform, double threshold){
        Map<String,Double> aprofile=getAveragedProfile();
        Map<String,Integer> profile=lexform.getProfile();
        int total=0;
        int sum=0;
        //The profile is a map with the different possible inflections root+suffix,
        //and a flag indicating if they were found in the corpus or not
        for(Entry suffix: profile.entrySet()){
            //The aprofile (average profile) indicates, for every suffix s, for
            //how many words in the dictionary the surface form generated when
            //combining the corresponding root with s appeared in the corpus
            if(aprofile.get((String)suffix.getKey())>threshold){
                sum+=(Integer)suffix.getValue();
                total++;
            }
        }
        if(total==0) {
            return 0;
        }
        else {
            return (double)sum/Math.sqrt(total);
        }
    }

    /**
     * Method that generates the profile of a paradigm, it is, the frequency
     * of apparition of the generated inflections in a corpus. Method that
     * generates the profile of a paradigm, it is, the frequency of apparition
     * of the generated inflections in a corpus
     * @param dic The dictionary containing the paradigm and the words
     * @param wordlist The list of words related with the paradigm
     */
    public void BuildProfiles(Vocabulary wordlist){
        SortedSet<DictionaryWord> newwords=new TreeSet<DictionaryWord>(new LexicalFormComparator());
        for(DictionaryWord dw: this.words){
            dw.setProfile(paradigm, wordlist);
            newwords.add(dw);
        }
        this.words=newwords;
    }

    /**
     * Method that obtains all the possible inflections derived of combining a
     * paradigm and a stem. Method that obtains all the possible inflections 
     * derived of combining a paradigm and a stem
     * @param stem Stem for the inflections
     * @param elements Elements in the dictionary which contain the suffixes to create the inflections
     * @param dic Dictionary containing the suffixes of the word
     * @param paradigm Paradigm from which the inflections should be created
     * @return
     */
    private String Expand(String stem, List<E> elements, Dictionary dic, Paradigm paradigm)
    {
         StringBuilder newlema=null;
         for(E element: elements)
         {
             for (DixElement e: element.children)
             {
                 if (e instanceof P){
                    newlema=new StringBuilder(stem);
                    newlema.append(((P)e).l.getValueNoTags());
                 } else if (e instanceof Par && newlema!=null)
                 {
                     if(((Par)e).name.equals(paradigm.getName())) {
                         return newlema.toString();
                     }
                     else{
                         List<E> parElements=dic.pardefs.getParadigmDefinition(((Par)e).name).elements;
                         //msg.err("Paradigm "+((Par)e).name+" has "+parElements.size()+" elements");
                         String tresult=Expand(newlema.toString(), parElements, dic, paradigm);
                         return tresult;
                     }
                 }
             }
         }
         return null;
    }

    /**
     * Method that returns the averaged profile for all the inflections in the
     * paradigm. Method that returns the averaged profile for all the
     * inflections in the paradigm
     * @return Returns the averaged profile for all the inflections in the paradigm
     */
    public Map<String,Double> getAveragedProfile(){
        if(this.averagedprofile==null){
            this.averagedprofile=new HashMap<String, Double>();
            for(Suffix suffix: this.paradigm.getSuffixes()){
                int sum=0;
                for(DictionaryWord lexform: this.words) {
                    sum+=lexform.getProfile().get(suffix.getSuffix());
                }
                if(this.words.size()==0)
                    this.averagedprofile.put(suffix.getSuffix(),0.0);
                else
                    this.averagedprofile.put(suffix.getSuffix(),(double)sum/this.words.size());
            }
        }
        return this.averagedprofile;
    }

    /**
     * Most usual suffix in the paradigm. Most usual suffix in the paradigm
     * @return Most usual suffix in the paradigm
     */
    public String getMostUsualSuffix(){
        Map<String,Double> profile=getAveragedProfile();
        double max=-1;
        String exit="";
        for(Entry pair: profile.entrySet()){
            if(((Double)pair.getValue())>max){
                max=(Double)pair.getValue();
                exit=(String)pair.getKey();
            }
        }
        return exit;
    }

    /**
     * Method that returns the second most usual suffix in the paradigm.
     * Method that returns the second most usual suffix in the paradigm
     * @return Returns the second most usual suffix in the paradigm
     */
    public String getSecondMostUsualSuffix(){
        Map<String,Double> profile=getAveragedProfile();
        double max=-1;
        double prev=-1;
        String exit="";
        String prevexit="";
        for(Entry pair: profile.entrySet()){
            if(((Double)pair.getValue())>max){
                prev=max;
                prevexit=exit;
                max=(Double)pair.getValue();
                exit=(String)pair.getKey();
            }
        }
        if(prev==-1) {
            return exit;
        }
        else {
            return prevexit;
        }
    }
    
    /**
     * Method that returns the number of words in the profile of the paradigm.
     * Method that returns the number of words in the profile of the paradigm
     * @return Returns the number of words in the profile of the paradigm
     */
    public int NumberOfWords(){
        return this.words.size();
    }
}
