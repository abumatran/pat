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
import es.ua.dlsi.querying.Vocabulary;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Thread for getting the representative candidates for a given set of candidates.
 * This thread gets a representative set of N candidates from a collection of 
 * candidates. To do so, it computes the total occurence of every candidate in a
 * corpus (the summation of all the inflected word forms of hte candidate) and
 * it builds a sorted list. Then it takes the N most representative candidates
 * by splitting the list in as many parts as candidates retrieved and picking
 * the central one in every partition. For example, in a list of 20 candidates,
 * want to get 4 representative candidates we would get candidates if we 2, 7, 
 * 12, and 17. 
 * @author Miquel Esplà Gomis
 */
public class ThreadPrintRepresentativeCandidates extends Thread{

    /**
     * Sub-class that implements a comparator for the sorted  list of candidates
     * which sorts the candidate regarding the total number of occurences.
     */
    public static class byValue implements Comparator {
        /** Data ordered by using a map with the candidates in the key and the
         * occurrences in the value */
        Map<Candidate,Integer> data;
        /**
         * Overloaded constructor adding the new data to the object.
         * @param data New data to be added to the object. This data is used when
         * comparing to get the number of occurrences for each of the candidates
         * compared
         */
        public byValue(Map<Candidate,Integer> data){
            this.data=data;
        }
        
        /**
         * Implementation of the comparator.
         * @param o1 First candidate in the comparison
         * @param o2 Second candidate in the comparison
         * @return Returns 1 if element 1 occurs less than element 2 and -1 otherwise
         */
        public int compare(Object o1, Object o2) {
            Candidate e1=(Candidate)o1;
            Candidate e2=(Candidate)o2;

            if (data.get(e1) < data.get(e2)){
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    /**
     * Static object that keeps track of the number of threads running.
     */
    private static int numberofthreads=0;
    
    /** Vocabulary used to compute the occurrence. */
    private Vocabulary vocabulary;
    
    /** Dictionary which contains the inflexion information for the candidates. */
    private Dictionary dic;
    
    /** Set of candidates to be checked. */
    private Set<Candidate> candidateset;
    
    /** Number of representative candidates to be retrieved. */
    private int numofelements;
    
    /** Output object where the output of the process will be printed */
    private PrintWriter output;

    /**
     * Overloaded constructor.
     * @param vocabulary new vocabulary
     * @param dic new dictionary
     * @param candidateset set of candidates to be checked
     * @param numofelements number of elemenets to be retrieved
     * @param output output <code>PrintWriter</code>
     */
    public ThreadPrintRepresentativeCandidates(Vocabulary vocabulary, Dictionary dic,
            Set<Candidate> candidateset, int numofelements, PrintWriter output){
        this.vocabulary = vocabulary;
        this.dic = dic;
        this.candidateset = candidateset;
        this.numofelements = numofelements;
        this.output = output;
        NewThread();
    }
    
    /**
     * Syncronised method that prints a new line in the output stream.
     * @param output Output <code>PrintWriter</code>
     * @param text Test to be written
     */
    private static synchronized void PrintLineOutput(PrintWriter output, String text){
        output.println(text);
        output.flush();
    }
    
    /**
     * Syncronised method that increments the thread counter
     */
    private static synchronized void NewThread(){
        numberofthreads++;
    }
    
    /**
     * Syncronised method that substracts one to the thread counter
     */
    private static synchronized void ThreadFinished(){
        numberofthreads--;
    }
    
    /**
     * Method that returns the number of threads running.
     * @return Returns the number of threads running
     */
    public static synchronized int GetNumberOfThreads(){
        return numberofthreads;
    }
    
    /**
     * Method that runs the algorithm for getting the set of representative 
     * candidates from the list of candidates.
     */
    public void run() {
        Map<Candidate,Integer> unsortedcollection=new HashMap<Candidate, Integer>();
        for(Candidate c: candidateset){
            int numberofocc=0;
            for(String form: c.GetSurfaceForms(dic)) {
                numberofocc+=vocabulary.Get(form);
            }
            unsortedcollection.put(c, numberofocc);
        }
        byValue comparator=new byValue(unsortedcollection);
        SortedMap<Candidate,Integer> sortedmap=
                new TreeMap<Candidate, Integer>(comparator);
        sortedmap.putAll(unsortedcollection);
        int separation=(int)((float)sortedmap.size()/numofelements);
        int initpoint=Math.round((float)separation/2);
        List<Candidate> sortedcandidatelist=new LinkedList<Candidate>(sortedmap.keySet());
        for(int pos=initpoint; numofelements>0; pos+=separation,numofelements--){
            Candidate c=sortedcandidatelist.get(pos-1);
            PrintLineOutput(output,c.toString());
        }
        ThreadFinished();
    }
}