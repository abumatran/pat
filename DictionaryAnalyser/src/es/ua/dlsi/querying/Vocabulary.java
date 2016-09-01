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

import es.ua.dlsi.monolingual.Paradigm;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class that represents a vocabulary with the number of occurrences of every
 * word in a corpus. This class contains the information of a vocabulary from a 
 * corpus. It is implemented on a <code>HashMap</code> which has the words of the
 * vocabulary and, as the value, the number of occurrences of every word in the
 * corpus
 * @author Miquel Esplà Gomis
 */
public class Vocabulary implements Serializable{
    
    /** The structure that contains the vocabulary information. */
    Map<String,Integer> vocabulary;
    
    /**
     * Overloaded constructor of the vocabulary. This method reads the
     * vocabulary from a file containing, for each line, a word and the number
     * of occurrences of that word in a corpus separated by a tabulator
     * @param vocabularypath Path where the file to be read is placed
     * @throws FileNotFoundException If the file cannot be found, an exception is thrown
     * @throws IOException If there is an error while reading the file, an exception
     * is thrown
     */
    public Vocabulary(String vocabularypath) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(vocabularypath));
        String line;
        vocabulary=new HashMap<String, Integer>();
        while ((line = br.readLine()) != null) {
            String[] readed = line.split(" ");
            int hits = Integer.parseInt(readed[0]);
            String word = readed[1];
            vocabulary.put(word, hits);
        }
    }
    
    /**
     * Method that returns the most frequent surface form inflected from a pair
     * stem/paradigm candidate by using the occurrence information in the
     * vocabulary.
     * @param stem The stem used to produce the inflected forms
     * @param paradigm The paradigm used to produce the inflected forms
     * @return Returns the most frequent surface form inflected from a pair
     * stem/paradigm candidate by using the occurrence information in the
     * vocabulary
     */
    public String GetMostFrequentSurfaceForm(String stem, Paradigm paradigm){
        int best_occ=0;
        String exit=null;
        Set<String> surfaceforms=paradigm.GetSurfaceFroms(stem);
        for(String form: surfaceforms){
            int occurrences;
            if(vocabulary.containsKey(form)) {
                occurrences=vocabulary.get(form);
            }
            else {
                occurrences=0;
            }
            if(best_occ<occurrences){
                best_occ=occurrences;
                exit=form;
            }
        }
        return exit;
    }
    
    /**
     * This method checks if a given surface form occurs in the vocabulary.
     * @param surfaceform The surface form to be checked in the vocabulary
     * @return The method returns <code>true</code> if the surface form occurs
     * in the vocabulary and <code>false</code> otherwise
     */
    public boolean Contains(String surfaceform){
        return (this.vocabulary.containsKey(surfaceform));
    }
    
    /**
     * This method returns the number of occurrences of a given surface form.
     * @param surfaceform Surface form for which the number of occurrences is
     * to be found
     * @return Returns the number of occurrences of a given surface form
     */
    public int Get(String surfaceform){
        if(this.vocabulary.containsKey(surfaceform)){
            return (this.vocabulary.get(surfaceform));
        }
        else{
            return 0;
        }
    }
}
