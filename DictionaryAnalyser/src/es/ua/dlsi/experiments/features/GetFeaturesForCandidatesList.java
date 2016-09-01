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

package es.ua.dlsi.experiments.features;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.features.FeatureExtractor;
import es.ua.dlsi.features.FeatureSet;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.querying.RankedCandidate;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class GetFeaturesForCandidatesList {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option ocandidates = parser.addStringOption('f',"feature-candidates");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        CmdLineParser.Option opathjavaobjects = parser.addStringOption('p',"path-objects");
        CmdLineParser.Option odumpsuffixtreepath = parser.addStringOption("dump-suffix-tree");
        CmdLineParser.Option osuffixtreeobjectpath = parser.addStringOption("load-suffix-tree-object");

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

        String dictionary=(String)parser.getOptionValue(odictionary,null);
        String candidates=(String)parser.getOptionValue(ocandidates,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String pathjavaobjects=(String)parser.getOptionValue(opathjavaobjects,null);
        String dumpsuffixtreepath=(String)parser.getOptionValue(odumpsuffixtreepath,null);
        String suffixtreeobjectpath=(String)parser.getOptionValue(osuffixtreeobjectpath,null);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);

        BufferedReader br=null;
        if(candidates==null){
            System.err.println("Error: It is necessary to set the path to the file containing the list of words to remove (use opton -w or --words).");
            System.exit(-1);
        }
        else{
            try {
                br = new BufferedReader(new FileReader(candidates));
            } catch (FileNotFoundException ex) {
                System.err.println("Error: file "+candidates+" could not be found.");
                System.exit(-1);
            }
        }
        
        //Preparing output stream
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

        System.err.print("Reading vocabulary... ");
        //Reading the vocabulary
        Vocabulary vocabulary=null;
        try {
            vocabulary=new Vocabulary(vocabularypath);
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: File '"+vocabularypath+"' could not be found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error while reading file '"+vocabularypath+"' could not be found.");
            System.exit(-1);
        }
        System.err.println("done!");

        //Reading the dictionary and generating the set of lexical forms
        DictionaryReader dicReader = new DictionaryReader(dictionary);
        Dictionary dic=dicReader.readDic();
        
        System.err.print("Building suffix tree... ");
        //Building the suffix tree
        ObjectInputStream ois=null;
        Dix2suffixtree d2s=null;
        if(suffixtreeobjectpath!=null){
            try{
                ois=new ObjectInputStream(
                        new FileInputStream(suffixtreeobjectpath));
                d2s=(Dix2suffixtree)ois.readObject();
                ois.close();
            }
            catch(FileNotFoundException fnfe){
                System.err.println("Error while trying to open the suffix tree object");
                ois=null;
            }
            catch(IOException ioe){
                System.err.println("Error while trying to read the suffix tree object");
                ois=null;
            }
            catch(ClassNotFoundException cnfe){
                System.err.println("Error while trying to read the suffix tree object");
                ois=null;
            }
        }
        if(ois==null){
            d2s=new Dix2suffixtree(dic);
            if(dumpsuffixtreepath!=null){
                try{
                    ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(dumpsuffixtreepath));
                    oos.writeObject(d2s);
                    oos.close();
                }
                catch(IOException ioe){
                    System.err.println("Error while trying to write the suffix tree object");
                }
            }
        }
        System.err.println("done!");

        System.err.print("Reading list of training candidates...");
        List<Candidate> stemparlist=new LinkedList<Candidate>();
        try{
            String line;
            while((line=br.readLine())!=null){
                String[] parstem=line.split("\\|");
                try{
                    String stem=parstem[0];
                    String paradigm=parstem[1];
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
        System.err.println("done!");
        
        FeatureExtractor feat_extract=new FeatureExtractor(dic, vocabulary, d2s, pathjavaobjects);
        
        System.err.println("Starting the feature extraction:");
        for(Candidate c: stemparlist){
            System.err.print("Extracting features for ");
            System.err.println(c);
            Set<String> surfaceforms=c.GetSurfaceForms(dic);
            for(String sform: surfaceforms){
                SortedSetOfCandidates ssoc=d2s.CheckNewWord(sform, vocabulary, 
                        pathjavaobjects, null, notclosedcats);
                for(RankedCandidate rc: ssoc.getCandidates()){
                    FeatureSet fs=feat_extract.GetFeatureSet(rc, notclosedcats);
                    StringBuilder sbuilder=new StringBuilder(fs.getStem());
                    sbuilder.append(";");
                    sbuilder.append(fs.getParadigm());
                    sbuilder.append(";");
                    sbuilder.append(fs.getProportionOfEntriesInDictionary());
                    sbuilder.append(";");
                    sbuilder.append(fs.getProportionOfInflections());
                    sbuilder.append(";");
                    sbuilder.append(fs.getProportionOFInflectionsOccurring());
                    sbuilder.append(";");
                    if(rc.contains(c)){
                        sbuilder.append(1);
                    }
                    else{
                        sbuilder.append(0);
                    }
                    pw.println(sbuilder.toString());
                    pw.flush();
                }
            }
        }
        pw.close();
    }
}
