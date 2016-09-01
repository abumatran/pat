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

import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class that only contains a main method which expands a pair stem-paradigm.
 * This class contains a main method that expands a pair stem-paradigm in an
 * Apertium's monolingual dictionary by producing all the possible surface forms
 * and, if specified, the lexical information corresponding of each of them
 * @author Miquel Esplà i Gomis
 */
public class ExpandStemParadigm {

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
        CmdLineParser.Option ostem = parser.addStringOption('s',"stem");
        //Paradigm to be used for expansion
        CmdLineParser.Option oparadigm = parser.addStringOption('p',"paradigm");
        //Path to a file containing the list of stems to be used for expansion
        CmdLineParser.Option ostemlistfile = parser.addStringOption("stem-list");
        //Path to a file containing the list of paradigms to be used for expansion
        CmdLineParser.Option oparadigmlistfile = parser.addStringOption("paradigm-list");
        //Option for specifying the output file path
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        //Dictionary to be expanded
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        //Flag for specifying whether lexical information should be produced
        //together with the surface forms obtained
        CmdLineParser.Option owithlexinfo = parser.addBooleanOption('l',"lexical-info");

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

        String stem=(String)parser.getOptionValue(ostem,null);
        String paradigm=(String)parser.getOptionValue(oparadigm,null);
        String outputpath=(String)parser.getOptionValue(ooutput,null);
        String stemlistfile=(String)parser.getOptionValue(ostemlistfile,null);
        String paradigmlistfile=(String)parser.getOptionValue(oparadigmlistfile,null);
        String dictionary=(String)parser.getOptionValue(odictionary,null);
        boolean withlexinfo=(Boolean)parser.getOptionValue(owithlexinfo,false);

        List<String> paradigm_list=new LinkedList<String>();
        List<String> stem_list=new LinkedList<String>();
        Dictionary dic;

        if(dictionary==null){
            System.err.println("Error: It is necessary to set the dictionary path (use opton -d or --dictionary).");
            System.exit(-1);
        }
        if(paradigm!=null){
            if(paradigmlistfile!=null){
                System.err.println("Error: option -p/--paradigm and option --paradigm-list are incompatible.");
                System.exit(-1);
            }
            else {
                paradigm_list.add(paradigm);
            }
        }
        else{
            if(paradigmlistfile!=null){
                try {
                    BufferedReader br = new BufferedReader(new FileReader(paradigmlistfile));
                    String line;
                    while ((line=br.readLine())!=null){
                        paradigm_list.add(line);
                    }
                } catch (FileNotFoundException ex) {
                    System.err.print("Error: paradigm list file '");
                    System.err.print(paradigmlistfile);
                    System.err.println("' could not be found.");
                    System.exit(-1);
                } catch (IOException ex) {
                    System.err.print("Error while reading file '");
                    System.err.print(paradigmlistfile);
                    System.err.println("'.");
                    System.exit(-1);
                }
            }
            else{
                System.err.println("Error: It is necessary to set a paradigm (use opton -p or --paradigm)"
                        + "or the path of a file containing a list of paradigms (use opton --paradigm-list).");
                System.exit(-1);
            }
        }
        if(stem!=null){
            if(stemlistfile!=null){
                System.err.println("Error: option -s/--stem and option --stem-list are incompatible.");
                System.exit(-1);
            }
            else {
                stem_list.add(stem);
            }
        }
        else{
            if(stemlistfile!=null){
                try {
                    BufferedReader br = new BufferedReader(new FileReader(stemlistfile));
                    String line;
                    while ((line=br.readLine())!=null){
                        stem_list.add(line);
                    }
                } catch (FileNotFoundException ex) {
                    System.err.print("Error: paradigm list file '");
                    System.err.print(paradigmlistfile);
                    System.err.println("' could not be found.");
                    System.exit(-1);
                } catch (IOException ex) {
                    System.err.print("Error while reading file '");
                    System.err.print(paradigmlistfile);
                    System.err.println("'.");
                    System.exit(-1);
                }
            }
            else{
                System.err.println("Error: It is necessary to set a paradigm (use opton -p or --paradigm)"
                        + "or the path of a file containing a list of paradigms (use opton --paradigm-list).");
                System.exit(-1);
            }
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
            for(String par: paradigm_list){
                for(String st: stem_list){
                    Pardef p= dic.pardefs.getParadigmDefinition(par);
                    Set<String> expansions=DicParadigm.ExpandParadigm(p,st,withlexinfo, dic);
                    for(String exp: expansions) {
                        output.println(par+"+"+st+";"+exp);
                    }
                }
            }
        }
        output.close();
    }
}
