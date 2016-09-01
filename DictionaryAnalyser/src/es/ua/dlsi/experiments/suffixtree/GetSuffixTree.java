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

package es.ua.dlsi.experiments.suffixtree;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.E;
import dics.elements.dtd.Section;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class that contains an only main method for running the extraction of the suffix-tree
 * from the surface forms in the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class GetSuffixTree {

    /**
     * Main method that can be called to print the suffix tree
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option onotclosedcategories = parser.addBooleanOption("not-closedcats");
        //CmdLineParser.Option onot1wordparadigms = parser.addBooleanOption("not-1word-paradigms");

        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        String dicpath=(String)parser.getOptionValue(odicpath,null);
        boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);

        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        for(Section section: dic.sections){
            for(E element: section.elements){
                Candidate elem_stem_paradigm=DicEntry.GetStemParadigm(element);
                if(elem_stem_paradigm!=null){
                    if(!notclosedcategories || !elem_stem_paradigm.isClosedCategoryParadigm(dic)){
                        Set<String> surfaceforms=elem_stem_paradigm.GetSurfaceForms(dic);
                        Set<Candidate> candidates=new LinkedHashSet<Candidate>();
                        for(String form: surfaceforms) {
                            candidates.addAll(d2s.getSuffixTree().SegmentWord(form));
                        }
                        Set<Candidate> equal_candidates=new LinkedHashSet<Candidate>();
                        candidates.remove(elem_stem_paradigm);
                        for(Candidate candidate: candidates){
                            Set<String> expandedcandidate=candidate.GetSurfaceForms(dic);
                            if(surfaceforms.equals(expandedcandidate)) {
                                equal_candidates.add(candidate);
                            }
                        }
                        System.out.print("[");
                        System.out.print(elem_stem_paradigm.getStem());
                        System.out.print("|");
                        System.out.print(elem_stem_paradigm.getParadigm());
                        System.out.print("]");
                        for(Candidate eq_candidate: equal_candidates){
                            System.out.print(";");
                            System.out.print(eq_candidate.getStem());
                            System.out.print("|");
                            System.out.print(eq_candidate.getParadigm());
                        }
                        System.out.println();
                    }
                }
            }
        }
    }
}
