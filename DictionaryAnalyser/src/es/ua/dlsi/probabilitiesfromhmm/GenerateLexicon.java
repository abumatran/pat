/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.probabilitiesfromhmm;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.Node;
import es.ua.dlsi.suffixtree.SuffixTree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author miquel
 */
public class GenerateLexicon {

    static public void AnalyseUsingDictionary(Dictionary dic, String output_path){
        System.err.println("Generating the suffix tree...");
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        SuffixTree suffixtree=d2s.getSuffixTree();
        System.err.println("Suffix tree generated!");
        suffixtree.rootnode.setStartingsuffix(true);
        System.err.println("Building the ambiguity analyser...");
        System.err.println("Ambiguity analyser built!");

        PrintWriter pw;
        if(output_path==null || output_path.equals("")){
            System.err.println("Error: undefined output file could not be found. "
                    + "The output will be redirected to the standard output.");
            pw=new PrintWriter(System.out);
        }
        else{
            try{
                pw=new PrintWriter(output_path);
            }
            catch(FileNotFoundException fnfe){
                System.err.println("Error: output file '"+output_path+"' could not be found. "
                        + "The output will be redirected to the standard output.");
                pw=new PrintWriter(System.out);
            }
        }
        
        pw.println("<NUM>\t<NUM>");
        pw.println("<SENT>\t<SENT>");
        pw.println("<COMA>\t<COMA>");
        pw.println("<LQUEST>\t<LQUEST>");
        pw.println("<LPAR>\t<LPAR>");
        pw.println("<RPAR>\t<RPAR>");
        pw.println("<WEB>\t<WEB>");
        pw.println("<NOTAWORD>\t<NOTAWORD>");
        pw.flush();
        
        Stack<StoredNode> intermediate_states=new Stack<StoredNode>();

        Set<String> init_visited_nodes=new LinkedHashSet<String>();
        Set<String> init_current_paradigms=new LinkedHashSet<String>();
        for(String par: suffixtree.rootnode.getParadigmNames()){
            init_visited_nodes.add(par);
            init_current_paradigms.add(par);
        }
        
        StringBuilder sb=new StringBuilder("<EMPTYSTRING>");
        sb.append("\t");
        for(String p: init_current_paradigms){
            sb.append(p);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length()-1);
        System.out.println(sb.toString());
        
        for(Node child: suffixtree.rootnode.getChildren().values()){
            StoredNode newstorednode=new StoredNode(child, init_visited_nodes,
                    init_current_paradigms);
            intermediate_states.push(newstorednode);
        }
        
        while(!intermediate_states.isEmpty()){
            StoredNode storednode=intermediate_states.pop();
            Node curr_node=storednode.current_node;
            Set<String> visited_nodes=new LinkedHashSet<String>(storednode.visited_paradigms);
            Set<String> current_paradigms=new LinkedHashSet<String>(storednode.possible_paradigms);
            if(curr_node.isStartingsuffix()){
                for(String par: curr_node.getParadigmNames()){
                    if(visited_nodes.contains(par)){
                        current_paradigms.add(par+"|"+curr_node.SufixToRoot());
                    }
                    else{
                        visited_nodes.add(par);
                        current_paradigms.add(par);
                    }
                }
                
                sb=new StringBuilder(curr_node.SufixToRoot());
                if(sb.length()==0)
                    sb.append("<EMPTYSTRING>");
                sb.append("\t");
                for(String p: current_paradigms){
                    sb.append(p);
                    sb.append(" ");
                }
                sb.deleteCharAt(sb.length()-1);
                System.out.println(sb.toString());
            }
            
            if(curr_node.getChildren()!=null){
                for(Node child: curr_node.getChildren().values()){
                    StoredNode newstorednode=new StoredNode(child,
                            visited_nodes, current_paradigms);
                    intermediate_states.push(newstorednode);
                }
            }
        }
        
        pw.close();
    }
    
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option ooutputpath = parser.addStringOption('o',"output");
        CmdLineParser.Option onotclosedcategories = parser.addBooleanOption("not-closedcats");
        //CmdLineParser.Option onotclosedcategories = parser.addBooleanOption("not-closedcats");
        //CmdLineParser.Option onot1wordparadigms = parser.addBooleanOption("not-1word-paradigms");

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
        String outputpath=(String)parser.getOptionValue(ooutputpath,null);
        boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);

        //boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);
        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        
        AnalyseUsingDictionary(dic, outputpath);
    }
}