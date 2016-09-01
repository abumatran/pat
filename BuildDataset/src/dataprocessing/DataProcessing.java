/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessing;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.Pardef;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.SuffixTree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author mespla
 */
public class DataProcessing {

    
    /**
     * Main method that can be called to print the suffix tree
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option ovalidpos = parser.addStringOption('v',"valid-pos");
        CmdLineParser.Option omultiword = parser.addBooleanOption('m',"mulriword");
        CmdLineParser.Option otrainingpercent = parser.addDoubleOption('p',"trainingpercent");
        CmdLineParser.Option otrainoutput = parser.addStringOption("training-output");
        CmdLineParser.Option otestoutput = parser.addStringOption("test-output");
        CmdLineParser.Option ocorpus = parser.addStringOption('c',"corpus");

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
        String trainoutputpath=(String)parser.getOptionValue(otrainoutput,null);
        String testoutputpath=(String)parser.getOptionValue(otestoutput,null);
        String corpusdir=(String)parser.getOptionValue(ocorpus,null);
        Boolean multiword=(Boolean)parser.getOptionValue(omultiword,false);
        Set<String> validpos=new HashSet<>();
        validpos.addAll(Arrays.asList(((String)parser.getOptionValue(ovalidpos,null)).split(",")));
        Double trainpercent=(Double)parser.getOptionValue(otrainingpercent,1.0);

        if(validpos==null){
            System.err.println("The list of valid POS must be provided (use option -v)");
        }
        
        PrintWriter trainoutput;
        if(trainoutputpath!=null){
            try {
                trainoutput = new PrintWriter(trainoutputpath);
            } catch (FileNotFoundException ex) {
                System.err.println("Warning: output file could not be opened; exit will be printed on screen;");
                trainoutput = new PrintWriter(System.out);
            }
        } else{
            System.err.println("Warning: no output file defined; exit will be printed on screen;");
            trainoutput = new PrintWriter(System.out);
        }
        
        
        PrintWriter testoutput;
        if(testoutputpath!=null){
            try {
                testoutput = new PrintWriter(testoutputpath);
            } catch (FileNotFoundException ex) {
                System.err.println("Warning: output file could not be opened; exit will be printed on screen;");
                testoutput = new PrintWriter(System.out);
            }
        } else{
            System.err.println("Warning: no output file defined; exit will be printed on screen;");
            testoutput = new PrintWriter(System.out);
        }

        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        
        Set<String> words_in_corpus=new HashSet();
        try{
            String line;
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(corpusdir));
            while ((line = br.readLine()) != null) {
               words_in_corpus.add(line.trim().split(" ")[1]);
            }       
        }catch(Exception e){
            e.printStackTrace(System.err);
            System.err.println("Error in the format of the corpus.");
            System.exit(-1);
        }
        
        //Building the suffix tree
        SuffixTree tree;
        tree=new Dix2suffixtree(dic).getSuffixTree();
        
        Set<Paradigm> small_pars=new HashSet();
        List<Paradigm> general_pars=new LinkedList();
        
        int min_size_of_paradigm;
        if(trainpercent==1.0)
            min_size_of_paradigm=1;
        else
            min_size_of_paradigm=(int)Math.ceil(1/(1-trainpercent));
        
        //Checking the paradigms that can be used
        for(Pardef p: dic.pardefs.elements){
            Paradigm paradigm=new Paradigm(p, dic);
            if(paradigm.getSuffixes().size()>0){
                if(paradigm.getSuffixes().iterator().next().getLexInfo().size()>0){
                    String category=paradigm.getSuffixes().iterator().next().getLexInfo().get(0);
                    if(validpos.contains(category)){
                        if(paradigm.GetNumberOfEntries(dic,false) < min_size_of_paradigm)
                            small_pars.add(paradigm);
                        else
                            general_pars.add(paradigm);
                    }
                }
            }
        }
        
        //Running through the paradigms to identify the entries to be used to build the data sets
        int n_training_paradigms_paradigm;
        if(trainpercent==1.0)
            n_training_paradigms_paradigm=general_pars.size();
        else
            n_training_paradigms_paradigm=(int)Math.ceil(general_pars.size()*trainpercent);
        System.err.print("Training portion: ");
        System.err.print(trainpercent*100);
        System.err.println("%");
        System.err.print("Number of paradigms that can be used either for trianing or test: ");
        System.err.println(general_pars.size());
        System.err.print("Number of training paradigms: ");
        System.err.println(n_training_paradigms_paradigm);
        
        System.err.println("PRINTING TRAINING CORPUS");
        long seed = System.nanoTime();
        Collections.shuffle(general_pars, new Random(seed));
        List<Paradigm> trainingpars=general_pars.subList(0, n_training_paradigms_paradigm);
        trainingpars.addAll(small_pars);
        BuildDataset(dic, multiword, trainingpars, tree, words_in_corpus, trainoutput);
        trainoutput.close();
        
        System.err.println("PRINTING TEST CORPUS");
        List<Paradigm> testpars=general_pars.subList(n_training_paradigms_paradigm+1,general_pars.size());
        BuildDataset(dic, multiword, testpars, tree, words_in_corpus, testoutput);
        testoutput.close();
    }
    
    /**
     * Method that builds a dataset in JSON using a list of paradigms that
     * should be used in this dataset.
     * @param dic Dictionary to be processed
     * @param multiword Flag to decide if multiword entries should be taken into account
     * @param pars List of paradigms
     * @param tree Suffix tree used to detect the possible candidates for a given surface words
     * @param words_in_corpus Words that appear in the corpus
     * @param output PrintWriter where the output should be written
     */
    public static void BuildDataset(Dictionary dic, boolean multiword,
            List<Paradigm> pars, SuffixTree tree, Set<String> words_in_corpus,
            PrintWriter output){
       
        //Getting all the entries associated to every paradigm
        List<Candidate> candidates=new LinkedList();
        for(Paradigm p: pars){
            candidates.addAll(p.GetRelatedEntries(dic, multiword));
        }
        
        //Getting all the surface forms for every entry associated to every paradigm
        for(Candidate c: candidates){
            Set<String> surfaceforms=c.GetSurfaceForms(dic);
            //Building the collection of candidates for every surface form
            for(String sform: surfaceforms){
                if(words_in_corpus.contains(sform)){
                    JSONObject json=new JSONObject();
                    JSONArray candidatelist=new JSONArray();
                    Set<Candidate> guessedcandidates=tree.SegmentWord(sform);
                    for(Candidate candidate: guessedcandidates){
                        candidatelist.add(candidate.toJSON(dic));
                    }
                    json.put("candidates",candidatelist);
                    json.put("correct_candidate",c.toJSON(dic));
                    json.put("surfaceword",sform);
                    output.println(json.toJSONString());
                }
            }
        }
    }
}
