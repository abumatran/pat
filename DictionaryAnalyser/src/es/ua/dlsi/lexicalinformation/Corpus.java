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

import dics.elements.dtd.Dictionary;
import es.ua.dlsi.monolingual.Candidate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * Class that represents a monolingual corpus. This class represents a
 * monolingual corpus. It reads the file containing the corpus on demand.
 * @author Miquel Esplà Gomis.
 */
public class Corpus {
    
    /** Path where the file containing the corpus is placed */
    private String path;
    
    /**
     * Overloaded constructor of the class.
     * @param path Path where the file containing the corpus is placed
     */
    public Corpus(String path){
        this.path=path;
    }
    
    /**
     * Method that retrieves all the lines containing a given surface form in the
     * corpus.
     * @param word Word to be searched in the corpus
     * @return Returns the set of lines containing a given surface form in the
     * corpus.
     */
    public Set<String> GetAllExamples(String word){
        Set<String> examples=new LinkedHashSet<String>();
        LineIterator corpus_it=null;
        try {
            corpus_it = FileUtils.lineIterator(new File(this.path));
        } catch (FileNotFoundException ex) {
            System.err.println("Error while trying to open '"+this.path+"' file.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error while reading '"+this.path+"' file.");
            System.exit(-1);
        }
        while (corpus_it.hasNext()){
            String line = corpus_it.nextLine();
            //If the surface form appears in the sentence...
            if(line.matches("^"+word+" .*") || line.matches(".* "+word+"$") || line.matches(".* "+word+" .*")) {
                examples.add(line);
            }
        }
        corpus_it.close();
        return examples;
    }
    
    /**
     * Method that retrieves all the lines in the corpus containing any of the 
     * surface forms produced by a given candidate.
     * @param c Candidate generating the surface forms to be searched
     * @param dic Dictionary form which the candidate is extracted
     * @return Returns all the lines in the corpus containing any of the surface forms
     * produced by a given candidate
     */
    public Set<String> GetAllExamplesOfInflections(Candidate c, Dictionary dic){
        Set<String> inflectedwordforms=c.GetSurfaceForms(dic);
        Set<String> examples=new LinkedHashSet<String>();
        LineIterator corpus_it=null;
        try {
            corpus_it = FileUtils.lineIterator(new File(this.path));
        } catch (FileNotFoundException ex) {
            System.err.println("Error while trying to open '"+this.path+"' file.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error while reading '"+this.path+"' file.");
            System.exit(-1);
        }
        while (corpus_it.hasNext()){
            String line = corpus_it.nextLine();
            for(String word: inflectedwordforms){
                //If the surface form appears in the sentence...
                if(line.matches("^"+word+" .*") || line.matches(".* "+word+"$") || line.matches(".* "+word+" .*")) {
                    examples.add(line);
                }
            }
        }
        corpus_it.close();
        return examples;
    }
}
