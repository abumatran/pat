/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a table in which the coocurence of two set of elements
 * is stored. This class represents a table in which the coocurence of two set
 * of elements is stored. The class is implemented on a hash map of two levels
 * and it provides several methods for ease the addition of new elements, or
 * the normalisation. It is worth noting that the implementation of the table
 * is asymetric. For example, one could get the coocurence of a given element A
 * in the first level of the map with an element of B, an element in the second
 * level, but not vice-versa.
 * @author Miquel Esplà Gomis.
 */
public class CoocurenceTable<T> {
    
    /** Map that contains a coocurence table counting the number of coocurences
     * of a pair of String elements.
     */
    Map<T,Map<T,ModificableInteger>> coocurence_table;
    
    /**
     * Default constructor of the class. This method only initialises the table.
     */
    public CoocurenceTable(){
        coocurence_table=new HashMap<T, Map<T, ModificableInteger>>();
    }
    
    /**
     * Method that returns the number of coocurences of a pair of elements in the
     * table.
     * @param elem1 First element in the talbe.
     * @param elem2 Second element in the table.
     * @return Returns the number of coocurences of a pair of elements in the
     * table.
     */
    public int get(T elem1, T elem2){
        if(coocurence_table.containsKey(elem1)){
            if(coocurence_table.get(elem1).containsKey(elem2)){
                return coocurence_table.get(elem1).get(elem2).getValue();
            }
            else{
                return 0;
            }
        }
        else{
            return 0;
        }
    }
    
    /**
     * Method that adds a new occurence of two elements to the table. This method
     * adds a new occurence of a pair of elements to the table. If the elements
     * already exist, the count is incremented. If not, it is initialised to 1.
     * @param elem1 First element in the table.
     * @param elem2 Second element in the table.
     */
    public void add(T elem1, T elem2){
        if(coocurence_table.containsKey(elem1)){
            if(coocurence_table.get(elem1).containsKey(elem2)){
                coocurence_table.get(elem1).get(elem2).increment();
            }
            else{
                coocurence_table.get(elem1).put(elem2, new ModificableInteger(1));
            }
        }
        else{
            Map<T,ModificableInteger> temp_map=new HashMap<T,ModificableInteger>();
            temp_map.put(elem2,new ModificableInteger(1));
            coocurence_table.put(elem1, temp_map);
        }
    }
    
    /**
     * Method that returns the elements in the first level of the hash map.
     * @return Returns the elements in the first level of the hash map.
     */
    public Set<T> getFirstLevelElements(){
        return coocurence_table.keySet();
    }
    
    /**
     * Method that returns all the occureneces of a given element of the table.
     * This method computes the sumation of all the occurences of a geiven element
     * in the first level of the hash map with all the elements of the second
     * level.
     * @param elem1 Element to be checked.
     * @return Returns all the occureneces of a given element of the table
     */
    public int getTotalOccurences(T elem1){
        int total=0;
        for(Map.Entry<T,ModificableInteger> e: coocurence_table.get(elem1).entrySet()) {
            total+=e.getValue().getValue();
        }
        return total;
    }
    
    /**
     * Method that returns the key set of the bilingual hashmap.
     * @return Returns the key set of the bilingual hashmap.
     */
    public Map<T,Map<T,Double>> Normalise(){
        Map<T,Map<T,Double>> exit=new HashMap<T, Map<T, Double>>();
        for(T s: coocurence_table.keySet()){
            Map<T,Double> tmpmap=new HashMap<T, Double>();
            int total=getTotalOccurences(s);
            for(Map.Entry<T,ModificableInteger> e: coocurence_table.get(s).entrySet()) {
                tmpmap.put(e.getKey(),(e.getValue().getValue())/(double)total);
            }
            exit.put(s, tmpmap);
        }
        return exit;
    }
    
    /**
     * Method that removes an entry in the second level hash map.
     * @param elem1 Element in the first level of the hash map.
     * @param elem2 Element in the secon dlevel of the hash map.
     */
    public void Remove(T elem1, T elem2){
        if(coocurence_table.containsKey(elem1)){
            if(coocurence_table.get(elem1).containsKey(elem2)){
                coocurence_table.get(elem1).remove(elem2);
            }
        }
    }
    
    /**
     * Method that removes an entry in the first level hash map and, consequently,
     * all the elements of the second level hash map related with it.
     * @param elem Element in the first level of the hash map.
     */
    public void Remove(T elem){
        if(coocurence_table.containsKey(elem)){
            coocurence_table.remove(elem);
        }
    }
}
