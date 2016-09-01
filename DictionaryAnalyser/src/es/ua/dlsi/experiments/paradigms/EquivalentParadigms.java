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

package es.ua.dlsi.experiments.paradigms;

import es.ua.dlsi.paradigms.*;
import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that only contains a main method which expands a pair stem-paradigm.
 * This class contains a main method that expands a pair stem-paradigm in an
 * Apertium's monolingual dictionary by producing all the possible surface forms
 * and, if specified, the lexical information corresponding of each of them
 * @author Miquel Esplà i Gomis
 */
public class EquivalentParadigms {

    /**
     * Main method that performs the expansion of the stem-paradigm pair. Main
     * method that expands a pair stem-paradigm in an Apertium's monolingual
     * dictionary by producing all the possible surface forms and, if specified,
     * the lexical information corresponding of each of them
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        CmdLineParser parser = new CmdLineParser();
        //Stem to be used for expansion
        CmdLineParser.Option oword = parser.addStringOption('w',"word");
        //Paradigm to be used for expansion
        CmdLineParser.Option oparadigm = parser.addStringOption('p',"paradigm");
        //Path to a file containing the list of paradigms to be used for expansion
        CmdLineParser.Option oparadigmlist = parser.addStringOption("paradigm-list");
        //Option for specifying the output file path
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        //Dictionary to be expanded
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");

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

        String word=(String)parser.getOptionValue(oword,null);
        String original_paradigm=(String)parser.getOptionValue(oparadigm,null);
        String outputpath=(String)parser.getOptionValue(ooutput,null);
        String paradigmlist=(String)parser.getOptionValue(oparadigmlist,null);
        String dictionary=(String)parser.getOptionValue(odictionary,null);

        Set<String> paradigm_set=null;
        Dictionary dic;

        if(dictionary==null){
            System.err.println("Error: It is necessary to set the dictionary path (use opton -d or --dictionary).");
            System.exit(-1);
        }
        if(original_paradigm==null){
            System.err.println("Error: It is necessary to set a paradigm (use opton -p or --paradigm)");
            System.exit(-1);
        }
        if(paradigmlist!=null){
            paradigm_set=new HashSet(Arrays.asList(paradigmlist.split(",")));
        }
        else{
            System.err.print("Error: a comma-separated paradigm list has to be provided.");
            System.exit(-1);
        }

        if(word==null){
            System.err.println("Error: It is necessary to set a paradigm (use opton -p or --paradigm)");
            System.exit(-1);
        }

        PrintWriter output;
        if(outputpath!=null){
            try {
                output = new PrintWriter(outputpath);
            } catch (FileNotFoundException ex) {
                System.err.println("Warning: output file could not be opened; exit will be printed on screen;");
                output = new PrintWriter(System.out);
            }
        } else{
            System.err.println("Warning: no output file defined; exit will be printed on screen;");
            output = new PrintWriter(System.out);
        }

        DictionaryReader dicReader = new DictionaryReader(dictionary);
        dic=dicReader.readDic();
        if(dic==null){
            System.err.print("There was an error while reading dictionary in ");
            System.err.println(dictionary);
        }
        else{
            Dix2suffixtree d2s;
            d2s=new Dix2suffixtree(dic);
            Set<Candidate> candidates=d2s.getSuffixTree().SegmentWord(word);
            Map<String,String> candidates_map=new HashMap<String, String>();
            for(Candidate c: candidates){
                if(!c.getParadigm().contains("__num")){
                    if(candidates_map.containsKey(c.getParadigm())){
                        if(c.getStem().length()<candidates_map.get(c.getParadigm()).length()){
                            candidates_map.put(c.getParadigm()+"|"+word.substring(c.getStem().length()),word.substring(0, c.getStem().length()));
                        }
                        else{
                            candidates_map.put(c.getParadigm()+"|"+word.substring(candidates_map.get(c.getParadigm()).length()),word.substring(0, candidates_map.get(c.getParadigm()).length()));
                            candidates_map.put(c.getParadigm(),c.getStem());
                        }
                    }
                    else
                        candidates_map.put(c.getParadigm(),c.getStem());
                }
            }
            if(!candidates_map.keySet().equals(paradigm_set)){
                System.err.println("ERROR: different sets:");
                for(String p: candidates_map.keySet()){
                    System.err.print(p);
                    System.err.print(",");
                }
                System.err.println();
                for(String p: paradigm_set){
                    System.err.print(p);
                    System.err.print(",");
                }
                System.err.println();
                System.exit(-1);
            }
            
            String par;
            if(original_paradigm.contains("|"))
                par=original_paradigm.split("\\|")[0];
            else
                par=original_paradigm;
            Pardef orig_p= dic.pardefs.getParadigmDefinition(par);
            Set<String> original_wordforms=DicParadigm.ExpandParadigm(orig_p,candidates_map.get(original_paradigm),false, dic);
            Set<String> equivalent_paradigms=new HashSet<String>();
            equivalent_paradigms.add(original_paradigm);
            for(String candidatepar: candidates_map.keySet()){
                if(candidatepar.contains("|"))
                    par=candidatepar.split("\\|")[0];
                else
                    par=candidatepar;
                Pardef p= dic.pardefs.getParadigmDefinition(par);
                Set<String> wordforms=DicParadigm.ExpandParadigm(p,candidates_map.get(candidatepar),false, dic);
                /*System.err.print(candidatepar);
                System.err.print("\t");
                for (String s: wordforms){
                    System.err.print(s);
                    System.err.print(" ");
                }
                System.err.println();*/
                if(wordforms.equals(original_wordforms))
                    equivalent_paradigms.add(candidatepar);
            }
            for(String p: equivalent_paradigms){
                output.print(p);
                output.print(" ");
            }
            output.println();
        }
        output.close();
    }
}
