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

package es.ua.dlsi.experiments.probabilitiesfromhmm;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.probabilitiesfromhmm.TextAnalyser;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Class that contains an only main method for running the extraction of the suffix-tree
 * from the surface forms in the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class AnalyseText {

    /**
     * Main method that can be called to print the suffix tree
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option ocorpuspath = parser.addStringOption('c',"corpus");
        CmdLineParser.Option ooutputpath = parser.addStringOption('o',"output");
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
        String corpuspath=(String)parser.getOptionValue(ocorpuspath,null);
        String outputpath=(String)parser.getOptionValue(ooutputpath,null);
        boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);
        
        PrintWriter pw;
        if(outputpath==null || outputpath.equals("")){
            System.err.println("Error: undefined output file could not be found. "
                    + "The output will be redirected to the standard output.");
            pw=new PrintWriter(System.out);
        }
        else{
            try{
                pw=new PrintWriter(outputpath);
            }
            catch(FileNotFoundException fnfe){
                System.err.println("Error: output file '"+outputpath+"' could not be found. "
                        + "The output will be redirected to the standard output.");
                pw=new PrintWriter(System.out);
            }
        }
        
        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        TextAnalyser analyser=new TextAnalyser(dic, d2s.getSuffixTree());
        analyser.ObtainTrainingSet(new File(corpuspath), pw);
        pw.close();
    }
}
