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

package es.ua.dlsi.monolingual;

import dics.elements.dtd.*;
import dictools.utils.DicOpts;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Main class that processes a dictionary and removes the indicated entries. This
 * main class contains a main method that processes a dictionary and comments in
 * it all the entries referring any of the pairs stem/paradigm in the list of
 * entries to be removed
 * @author Miquel Esplà Gomis
 */
public class RemoveWordsFromDic {

    /**
     * Main method that performs the processing of the dictionary for removing
     * the specified elements. This method takes an Apertium dictionary (option
     * -d) and a list of candidates (option -w) and processes the dictionary for
     * commenting all the candidates specified. The output is set to the output
     * file defined through option -o.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option owordspath = parser.addStringOption('w',"words");
        //CmdLineParser.Option odebugpath = parser.addStringOption("debug");

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
        String wordspath=(String)parser.getOptionValue(owordspath,null);

        BufferedReader br=null;
        if(wordspath==null){
            System.err.println("Error: It is necessary to set the path to the file containing the list of words to remove (use opton -w or --words).");
            System.exit(-1);
        }
        else{
            try {
                br = new BufferedReader(new FileReader(wordspath));
            } catch (FileNotFoundException ex) {
                System.err.println("Error: file "+wordspath+" could not be found.");
                System.exit(-1);
            }
        }

        Dictionary dic=null;
        if(dictionary==null){
            System.err.println("Error: It is necessary to set the dictionary path (use opton -d or --dictionary).");
            System.exit(-1);
        }
        else{
            DictionaryReader dr=new DictionaryReader(dictionary);
            dic=dr.readDic();
        }

        List<Candidate> stemparlist=new LinkedList<Candidate>();
        try{
            String line;
            while((line=br.readLine())!=null){
                String[] parstem=line.split("\\|");
                try{
                    String stem=parstem[0];
                    System.out.println(stem);
                    String paradigm=parstem[1];
                    System.out.println(paradigm);
                    stemparlist.add(new Candidate(stem, paradigm));
                }catch(IndexOutOfBoundsException ex){
                    System.err.print("Error while reading wordlist file at line '");
                    System.err.print(line);
                    System.err.println("'");
                    System.exit(-1);
                }
            }
        }catch(IOException ex){
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        for(Section s: dic.sections){
            for(E e: s.elements){
                Candidate c=null;
                for(DixElement de: e.children){
                    if(c==null){
                        if(de instanceof I){
                            for(Candidate tmpc: stemparlist){
                                if(tmpc.getStem().equals(((I)de).getValueNoTags())){
                                    c=tmpc;
                                    break;
                                }
                            }
                        }
                        else if(de instanceof P){
                            for(Candidate tmpc: stemparlist){
                                if(tmpc.getStem().equals(((P)de).l.getValueNoTags())){
                                    c=tmpc;
                                    break;
                                }
                            }
                        }
                    }
                    else if(de instanceof Par){
                        if(((Par)de).name.equals(c.getParadigm())){
                            e.prependCharacterData="<!--";
                            e.appendCharacterData="-->";
                            break;
                        }
                    }
                }
            }
        }
        dic.printXMLToFile(outputpath, DicOpts.STD);
    }
}
