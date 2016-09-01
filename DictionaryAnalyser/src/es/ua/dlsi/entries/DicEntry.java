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

package es.ua.dlsi.entries;

import dics.elements.dtd.*;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.paradigms.DicParadigm;
import es.ua.dlsi.utils.Pair;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class which contains a string which is used as a suffix of a word or set of
 * words.
 * @author Miquel Esplà i Gomis
 */
public class DicEntry {

    /**
     * Method that recursively expands an element of the dictionary to produce
     * a surface form.
     * @param element Element to be expanded.
     * @param dic Dictionary from which the information of the paradigm are read.
     * @return Returns a set of surface forms as a result of the expansion.
     */
    public static Set<String> ExpandElement(E element, boolean withlexinfo, Dictionary dic){
        Set<String> exit=new HashSet<String>();
        for(DixElement de: element.children){
            if(de instanceof P){
                StringBuilder suffix;
                if(withlexinfo){
                    suffix=new StringBuilder(((P)de).r.getValueNoTags());
                    for(DixElement pe: ((P)de).r.children){
                        if(pe instanceof S){
                            suffix.append("<");
                            suffix.append(((S)pe).name);
                            suffix.append(">");
                        }
                    }
                }
                else {
                    suffix=new StringBuilder(((P)de).l.getValueNoTags());
                }
                exit.add(suffix.toString());
            }
            else if(de instanceof Par){
                Set<String> roots=new HashSet<String>();
                for(String sb: exit){
                    if(withlexinfo) {
                        sb+="+";
                    }
                    roots.addAll(DicParadigm.ExpandParadigm(dic.pardefs.getParadigmDefinition(((Par)de).name),sb, withlexinfo, dic));
                }
                exit=roots;
            }
        }
        return exit;
    }

    /**
     * Method that recursively expands an element of the dictionary to produce
     * a surface form.
     * @param element Element to be expanded.
     * @param dic Dictionary from which the information of the paradigm are read.
     * @return Returns a set of surface forms as a result of the expansion.
     */
    public static List<Pair<StringBuilder,StringBuilder>> ExpandElement(
            E element, Dictionary dic, Set<String> restriction){
        List<Pair<StringBuilder,StringBuilder>> exit=new
                LinkedList<Pair<StringBuilder, StringBuilder>>();
        exit.add(new Pair<StringBuilder, StringBuilder>(new StringBuilder(""),
                new StringBuilder("")));
        for(DixElement de: element.children){
            if(de instanceof P){
                StringBuilder suffix_l, suffix_r;
                suffix_r=new StringBuilder();
                //suffix_r=new StringBuilder(((P)de).r.getValueNoTags());
                for(DixElement pe: ((P)de).r.children){
                    if(pe instanceof S){
                        suffix_r.append("<");
                        suffix_r.append(((S)pe).name);
                        suffix_r.append(">");
                    }
                }
                suffix_l=new StringBuilder(((P)de).l.getValueNoTags());
                
                for(Pair<StringBuilder,StringBuilder> sb: exit){
                    sb.getFirst().append(suffix_l);
                    sb.getSecond().append(suffix_r);
                }
            }
            else if(de instanceof Par){
                //System.out.println("PAR: "+((Par)de).name);
                List<Pair<StringBuilder,StringBuilder>> roots=new LinkedList<Pair<StringBuilder,StringBuilder>>();
                for(Pair<StringBuilder,StringBuilder> sb: exit){
//                    sb.getSecond().append("+");
                    roots.addAll(DicParadigm.ExpandParadigm(
                            dic.pardefs.getParadigmDefinition(((Par)de).name),
                            sb.getFirst().toString(), sb.getSecond().toString(),
                            dic, restriction));
                }
                exit=roots;
            }
        }
        return exit;
    }
    
    /**
     * Method that returns a pair stem/paradigm from an element of the dictionary.
     * @param element Element in the dictionary from which the stem/paradigm will be extracted.
     * @return Candidate object containing the exctracted stem/paradigm object.
     */
    public static Candidate GetStemParadigm(E element){
        String paradigm=null, stem;
        stem=GetStem(element);
        if(stem!=null){
            for(DixElement de: element.children){
                if(de instanceof Par){
                    paradigm=((Par)de).name;
                }
            }
            if(paradigm!=null) {
                return new Candidate(stem,paradigm);
            }
            else {
                return null;
            }
        }
        else{
            return null;
        }
    }
    
    /**
     * Method that returns the stem from an element of the dictionary.
     * Method that returns the stem from an element of the dictionary.
     * @param element Element in the dictionary from which the stem will be extracted.
     * @return The stem found in the entry. If no stem is found in the entry, the
     * method returns <code>null</code>.
     */
    public static String GetStem(E element){
        for(DixElement de: element.children){
            if(de instanceof I) {
                return ((I)de).getValueNoTags();
            }
            if(de instanceof P) {
                return ((P)de).l.getValueNoTags();
            }
        }
        return "";
    }
}
