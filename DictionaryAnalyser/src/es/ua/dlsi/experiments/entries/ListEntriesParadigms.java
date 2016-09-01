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

package es.ua.dlsi.experiments.entries;

import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Class that implements an only main mehtod for listing all the entries and the
 * corresponding paradigms from a dictionary.
 * @author Miquel Esplà i Gomis
 */
public class ListEntriesParadigms {

    /**
     * Main method that may be call to obtain the list of stems/paradigms from the
     * dictionary specified.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dicpath=null;
        PrintWriter output=null;
        switch(args.length){
            case 1:
                dicpath = args[0];
                System.err.println("Warning: no output file defined; exit will be printed on screen;");
                output = new PrintWriter(System.out);
                break;
            case 2:
                dicpath = args[0];
                try {
                    output = new PrintWriter(args[1]);
                } catch (FileNotFoundException ex) {
                    System.err.println("Warning: output file could not be opened; exit will be printed on screen;");
                    output = new PrintWriter(System.out);
                }
                break;
            default:
                System.err.println("Error: Wrong number of parameters. Application must be called:");
                System.err.println("\tjava -cp DictionaryAnalyser es.ua.dlsi.entries.ListEntriesParadigms <dictionary_path> [<output_file>]");
                System.exit(-1);
        }
        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic=dicReader.readDic();
        if(dic==null){
            System.err.print("There was an error while reading dictionary in ");
            System.err.println(args[0]);
        }
        else{
            for(Section s: dic.sections){
                for(E e: s.elements){
                    String stem=null;
                    String par=null;
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
                        else if(de instanceof L){
                            if(stem==null) {
                                stem=((L)de).getValueNoTags();
                            }
                            else{
                                stem=null;
                                break;
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
                        output.print(stem);
                        output.print(":");
                        output.println(par);
                    }
                }
            }
        }
        output.close();
    }
}
