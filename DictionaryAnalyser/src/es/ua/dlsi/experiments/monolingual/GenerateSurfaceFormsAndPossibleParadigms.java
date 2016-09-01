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

package es.ua.dlsi.experiments.monolingual;

import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.monolingual.Candidate;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that implements an only main mehtod for listing all the entries and the
 * corresponding paradigms from a dictionary.
 * @author Miquel Esplà i Gomis
 */
public class GenerateSurfaceFormsAndPossibleParadigms {

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
        Map<String,Set<String>> surface_forms_paradigms=new HashMap<String,Set<String>>();
        if(dic==null){
            System.err.print("There was an error while reading dictionary in ");
            System.err.println(args[0]);
        }
        else{
            for(Section s: dic.sections){
                for(E e: s.elements){
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    if(candidate!=null){
                        Set<String> surfaceforms=candidate.GetSurfaceForms(dic);
                        for(String form: surfaceforms){
                            if(!surface_forms_paradigms.containsKey(form)){
                                Set<String> parlist=new HashSet<String>();
                                parlist.add(candidate.getParadigm());
                                surface_forms_paradigms.put(form, parlist);
                            }
                            else{
                                surface_forms_paradigms.get(form).add(candidate.getParadigm());
                            }
                        }
                    }
                }
            }
        }
        for(Map.Entry<String,Set<String>> entry: surface_forms_paradigms.entrySet()){
            output.print(entry.getKey());
            output.print(": ");
            StringBuilder sb=new StringBuilder();
            for(String paradigm: entry.getValue()){
                sb.append(paradigm);
                sb.append(", ");
            }
            output.println(sb.substring(0, sb.length()-2));
            output.flush();
        }
        output.close();
    }
}
