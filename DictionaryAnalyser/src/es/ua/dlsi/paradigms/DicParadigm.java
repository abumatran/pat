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


package es.ua.dlsi.paradigms;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.E;
import dics.elements.dtd.Pardef;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.utils.Pair;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class that contains some static method for dealing with the representation of
 * paradigms in Apertium dictionaries.
 * @author Miquel Esplà i Gomis
 */
public class DicParadigm {
    /**
     * Method that returns a set of inflected word forms from a stem and a
     * paradigm definition in an Apertium dictionary. Method that returns a set
     * of inflected word forms from a stem and a paradigm definition in an
     * Apertium dictionary
     * @param p Definition of the paradigm in an Apertium dictionary
     * @param stem Stem for inflecting the new surface word forms
     * @param dic Dictionary from which the paradigm is taken
     * @return Returns 
     */
    public static Set<String> ExpandParadigm(Pardef p, String stem,
            boolean withlexinfo, Dictionary dic){
        Set<String> exit=new LinkedHashSet<String>();
        for(E e: p.elements){
            Set<String> l=DicEntry.ExpandElement(e, withlexinfo, dic);
            StringBuilder sb=new StringBuilder(stem);
            for(String s: l){
                sb.append(s);
                exit.add(sb.toString());
            }
        }
        return exit;
    }
    /**
     * Method that returns a set of inflected word forms from a stem and a
     * paradigm definition in an Apertium dictionary. Method that returns a set
     * of inflected word forms from a stem and a paradigm definition in an
     * Apertium dictionary
     * @param p Definition of the paradigm in an Apertium dictionary
     * @param stem Stem for inflecting the new surface word forms
     * @param dic Dictionary from which the paradigm is taken
     * @return Returns 
     */
    public static List<Pair<StringBuilder,StringBuilder>> ExpandParadigm(Pardef p,
            String stem, String lexinfo, Dictionary dic, Set<String> restriction){

        List<Pair<StringBuilder, StringBuilder>> exit=new
                LinkedList<Pair<StringBuilder, StringBuilder>>();
        //System.out.println("----PAR: "+p.name+":"+p.elements);

        for(E e: p.elements){
            if(!e.isMultiWord() && (restriction==null || e.restriction==null || !restriction.contains(e.restriction))){
                List<Pair<StringBuilder, StringBuilder>> l=DicEntry.ExpandElement(
                        e, dic, restriction);
                for(Pair<StringBuilder,StringBuilder> s: l){
                    StringBuilder stemsb=new StringBuilder(stem);
                    stemsb.append(s.getFirst().toString());
                    StringBuilder lexinfosb=new StringBuilder(lexinfo);
                    lexinfosb.append(s.getSecond().toString());
                    exit.add(new Pair(stemsb, lexinfosb));
                }
            }
        }
        return exit;
    }
    
    /**
     * Method that returns a set of inflected word forms from a stem and a
     * paradigm definition in an Apertium dictionary. Method that returns a set
     * of inflected word forms from a stem and a paradigm definition in an
     * Apertium dictionary
     * @param parname Name of the paradigm in an Apertium dictionary to be expanded
     * @param stem Stem for inflecting the new surface word forms
     * @param dic Dictionary from which the paradigm is taken
     * @return Returns 
     */
    public static Set<String> ExpandParadigm(String parname, String stem,
            boolean withlexinfo, Dictionary dic){
        Pardef p=dic.pardefs.getParadigmDefinition(parname);
        if(p==null) {
            return null;
        }
        else {
            return ExpandParadigm(p, stem, withlexinfo, dic);
        }
    }
}
