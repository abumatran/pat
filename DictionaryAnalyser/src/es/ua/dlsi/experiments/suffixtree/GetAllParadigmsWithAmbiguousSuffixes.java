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

package es.ua.dlsi.experiments.suffixtree;

import dics.elements.dtd.Dictionary;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.Node;
import es.ua.dlsi.suffixtree.SuffixTree;
import es.ua.dlsi.utils.CmdLineParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that contains an only main method for running the extraction of the suffix-tree
 * from the surface forms in the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class GetAllParadigmsWithAmbiguousSuffixes {

    public static Map<String,Set<Node>> GetVisitedNodesPerParadigm(Node leaf, 
            Dictionary dic, Boolean notclosedcategories){
        Map<String,Set<Node>> visited_nodes_branch=new HashMap<String, Set<Node>>();
        Node curr_pos=leaf;
        while(curr_pos!=null){
            if(curr_pos.getParadigmNames()!=null){
                for(String paradigm: curr_pos.getParadigmNames()){
                    Paradigm p=new Paradigm(paradigm, dic);
                    if(!notclosedcategories || !p.isClosedCategory()){
                        if(visited_nodes_branch.containsKey(paradigm)){
                            //If the node converges with 
                            visited_nodes_branch.get(paradigm).add(curr_pos);
                        }
                        else{
                            Set<Node> tmp_set=new LinkedHashSet<Node>();
                            tmp_set.add(curr_pos);
                            visited_nodes_branch.put(paradigm, tmp_set);
                        }
                    }
                }
            }
            curr_pos=curr_pos.GetParent();
        }
        return visited_nodes_branch;
    }
    
    /**
     * Main method that can be called to print the suffix tree
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");
        CmdLineParser.Option onotclosedcategories = parser.addBooleanOption("not-closedcats");
        CmdLineParser.Option oproportions = parser.addBooleanOption('p',"proportions");
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
        boolean notclosedcategories=(Boolean)parser.getOptionValue(onotclosedcategories,false);
        boolean proportions=(Boolean)parser.getOptionValue(oproportions,false);

        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        System.err.println("Suffix tree built!");
        SuffixTree st=d2s.getSuffixTree();
        Map<String,Map<Node,String>> par_suffixes=new HashMap<String, Map<Node, String>>();
        Set<Node> leaf_nodes=st.GetLeafsSet();
        System.err.println("Leaf nodes in suffix tree obtained!");
        int count_repeated_nodes_in_branch=0;
        for(Node leaf: leaf_nodes){
            Map<String,Set<Node>> visited_nodes_branch=
                    GetVisitedNodesPerParadigm(leaf, dic, notclosedcategories);
            if(proportions){
                for(Map.Entry<String,Set<Node>> pair: visited_nodes_branch.entrySet()){
                    if(pair.getValue().size()>1){
                        count_repeated_nodes_in_branch+=pair.getValue().size();
                    }
                }
            }
            else{
                //for(Map.Entry<String,Set<Node>> entry: visited_nodes_branch.entrySet()){
                //    System.err.println(entry.getKey()+": "+entry.getValue().size());
                //}
                //System.err.println("------------------------");
                for(Map.Entry<String,Set<Node>> pair: visited_nodes_branch.entrySet()){
                    if(pair.getValue().size()>1){
                        if(par_suffixes.containsKey(pair.getKey())){
                            for(Node newnode: pair.getValue()){
                                if(!par_suffixes.get(pair.getKey()).containsKey(newnode)){
                                    StringBuilder sb=new StringBuilder();
                                    Node curr_node=newnode;
                                    while(curr_node.GetParent()!=null){
                                        sb.append(curr_node.GetChar());
                                        curr_node=curr_node.GetParent();
                                    }
                                    par_suffixes.get(pair.getKey()).put(newnode, sb.toString());
                                }
                            }
                        }
                        else{
                            Map<Node,String> tmp_map=new HashMap<Node,String>();
                            for(Node newnode: pair.getValue()){
                                StringBuilder sb=new StringBuilder();
                                Node curr_node=newnode;
                                while(curr_node.GetParent()!=null){
                                    sb.append(curr_node.GetChar());
                                    curr_node=curr_node.GetParent();
                                }
                                tmp_map.put(newnode, sb.toString());
                            }
                            par_suffixes.put(pair.getKey(), tmp_map);
                        }
                    }
                }
                for(Map.Entry<String,Map<Node,String>> entry: par_suffixes.entrySet()){
                    System.out.print(entry.getKey());
                    System.out.println(":");
                    for(String first_suffix: entry.getValue().values()){
                        int count=0;
                        StringBuilder sb=new StringBuilder("\t'");
                        sb.append(first_suffix);
                        sb.append("': ");
                        for(String other_suffix: entry.getValue().values()){
                            if(other_suffix.endsWith(first_suffix) && !other_suffix.equals(first_suffix)){
                                count++;
                                sb.append("'");
                                sb.append(other_suffix);
                                sb.append("';");
                            }
                        }
                        if(count>0){
                            System.out.println(sb.toString());
                        }
                    }
                }
            }
        }
        if(proportions){
            int total_number_pars_node=NumberOfPairsNodeSuffix.NumberOfNodes(st).getFirst();
            System.out.print(count_repeated_nodes_in_branch);
            System.out.print(" ");
            System.out.println(total_number_pars_node);
        }
    }
}
