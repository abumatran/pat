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
import es.ua.dlsi.lexicalinformation.ClosedCategories;
import es.ua.dlsi.utils.CoocurenceTable;

/**
 * Class that represents the information about the relationship between lexical
 * categories in two languages. This class keeps a table with the normalised
 * frequency with which, given a bilingual dictionary with languages A and B, a
 * lexical category in language A coocurs with another lexical category in
 * language B.
 * @author Miquel Esplà Gomis
 */
public class LexicalCategoryRelationship extends BilingualRelationship{
    
    /**
     * Constructor that computes, for every lexical categroy in one of the
     * languages of a bilingual dictionary, the proportion of times that is
     * related with the lexical categories in the other language.
     * This method computes, for every lexical category in one of the languages
     * of a bilingual dictionary, the proportion of coocurences with the other
     * lexical categories in the dictionary.
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
    public LexicalCategoryRelationship(Dictionary biling_dic, boolean reverse,
            boolean remove1entry, boolean notclosedcats){
        CoocurenceTable<String> coocurencetable=new CoocurenceTable<String>();
        for(Section sec : biling_dic.sections) {
            for (E e : sec.elements) {
                if(!e.isMultiWord()){
                    for(DixElement de: e.children){
                        if(de instanceof I){
                            I i=(I)de;
                            if(!i.getSymbols().isEmpty()){
                                String lstmp1=i.getSymbols().get(0).name;
                                if(!ClosedCategories.isClosedCat(lstmp1)){
                                    coocurencetable.add(lstmp1, lstmp1);
                                }
                            }
                        }
                        else if(de instanceof P){
                            P p=(P)de;
                            if(!p.l.getSymbols().isEmpty()){
                                String lstmp1=p.l.getSymbols().get(0).name;
                                if(!ClosedCategories.isClosedCat(lstmp1)){
                                    if(!p.r.getSymbols().isEmpty()){
                                        String lstmp2=p.r.getSymbols().get(0).name;
                                        if(reverse){
                                            coocurencetable.add(lstmp2, lstmp1);
                                        }else{
                                            coocurencetable.add(lstmp1, lstmp2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(remove1entry){
            for(String element: coocurencetable.getFirstLevelElements()){
                if(coocurencetable.getTotalOccurences(element)<=1){
                    coocurencetable.Remove(element);
                }
            }
        }
        bilingual_relationship=coocurencetable.Normalise();
    }
}
