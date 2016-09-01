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

package es.ua.dlsi.experiments.sortedsetofcandidates;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.bilingual.ParadigmRelationship;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.*;

/**
 *
 * @author miquel
 */
public class CheckPositionCandidateForStemPar {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option oleftdic = parser.addStringOption('l',"right-dic");
        CmdLineParser.Option orightdic = parser.addStringOption('r',"left-dic");
        CmdLineParser.Option obilingdic = parser.addStringOption('b',"biling-dic");
        CmdLineParser.Option otestset = parser.addStringOption('t',"test-set");
        CmdLineParser.Option oremove1entry = parser.addBooleanOption("remove-1entrypars");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option oreverse = parser.addBooleanOption("reverse");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        CmdLineParser.Option opathjavaobjects = parser.addStringOption('p',"path-objects");

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
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String testsetfile=(String)parser.getOptionValue(otestset,null);
        boolean remove1entry=(Boolean)parser.getOptionValue(oremove1entry,false);
        boolean reverse=(Boolean)parser.getOptionValue(oreverse,false);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);
        String pathjavaobjects=(String)parser.getOptionValue(opathjavaobjects,null);

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

        //Reading dictionaries
        DictionaryReader dicReader = new DictionaryReader(leftdic);
        Dictionary dic_left=dicReader.readDic();
        dicReader = new DictionaryReader(rightdic);
        Dictionary dic_right=dicReader.readDic();
        Dictionary biling_dic;
        dicReader = new DictionaryReader(bilingdic);
        biling_dic = dicReader.readDic();
        Dix2suffixtree d2s;
        Dictionary dic;
        if(reverse){
            dic=dic_left;
        }
        else{
            dic=dic_right;
        }
        d2s=new Dix2suffixtree(dic);
        
        ParadigmRelationship parrelationship=new ParadigmRelationship(biling_dic,
                dic_left, dic_right, reverse, remove1entry, notclosedcats);

        try{
            BufferedReader br=new BufferedReader(new FileReader(testsetfile));
            String line;
            while ((line=br.readLine())!=null){
                String[] linetest=line.split("\t");
                String surfaceform=linetest[0];
                String otherlang_par=linetest[1];
                String solution=linetest[2];
                SortedSetOfCandidates candidates=d2s.CheckNewWord(surfaceform,
                        vocabulary, pathjavaobjects, 
                        parrelationship.get(otherlang_par),notclosedcats);
                SortedSetOfCandidates candidates2=d2s.CheckNewWord(surfaceform,
                        vocabulary, pathjavaobjects, null, notclosedcats);
                int nobilingpos=-1;
                int bilingpos=-1;
                for(int j=0;j<candidates2.getCandidates().size() && j==-1;j++){
                    for(Candidate c: candidates2.getCandidates().get(j).getCandidates()){
                        if(c.getParadigm().equals(solution)){
                            nobilingpos=j;
                            break;
                        }
                    }
                }
                for(int j=0;j<candidates.getCandidates().size() && j==-1;j++){
                    for(Candidate c: candidates.getCandidates().get(j).getCandidates()){
                        if(c.getParadigm().equals(solution)){
                            bilingpos=j;
                            break;
                        }
                    }
                }
                pw.println(surfaceform+";"+solution+";"+nobilingpos+";"+bilingpos);
                pw.flush();
            }
        } catch(FileNotFoundException ex){
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch(IOException ex){
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        
        pw.close();
    }

}
