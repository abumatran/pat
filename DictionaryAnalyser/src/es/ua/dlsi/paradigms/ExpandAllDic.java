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
import es.ua.dlsi.lexicalinformation.ClosedCategories;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Class that only contains a main method which runs over all the entries of
 * a dictionary and expands them. This class contains a main method that
 * runs over all the entries of the an Apertium's monolingual dictionary and
 * expands them by producing all the surface forms and, if specified, the
 * lexical information
 * @author Miquel Esplà i Gomis
 */
public class ExpandAllDic {

    /** Dictionary to be expanded */
    private static Dictionary dic;

    /**
     * The main method that expands the dictionary. This main method that
     * runs over all the entries of the an Apertium's monolingual dictionary and
     * expands them by producing all the surface forms and, if specified, the
     * lexical information
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        CmdLineParser parser = new CmdLineParser();
        //Option for specifying the output file path
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        //Dictionary to be expanded
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        //Flag for specifying whether lexical information should be produced
        //together with the surface forms obtained
        CmdLineParser.Option owithlexinfo = parser.addBooleanOption('l',"lexical-info");
        //Flag for specifying if entries corresponding to closed categories should
        //be discarded
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("no-closedcats");

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

        String outputpath=(String)parser.getOptionValue(ooutput,null);
        String dictionary=(String)parser.getOptionValue(odictionary,null);
        boolean withlexinfo=(Boolean)parser.getOptionValue(owithlexinfo,false);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);

        if(dictionary==null){
            System.err.println("Error: It is necessary to set the dictionary path (use opton -d or --dictionary).");
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
            for(Section s: dic.sections){
                for(E e: s.elements){
                    String stem=null;
                    String par=null;
                    if(e.isMultiWord()){
                        System.err.println("Multiword: "+e.toString());
                    }
                    else{
                        for(DixElement de: e.children){
                            if(de instanceof I){
                                if(stem==null) {
                                    stem=((I)de).getValueNoTags();
                                }
                                else{
                                    stem=null;
                                    break;
                                }
                            }
                            else if(de instanceof P){
                                if(stem==null) {
                                    stem=((P)de).l.getValueNoTags();
                                }
                            }
                            else if(de instanceof Par){
                                if(par==null && stem!=null) {
                                    par=((Par)de).name;
                                }
                                else{
                                    stem=null;
                                    break;
                                }
                            }
                        }
                        if(stem!=null && par!=null){
                            Pardef p= dic.pardefs.getParadigmDefinition(par);
                            if(!notclosedcats || !ClosedCategories.isClosedCategoryParadigm(p)){
                                Set<String> expansions=DicParadigm.ExpandParadigm(p,
                                        stem, withlexinfo, dic);
                                for(String exp: expansions) {
                                    output.print(par);
                                    output.print("+");
                                    output.print(stem);
                                    output.print(";");
                                    output.println(exp);
                                }
                            }
                        }
                    }
                }
            }
        }
        output.close();
    }
}
