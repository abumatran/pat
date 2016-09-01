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

package es.ua.dlsi.id3;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.EquivalentCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.PrintWriter;
import java.util.*;

/**
 * Main class that builds an ID3 tree for a given surface form by using a suffix
 * tree to obtain the candidates stem/paradigm. Main class that builds an ID3
 * tree for a given surface form by using a suffix tree to obtain the candidates
 * stem/paradigmThe output in dot format. The output is formated as dot.
 * @author Miquel Esplà Gomis
 */
public class BuildTreeFromSuffixTree {

    /**
     * Main method that builds the tree for a given word by using a suffix tree.
     * This method builds a suffix tree from an Apertium dictionary (option -d)
     * and, given a surface word (option -w), obtains the set of candidates
     * stem/paradigm that could generate it. Then, it builds an ID3 tree that
     * optimises the number of questions to be performed to a user for deciding
     * an only candidate. The instances table can be saved to a file by defining
     * the option -t and the output tree is generated in dot format in the path
     * defined with option -o. Candidates belonging to closed categories can be
     * discarded by using the option --not-closedcats
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option oword = parser.addStringOption('w',"word");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option onotclosedcategories = parser.addBooleanOption("not-closedcats");
        CmdLineParser.Option otable = parser.addStringOption('t',"table");
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
        String output=(String)parser.getOptionValue(ooutput,null);
        String word=(String)parser.getOptionValue(oword,null);
        String tablepath=(String)parser.getOptionValue(otable,null);
        boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);

        PrintWriter pwtree;
        try{
            pwtree=new PrintWriter(output);
        }
        catch(Exception e){
            System.err.println("Output redirected to standard output.");
            pwtree=new PrintWriter(System.out);
        }

        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        Set<Candidate> candidates=new LinkedHashSet<Candidate>();
        candidates.addAll(d2s.getSuffixTree().SegmentWord(word));
        
        Set<String> possiblesurfaceforms=new LinkedHashSet<String>();
        //the key of this map is the set of surface forms and the value is the set of paradigms generating them
        Set<EquivalentCandidates> sf_candidate=new LinkedHashSet<EquivalentCandidates>();
        for(Candidate c: candidates){
            if(!notclosedcategories || ! c.isClosedCategoryParadigm(dic)){
                boolean added=false;
                for(EquivalentCandidates ec: sf_candidate){
                    if(c.GetSurfaceForms(dic).equals(ec.getSurfaceForms(dic))){
                        ec.addCandidate(c);
                        added=true;
                        break;
                    }
                }
                if(!added){
                    sf_candidate.add(new EquivalentCandidates(c));
                    possiblesurfaceforms.addAll(c.GetSurfaceForms(dic));
                }
            }
        }
        
        if(tablepath!=null){
            PrintWriter pwtable;
            try{
                pwtable=new PrintWriter(tablepath);
                for(String s: possiblesurfaceforms){
                    pwtable.print(s);
                    pwtable.print(",");
                }
                pwtable.println("paradigm_class");
                //for(int i=0;i<sf_candidate.size();i++){
                for(EquivalentCandidates ec: sf_candidate){
                    for(String s: possiblesurfaceforms){
                        if(ec.getSurfaceForms(dic).contains(s)) {
                            pwtable.print("true,");
                        }
                        else {
                            pwtable.print("false,");
                        }
                    }
                    pwtable.println(ec.toString());
                    pwtable.println();
                }
                pwtable.close();
            }
            catch(Exception e){
                System.err.println("Output path for table "+tablepath+" couldn't be created.");
                e.printStackTrace(System.err);
            }
        }
        
        InstanceCollection records;

        // read in all our data
        records=new InstanceCollection();
        records.buildInstances(possiblesurfaceforms,sf_candidate, dic);

        Tree tree=new Tree(records);
        
        tree.Print(pwtree);
        pwtree.close();
    }
}
