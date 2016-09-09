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

import java.io.Serializable;
import java.util.List;
/**
 * Class which contains a string which is used as a suffix of a word or set of
 * words.
 * @author Miquel Esplà i Gomis
 */
public class Suffix implements Serializable{
    /** The suffix. */
    private String suffix;

    /** List of lexical tags. */
    private List<String> lexinfo;

    /**
     * Overloaded constructor of the class.
     * @param suffix New suffix
     */
    public Suffix(String suffix){
        this.suffix=suffix;
        this.lexinfo=null;
    }

    /**
     * Overloaded constructor of the class.
     * @param suffix New suffix
     * @param lexinfo Lexical tags
     */
    public Suffix(String suffix, List<String> lexinfo){
        this.suffix=suffix;
        this.lexinfo=lexinfo;
    }

    /**
     * Method that returns the suffix contained by the class.
     * @return Returns the suffix contained by the class
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Method that returns the list of lexical tags.
     * @return Returns the list of lexical tags
     */
    public List<String> getLexInfo(){
        return this.lexinfo;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.suffix != null ? this.suffix.hashCode() : 0);
        hash = 37 * hash + (this.lexinfo != null ? this.lexinfo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Suffix other = (Suffix) obj;
        if ((this.suffix == null) ? (other.suffix != null) : !this.suffix.equals(other.suffix)) {
            return false;
        }
        if (this.lexinfo != other.lexinfo && (this.lexinfo == null || !this.lexinfo.equals(other.lexinfo))) {
            return false;
        }
        return true;
    }
    
    

    /**
     * Method that sets a new suffix.
     * @param suffix Suffix to be set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    /**
     * Method that builds a string from the suffix. Method that builds a string
     * with the suffix by concatenating the lemma and the lexical information 
     * in the same fashion to that used in Apertium dictionaries.
     * @return Returns a String object with the resulting formatting
     */
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder(this.suffix);
        for(String label: this.lexinfo){
            sb.append("<");
            sb.append(label);
            sb.append(">");
        }
        return sb.toString();
    }
}
