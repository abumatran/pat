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
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.SuffixTree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Class that only contains a main method which gets, for a given word, the list
 * of possible candidates (stem+paradigm).
 * This class contains a main method that expands a pair stem-paradigm in an
 * Apertium's monolingual dictionary by producing all the possible surface forms
 * and, if specified, the lexical information corresponding of each of them
 * @author Miquel Esplà i Gomis
 */
public class CandidatesForWord {

    /**
     * Main method that performs the expansion of the stem-paradigm pair. Main
     * method that expands a pair stem-paradigm in an Apertium's monolingual
     * dictionary by producing all the possible surface forms and, if specified,
     * the lexical information corresponding of each of them
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        CmdLineParser parser = new CmdLineParser();
        //Help command
        CmdLineParser.Option ohelp = parser.addBooleanOption('h',"help");
        //Surface form to be used for expansion
        CmdLineParser.Option osurfaceform = parser.addStringOption('s',"surfaceform");
        //Path to a file containing the list of stems to be used for expansion
        CmdLineParser.Option osurfaceformlistfile = parser.addStringOption("surfaceforms-list");
        //Option for specifying the output file path
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        //Dictionary to be expanded
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        //together with the surface forms obtained
        CmdLineParser.Option oparadigms = parser.addStringOption('p',"paradigms");
        //Valid part-of-speech cathegories that can be taken into account for generating candidates
        CmdLineParser.Option ovalidpos = parser.addStringOption('v',"valid-pos");


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

        boolean help=(Boolean)parser.getOptionValue(ohelp,false);
        String surfaceform=(String)parser.getOptionValue(osurfaceform,null);
        String outputpath=(String)parser.getOptionValue(ooutput,null);
        String surfaceformlistfile=(String)parser.getOptionValue(osurfaceformlistfile,null);
        String dictionary=(String)parser.getOptionValue(odictionary,null);
        String paradigmsfile=(String)parser.getOptionValue(oparadigms,null);
        Set<String> validpos=new HashSet<String>();
        validpos.addAll(Arrays.asList(((String)parser.getOptionValue(ovalidpos,null)).split(",")));
        
        if(help){
            System.err.println("This tool provides the list of stem/paradigm candidates"
                    + "for a given surface form. To use it do:");
            System.err.println("java -cp DictionaryAnalyser.jar"
                    + "es.ua.dlsi.experiments.monolingual.CandidatesForWord [OPTIONS]");
            System.err.println("REQUIERED OPTIONS:");
            System.err.println("\t-d: Apertium dictionary to build the suffix tree");
            System.err.println("\t-s: Surface form to be evaluated; this option"
                    + "can be replaced by option '--surfaceforms-list'");
            System.err.println("\t--surfaceforms-list: File containing a collection"
                    + "of surface forms to be evaluated (one per line)");
            System.err.println("ADDITIONAL OPTIONS:");
            System.err.println("\t-l: If this option is enabled, lexical information"
                    + "is provided for each infleciton");
            System.err.println("\t-o: Path to the output file (standard output by"
                    + "default)");
            System.err.println("\t-c: If this option is enabled, paradigms"
                    + "belonging to closed categories are ignored");
            System.err.println("\t--remove-categories: Comma sepparated list of"
                    + "categories to be ignored");
            System.err.println("\t-p: Path to a file containing a list of"
                    + "paradigms; if this option is enabled, only th eparadigms"
                    + "in this list are taken into account");
            System.err.println("\t-r: A comma sepparated list of direction"
                    + "restrictions (LR, RL, or both) can be set with this option;"
                    + "inflections produced by entries tagged with this"
                    + "restriction will be ignored");
            
            System.exit(0);
        }

        List<String> surfaceform_list=new LinkedList<String>();
        Dictionary dic;

        if(dictionary==null){
            System.err.println("Error: It is necessary to set the dictionary path (use opton -d or --dictionary).");
            System.exit(-1);
        }
        if(surfaceform!=null){
            if(surfaceformlistfile!=null){
                System.err.println("Error: option -s/--stem and option --stem-list are incompatible.");
                System.exit(-1);
            }
            else {
                surfaceform_list.add(surfaceform);
            }
        }
        else{
            if(surfaceformlistfile!=null){
                try {
                    BufferedReader br = new BufferedReader(new FileReader(surfaceformlistfile));
                    String line;
                    while ((line=br.readLine())!=null){
                        surfaceform_list.add(line);
                    }
                } catch (FileNotFoundException ex) {
                    System.err.print("Error: surface form list file '");
                    System.err.print(surfaceformlistfile);
                    System.err.println("' could not be found.");
                    System.exit(-1);
                } catch (IOException ex) {
                    System.err.print("Error while reading file '");
                    System.err.print(surfaceformlistfile);
                    System.err.println("'.");
                    System.exit(-1);
                }
            }
            else{
                System.err.println("Error: It is necessary to set a surface form (use opton -s or --surfaceform)"
                        + "or the path of a file containing a list of surface forms (use opton --surfaceform-list).");
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
        Set<String> acceptedparadigmslist=null;
        if(paradigmsfile!=null){
            acceptedparadigmslist=new HashSet<String>();
            try{
                BufferedReader br=new BufferedReader(new FileReader(paradigmsfile));
                String line;
                while((line=br.readLine())!=null){
                    acceptedparadigmslist.add(line);
                }
            }
            catch(FileNotFoundException ex){
                System.err.println("File "+paradigmsfile+" does not exist. The tool will continue without taking into account this parameter");
                acceptedparadigmslist=null;
            }
            catch(IOException ex){
                System.err.println("File "+paradigmsfile+" could not be read. The tool will continue without taking into account this parameter");
                acceptedparadigmslist=null;
            }
        }
        
        //Reading the dictionary and generating the set of lexical forms
        DictionaryReader dicReader = new DictionaryReader(dictionary);
        dic=dicReader.readDic();

        //Building the suffix tree
        SuffixTree tree;
        tree=new Dix2suffixtree(dic).getSuffixTree();

        if(dic==null){
            System.err.print("There was an error while reading dictionary in ");
            System.err.println(dictionary);
        }
        else{
            for(String sform: surfaceform_list){
                //output.println("=== PARSING NEW SURFACE FORM: "+sform+" ===");
                
                JSONObject json=new JSONObject();
                JSONArray candidatelist=new JSONArray();
                
                Set<Candidate> guessedcandidates=tree.SegmentWord(sform);

                for(Candidate candidate: guessedcandidates){
                    Set<Suffix> candidatesuffixes=candidate.getSuffixes(dic);
                    if(candidatesuffixes.iterator().hasNext()){
                        List<String> lexinfo=candidate.getSuffixes(dic).iterator().next().getLexInfo();
                        if(validpos.isEmpty() || (lexinfo.size() > 0 && validpos.contains(lexinfo.get(0))))
                            candidatelist.add(candidate.toJSON(dic));
                    }
                }
                json.put("candidates",candidatelist);
                json.put("surfaceword",sform);
                output.println(json.toJSONString());
            }
            //output.println();
        }
        output.close();
    }
}
