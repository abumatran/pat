/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.probabilitiesfromhmm;

import dics.elements.dtd.Dictionary;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.Node;
import es.ua.dlsi.suffixtree.SuffixTree;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 * @author miquel
 */
public class Lexicon {
    
    static public String NUM="<NUM>";
    static public String EOS="<SENT>";
    static public String COMA="<COMA>";
    static public String LQUEST="<LQUEST>";
    static public String LPAR="<LPAR>";
    static public String RPAR="<RPAR>";
    static public String URL="<WEB>";
    static public String UNK="<UNK>";
    static public String NOW="<NOTAWORD>";
    
    private Set<String> paradigms;
    
    private Map<String,Set<String>> lexicon;
    
    public Set<String> getAmbiguityClass(String suffix){
        return lexicon.get(suffix);
    }
    
    public Set<String> getSuffixesSet(){
        return lexicon.keySet();
    }
    
    public Set<Set<String>> getAllAmbiguityClasses(){
        return new HashSet<Set<String>>(lexicon.values());
    }
    
    public Set<String> getParadigms(){
        return paradigms;
    }
    
    public Lexicon(Dictionary dic){

        lexicon=new TreeMap<String, Set<String>>();
        
        // Generating the suffix tree...
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        SuffixTree suffixtree=d2s.getSuffixTree();
        BuildLexicon(suffixtree);
    }
    
    public Lexicon(SuffixTree suffixtree){
        BuildLexicon(suffixtree);
    }
    
    public final void BuildLexicon(SuffixTree suffixtree){
        // Suffix tree generated
        suffixtree.rootnode.setStartingsuffix(true);
        
        // Adding initial elements to the lexicon:
        Set<String> ambig_class;
        for (String initclasses: Arrays.asList("<NUM>", "<SENT>", "<COMA>",
                "<LQUEST>", "<LPAR>", "<RPAR>", "<WEB>", "<NOTAWORD>")){
            ambig_class=new HashSet<String>();
            ambig_class.add(initclasses);
            lexicon.put(initclasses,ambig_class);
        }
        // Open class: only containing paradigms which only add the emtpy
        //string suffix
        lexicon.put(UNK, suffixtree.rootnode.getParadigmNames());
        
        Stack<StoredNode> intermediate_states=new Stack<StoredNode>();

        Set<String> init_visited_nodes=new LinkedHashSet<String>();
        Set<String> init_current_paradigms=new LinkedHashSet<String>();
        for(String par: suffixtree.rootnode.getParadigmNames()){
            init_visited_nodes.add(par);
            init_current_paradigms.add(par);
        }
        
        for(Node child: suffixtree.rootnode.getChildren().values()){
            StoredNode newstorednode=new StoredNode(child, init_visited_nodes,
                    init_current_paradigms);
            intermediate_states.push(newstorednode);
        }
        
        while(!intermediate_states.isEmpty()){
            StoredNode storednode=intermediate_states.pop();
            Node curr_node=storednode.current_node;
            Set<String> visited_nodes=
                    new LinkedHashSet<String>(storednode.visited_paradigms);
            Set<String> current_paradigms=
                    new LinkedHashSet<String>(storednode.possible_paradigms);
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
                
                lexicon.put(curr_node.SufixToRoot(), current_paradigms);
            }
            
            if(curr_node.getChildren()!=null){
                for(Node child: curr_node.getChildren().values()){
                    StoredNode newstorednode=new StoredNode(child,
                            visited_nodes, current_paradigms);
                    intermediate_states.push(newstorednode);
                }
            }
        }
        for(Set<String> ambiguityclass: lexicon.values()){
            paradigms.addAll(ambiguityclass);
        }
    }
}
