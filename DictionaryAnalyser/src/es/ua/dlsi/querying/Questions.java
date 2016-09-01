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

package es.ua.dlsi.querying;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.id3.InstanceCollection;
import es.ua.dlsi.id3.Tree;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.EquivalentCandidates;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class that queries a user to confirm which is the right candidate for a given
 * surface form. Method that queries a user to confirm which is the right
 * candidate for a given surface form. The user can choose the method that will
 * organise the queries by using the option -m and choosing among the options
 * "sorted-list", "id3", or "id3-with-scores". While the first option uses a
 * sorted list for querying, the other two use the ID3 algorithm. While the first
 * option with ID3 considers equiprobables, the second one uses the scores in from
 * the sorted list to assign likelyhoods to the different candidates
 * @author Miquel Esplà Gomis
 */
public class Questions {
    /**
     * Method that performs the process of querying the different surface forms.
     * Method that performs the process of querying the different surface forms
     * @param candidates structure of organised and scored candidates to be checked
     * @param pWriter PrintWriter through which the result is printed
     */
    static public void AskQuestions(GenericCandidateStructure candidates, PrintWriter pWriter){
        int numberofquestions=0;
        long start = System.currentTimeMillis();

        if(candidates==null){
            System.out.println("No possible paradigm found");
            pWriter.append("\t\t0");
        }
        else{
            //System.out.println(candidates.getCandidates().size()+" possible paradigms.");
            int counter=1;
            String formtoask;
            while((formtoask=candidates.getNextSurfaceFormToAsk())!=null){
                try {
                    boolean incorrect=true;
                    numberofquestions++;
                    System.out.print("Is the word '"+formtoask+"' possible? (y=yes, n=no, b=go back): ");
                    while(incorrect){
                        char answer=(char)System.in.read();
                        switch(answer){
                            case 'y':
                                candidates.AcceptForm(formtoask);
                                //System.out.println(formtoask+" accepted ("+candidates.getCandidates().size()+" remaining)");
                                incorrect=false;
                            break;
                            case 'n':
                                candidates.RejectForm(formtoask);
                                //System.out.println(formtoask+" discarded ("+candidates.getCandidates().size()+" remaining)");
                                incorrect=false;
                            break;
                            case 'b':
                                counter--;
                                numberofquestions--;
                                candidates.GoBack();
                                incorrect=false;
                            break;
                            default:
                                System.out.print("You have to type an answer y (yes), n (no) or b (go back):");
                            break;
                        }
                        //answer=(char)System.in.read();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                counter++;
            }
            pWriter.append("\t");
            System.out.println("Chosen steam-paradigm: "+candidates.getSolution().toString());
            pWriter.append("\t");
            pWriter.append(Integer.toString(numberofquestions));
            pWriter.append("\t");
            long elapsed = System.currentTimeMillis() - start;
            pWriter.append(Long.toString(elapsed));
            pWriter.append("\t");
        }
    }
    
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odictionary = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option omode = parser.addStringOption('m',"mode");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        CmdLineParser.Option opathjavaobjects = parser.addStringOption('p',"path-objects");
        CmdLineParser.Option opathwordstoask = parser.addStringOption('w',"words-to-ask");
        
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
        String mode=(String)parser.getOptionValue(omode,null);
        String outputpath=(String)parser.getOptionValue(ooutput,null);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String pathjavaobjects=(String)parser.getOptionValue(opathjavaobjects,null);
        String pathwordstoask=(String)parser.getOptionValue(opathwordstoask,null);
        
        int modecode=0;
        
        if(mode.equals("sorted-list")){
            modecode=0;
        } else if(mode.equals("id3")){
            modecode=1;
        } else if(mode.equals("id3-with-scores")){
            modecode=2;
        } else{
            System.err.println("Error: unknown mode chosedn '"+mode+"'");
            System.err.println("The possible modes are 'sorted-list', 'id3', and"
                    + "'id3-with-scores'");
            System.exit(-1);
        }
        
        if(pathwordstoask==null){
            System.err.println("Error: It is necessary to set the path to the file"
                    + "containing the words to be queried to the user (use opton"
                    + "-w or --words-to-ask).");
            System.exit(-1);
        }
        
        if(dictionary==null){
            System.err.println("Error: It is necessary to set the dictionary path"
                    + "(use opton -d or --dictionary).");
            System.exit(-1);
        }
        
        if(vocabularypath==null){
            System.err.println("Error: It is necessary to set the vocabulary path"
                    + "(use opton -v or --vocabulary).");
            System.exit(-1);
        }

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
        
        //Reading the dictionary and generating the set of lexical forms
        DictionaryReader dicReader = new DictionaryReader(dictionary);
        Dictionary dic=dicReader.readDic();

        //Building the suffix tree
        Dix2suffixtree d2s;
        d2s=new Dix2suffixtree(dic);

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
        
        BufferedReader br=null;
        try{
            br=new BufferedReader(new FileReader(pathwordstoask));
        }
        catch(FileNotFoundException fnf){
            System.err.println("Error: the file containing the words to be asked '"
                    + pathwordstoask +"'could not be found");
            System.exit(-1);
        }
        String word;
        try{
            while((word=br.readLine())!=null){

                GenericCandidateStructure candidate_structure;
                switch(modecode){
                    case 0:
                        candidate_structure=d2s.CheckNewWord(word, vocabulary,
                                pathjavaobjects, null, notclosedcats);
                    break;
                    case 1:
                        Set<Candidate> candidates=new LinkedHashSet<Candidate>();
                        candidates.addAll(d2s.getSuffixTree().SegmentWord(word));
                        Set<String> possiblesurfaceforms=new HashSet<String>();
                        Set<EquivalentCandidates> sf_candidate=new LinkedHashSet<EquivalentCandidates>();
                        for(Candidate c: candidates){
                            if(!notclosedcats || ! c.isClosedCategoryParadigm(dic)){
                                boolean added=false;
                                for(EquivalentCandidates ec: sf_candidate){
                                    if(c.GetSurfaceForms(dic).equals(ec.getSurfaceForms(dic))){
                                        ec.addCandidate(c);
                                        added=true;
                                        break;
                                    }
                                }
                                if(!added){
                                    sf_candidate.add(new EquivalentCandidates(c));
                                    possiblesurfaceforms.addAll(c.GetSurfaceForms(dic));
                                }
                            }
                        }

                        InstanceCollection records;

                        // read in all our data
                        records=new InstanceCollection();
                        records.buildInstances(possiblesurfaceforms,
                                sf_candidate,dic);

                        candidate_structure=new Tree(records);
                    break;
                    case 2:
                        possiblesurfaceforms=new HashSet<String>();
                        //the key of this map is the set of surface forms and the value is the set of paradigms generating them
                        sf_candidate=new HashSet<EquivalentCandidates>();
                        SortedSetOfCandidates ssc=d2s.CheckNewWord(word, vocabulary,
                                pathjavaobjects, null, notclosedcats);
                        for(RankedCandidate qc: ssc.getCandidates()){
                            possiblesurfaceforms.addAll(qc.getSurfaceForms(dic));
                            sf_candidate.add(qc);
                        }

                        // read in all our data
                        records=new InstanceCollection();
                        records.buildInstances(possiblesurfaceforms,
                                sf_candidate,dic);

                        candidate_structure=new Tree(records);
                    break;
                }
                AskQuestions(null, output);
                output.flush();
            }
            output.close();
        }
        catch(IOException ex){
            System.err.println("Error while reading the file containing the "+
                    "words to be asked '"+ pathwordstoask +"'");
        }
    }
}
