/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.probabilitiesfromhmm;

import dics.elements.dtd.Dictionary;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.SuffixTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class TextAnalyser {
    
    private StatesFromSurfaceForm statesguesser;
    
    
    public TextAnalyser(Dictionary dic){
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        this.statesguesser=new StatesFromSurfaceForm(dic, d2s.getSuffixTree());
        /*for(Map.Entry<String, Set<String>> e: this.statesguesser.surface_forms_paradigms.entrySet()){
            System.out.print(e.getKey()+"\t");
            for(String s: e.getValue())
                System.out.print(s+" ");
            System.out.println();
        }
        for(Map.Entry<String, Set<String>> e: this.statesguesser.lowercase_surface_forms_paradigms.entrySet()){
            System.out.print(e.getKey()+"\t");
            for(String s: e.getValue())
                System.out.print(s+" ");
            System.out.println();
        }
        System.exit(-1);*/
    }
    
    public TextAnalyser(Dictionary dic, SuffixTree suffixtree){
        this.statesguesser=new StatesFromSurfaceForm(dic, suffixtree);
        /*for(Map.Entry<String, Set<String>> e: this.statesguesser.surface_forms_paradigms.entrySet()){
            System.out.print(e.getKey()+"\t");
            for(String s: e.getValue())
                System.out.print(s+" ");
            System.out.println();
        }
        for(Map.Entry<String, Set<String>> e: this.statesguesser.lowercase_surface_forms_paradigms.entrySet()){
            System.out.print(e.getKey()+"\t");
            for(String s: e.getValue())
                System.out.print(s+" ");
            System.out.println();
        }
        System.exit(-1);*/
    }
    
    public List<String> ObtainLongestSuffixes(String plain_text){
        List<String> analysed_text=new LinkedList<String>();
        String[] words=plain_text.trim().split("\\s+");
        if(words.length>0){
            for(String word: words){
                if(word.matches("\\p{Alpha}+")){
                    analysed_text.add(statesguesser.GetLongestSuffix(word));
                }
                else if(word.matches("[0-9]+([.,][0-9]+)?(e[+-][0-9]+)?")){
                    analysed_text.add(Lexicon.NUM);
                }
                else if(word.matches("[.\\?;:!]")){
                    analysed_text.add(Lexicon.EOS);
                }
                else if(word.matches(",")){
                    analysed_text.add(Lexicon.COMA);
                }
                else if(word.matches("[¿¡]")){
                    analysed_text.add(Lexicon.LQUEST);
                }
                else if(word.matches("[\\(\\[]")){
                    analysed_text.add(Lexicon.LPAR);
                }
                else if(word.matches("[\\)\\]]")){
                    analysed_text.add(Lexicon.RPAR);
                }
                else if(word.matches("[a-z.]+@[a-z.]+")){
                    analysed_text.add(Lexicon.URL);
                }
                else{
                    analysed_text.add(Lexicon.NOW);
                }
            }
        }
        return analysed_text;
    }
    
    public void ObtainTrainingSet(File f, PrintWriter pw){
        try {
            BufferedReader br=new BufferedReader(new FileReader(f));
            String line;
            while((line=br.readLine())!=null){
                String tokenised_line=line.replaceAll("\\s+", " ").trim();
                
                List<AnalysedToken> list=ObtainAnalysedTokens(tokenised_line);
                
                for(AnalysedToken t: list){
                    pw.print(t.getSuffix());
                    pw.print("\t");
                    for(String s: t.getTags()){
                        pw.print(s);
                        pw.print(" ");
                    }
                    pw.println();
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File "+f.getAbsolutePath()+" could not be open.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error in reading in "+f.getAbsolutePath()+".");
            System.exit(-1);
        }
    }
    
    public List<AnalysedToken> ObtainAnalysedTokens(String plain_text){
        List<AnalysedToken> training_set=new LinkedList<AnalysedToken>();
        String[] words=plain_text.trim().split("\\s+");
        if(words.length>0){
            for(String word: words){
                AnalysedToken t;
                if(word.matches("[\\p{Alpha}'-·]+")){
                    t=new AnalysedToken(
                            statesguesser.GetLongestSuffix(word),
                            statesguesser.GetStatesFromSurfaceForm(word));
                }
                else if(word.matches("[0-9]+([.,][0-9]+)?(e[+-][0-9]+)?")){
                    t=new AnalysedToken(Lexicon.NUM,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.NUM)));
                }
                else if(word.matches("[.\\?;:!]")){
                    t=new AnalysedToken(Lexicon.EOS,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.EOS)));
                }
                else if(word.matches(",")){
                    t=new AnalysedToken(Lexicon.COMA,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.COMA)));
                }
                else if(word.matches("[¿¡]")){
                    t=new AnalysedToken(Lexicon.LQUEST,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.LQUEST)));
                }
                else if(word.matches("[\\(\\[]")){
                    t=new AnalysedToken(Lexicon.LPAR,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.LPAR)));
                }
                else if(word.matches("[\\)\\]]")){
                    t=new AnalysedToken(Lexicon.RPAR,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.RPAR)));
                }
                else if(word.matches("[a-z.]+@[a-z.]+")){
                    t=new AnalysedToken(Lexicon.URL,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.URL)));
                }
                else{
                    t=new AnalysedToken(Lexicon.NOW,
                    new LinkedHashSet<String>(Arrays.asList(Lexicon.NOW)));
                }
                training_set.add(t);
            }
        }
        return training_set;
    }
}
