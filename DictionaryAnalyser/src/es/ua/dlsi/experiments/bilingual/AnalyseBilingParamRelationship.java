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

package es.ua.dlsi.experiments.bilingual;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.bilingual.ParadigmRelationship;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class contains a set of methods that allow to obtain the relationship
 * between the paradimgs and the lexical information of the entries in a pair
 * of dictionaries through the corresponding bilingual dictionary.
 * @author Miquel Esplà i Gomis
 */
public class AnalyseBilingParamRelationship {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Dictionary dic_left;
        Dictionary dic_right;

        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option orightdic = parser.addStringOption('l',"right-dic");
        CmdLineParser.Option oleftdic = parser.addStringOption('r',"left-dic");
        CmdLineParser.Option obilingdic = parser.addStringOption('b',"biling-dic");
        CmdLineParser.Option oremove1entry = parser.addBooleanOption("remove-1entrypars");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option oreverse = parser.addBooleanOption("reverse");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option obinoutput = parser.addStringOption("binary-output");

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

        String rightdic=(String)parser.getOptionValue(orightdic,null);
        String leftdic=(String)parser.getOptionValue(oleftdic,null);
        String bilingdic=(String)parser.getOptionValue(obilingdic,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        boolean remove1entry=(Boolean)parser.getOptionValue(oremove1entry,false);
        boolean reverse=(Boolean)parser.getOptionValue(oreverse,false);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);
        String binoutput=(String)parser.getOptionValue(obinoutput,null);

        PrintWriter pw;
        if(output!=null){
            try{
                pw=new PrintWriter(output);
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file '"+output+"'.");
                pw=new PrintWriter(System.out);
            }
        } else{
            System.err.println("Warning: output file not defined. Output redirected to standard output.");
            pw=new PrintWriter(System.out);
        }

        DictionaryReader dicReader = new DictionaryReader(leftdic);
        dic_left=dicReader.readDic();
        dicReader = new DictionaryReader(rightdic);
        dic_right=dicReader.readDic();
        Dictionary biling_dic;
        dicReader = new DictionaryReader(bilingdic);
        biling_dic = dicReader.readDic();

        ParadigmRelationship par_relationship=new ParadigmRelationship(biling_dic,
                 dic_left, dic_right, reverse, remove1entry, notclosedcats);
        if(binoutput!=null){
            try{
                ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(binoutput));
                oos.writeObject(par_relationship);
                oos.close();
            }catch(IOException e){
                e.printStackTrace(System.err);
            }
        }
        for(String s: par_relationship.keySet()){
            pw.println(s);
            Map<String,Double> tmpmap=new LinkedHashMap<String, Double>();
            for(Entry<String,Double> e: par_relationship.get(s).entrySet()){
                pw.print("\t");
                pw.print(e.getKey());
                pw.print(": ");
                pw.println(e.getValue());
            }
            par_relationship.put(s, tmpmap);
        }
        pw.close();
    }
}
