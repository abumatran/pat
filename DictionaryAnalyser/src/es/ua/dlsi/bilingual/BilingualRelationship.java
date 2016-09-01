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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class that represents the information some kind of relationships between data
 * from two languages connected through a bilingual dictionary. This class
 * contains a hash map implementing a table in which every pair of elements is
 * related to a floating value. It is intended to represent a normalised table
 * of relationships between elements from two dictionaries which are connected
 * through a bilingaul dictionary, such as the relationship between paradigms or
 * the relation between the lexical categories.
 * @author Miquel Esplà Gomis
 */
public class BilingualRelationship{
    
    /** Map that contains the relationship between all the paradigms in both languages */
    Map<String,Map<String,Double>> bilingual_relationship;
    
    /**
     * Default constructor of the class. This method only initialises the
     * hash map containing the information.
     */
    public BilingualRelationship(){
        bilingual_relationship=new HashMap<String, Map<String, Double>>();
    }
    
    /**
     * Method that returns the key set of the bilingual hashmap.
     * @return Returns the key set of the bilingual hashmap.
     */
    public Set<String> keySet(){
        return this.bilingual_relationship.keySet();
    }
    
    /**
     * Method that returns the relations for a given element in the hash map.
     * @param key Element to be found in the map.
     * @return Retunrs the relations for a given element in the hash map.
     */
    public Map<String,Double> get(String key){
        return this.bilingual_relationship.get(key);
    }
    
    /**
     * Method that puts a new element in the hash map.
     * @param key Key of the new element
     * @param value Value of the new element
     * @return If a former element existed in the map with the same key, it is
     * replaced and returned.
     */
    public Map<String,Double> put(String key, Map<String,Double> value){
        return this.bilingual_relationship.put(key,value);
    }
}
