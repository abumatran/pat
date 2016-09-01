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
import dictools.utils.DictionaryReader;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that implements an only main method for listing all the entries and the
 * corresponding paradigms from a dictionary.
 * @author Miquel Esplà i Gomis
 */
public class RepresentativeSampleOfEntries {
    
    /**
     * Main method that may be call to obtain a representative set of candidates
     * stems/paradigms from the dictionary specified. Main method that may be
     * run to obtain a representative set of candidates from a list of candidates
     * grouped by paradigm, from the whole list of paradigms in the dictionary
     * set by using the option -d. A vocabulary (option -c) is used to count the
     * number of occurrences. The output is saved in the file indicated by using
     * the option -o. Option -t allows to set the number of parallel threads and
     * option f allows to set the proportion of representative candidates to be
     * retrieved from each paradigm. 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option ocorpuspath = parser.addStringOption('c',"corpus");
        CmdLineParser.Option ooutputpath = parser.addStringOption('o',"output");
        CmdLineParser.Option ofilter = parser.addDoubleOption('f',"filter");
        CmdLineParser.Option othreads = parser.addIntegerOption('t',"threads");
        
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
        double filter=(Double)parser.getOptionValue(ofilter,0.0);
        int threads=(Integer)parser.getOptionValue(othreads,1);
        
        if(filter > 1 || filter<=0){
            System.err.println("The value of the paramieter -f (filter) must be"
                    + "a percentage higher than 0.0.");
        }
        
        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic=dicReader.readDic();
        if(dic==null){
            System.err.print("There was an error while reading dictionary in ");
            System.err.println(dicpath);
        }
        else{
            
            Map<String, Set<Candidate>> paradigm_grouped_entries;
            //Gropuing the entries regarding the paradigm in order to create a representative sample
            paradigm_grouped_entries=new HashMap<String, Set<Candidate>>();
            for(Section s: dic.sections){
                for(E e: s.elements){
                    if(!e.isMultiWord()){
                        Candidate c=DicEntry.GetStemParadigm(e);
                        if(c!=null){
                            if(paradigm_grouped_entries.containsKey(c.getParadigm())){
                                paradigm_grouped_entries.get(c.getParadigm()).add(c);
                            }
                            else{
                                Set<Candidate> tmpset=new LinkedHashSet<Candidate>();
                                tmpset.add(c);
                                paradigm_grouped_entries.put(c.getParadigm(),tmpset);
                            }
                        }
                    }
                    else{
                        System.err.println("Entry "+e.toString()+" is a multiword");
                    }
                }
            }
            
            PrintWriter output;
            if(outputpath!=null){
                try{
                    output=new PrintWriter(new FileOutputStream(outputpath));
                } catch(FileNotFoundException ex){
                    System.err.println("Warning: Output file "+outputpath+" could not be found: the results will be printed in the default output.");
                    output=new PrintWriter(System.out);
                } catch(IOException ex){
                    System.err.println("Warning: Error while writting file "+outputpath+": the results will be printed in the default output.");
                    output=new PrintWriter(System.out);
                }
            }else{
                output=new PrintWriter(System.out);
            }
            
            Vocabulary vocabulary=null;
            try{
                vocabulary=new Vocabulary(corpuspath);
            }catch(IOException ex2){
                ex2.printStackTrace(System.err);
                System.exit(-1);
            }
            
            int processed=0;
            for(Set<Candidate> candidateset: paradigm_grouped_entries.values()){
                processed++;
                int numofelements=(int)((double)(candidateset.size())*filter);
                if(numofelements>=1){
                    if(processed%50==0){
                        System.err.print("\rCompleted: "+(double)processed/paradigm_grouped_entries.size());
                    }
                    ThreadPrintRepresentativeCandidates thread=new
                            ThreadPrintRepresentativeCandidates(vocabulary, dic,
                            candidateset, numofelements, output);
                    thread.start();
                    while(ThreadPrintRepresentativeCandidates.GetNumberOfThreads()>=threads);
                }
            }
            output.close();
        }
    }
}
