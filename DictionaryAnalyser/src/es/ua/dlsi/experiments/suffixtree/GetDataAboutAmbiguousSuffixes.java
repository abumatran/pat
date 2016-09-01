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
import dics.elements.dtd.Pardef;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.Node;
import es.ua.dlsi.suffixtree.SuffixTree;
import es.ua.dlsi.utils.CmdLineParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Class that contains an only main method for running the extraction of the suffix-tree
 * from the surface forms in the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class GetDataAboutAmbiguousSuffixes {

    public static Map<String,Stack<Node>> GetVisitedNodesPerParadigm(Node leaf, 
            Dictionary dic, Boolean notclosedcategories){
        Map<String,Stack<Node>> visited_nodes_branch=new HashMap<String, Stack<Node>>();
        Node curr_pos=leaf;
        while(curr_pos!=null){
            if(curr_pos.getParadigmNames()!=null){
                for(String paradigm: curr_pos.getParadigmNames()){
                    Paradigm p=new Paradigm(paradigm, dic);
                    if(!notclosedcategories || !p.isClosedCategory()){
                        if(visited_nodes_branch.containsKey(paradigm)){
                            //If the node converges with 
                            visited_nodes_branch.get(paradigm).push(curr_pos);
                        }
                        else{
                            Stack<Node> tmp_set=new Stack<Node>();
                            tmp_set.push(curr_pos);
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

        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        System.err.println("Suffix tree built!");
        SuffixTree st=d2s.getSuffixTree();
        Set<Node> leaf_nodes=st.GetLeafsSet();
        System.err.println("Leaf nodes in suffix tree obtained!");
        Map<Node,Map<String,Set<Node>>> hierarchical_nodes=
                new HashMap<Node, Map<String,Set<Node>>>();
        //Building the hierarchi of nodes (a list of the highest nodes involving
        //other nodes in lower positions of the tree with the same paradigm and
        //
        for(Node leaf: leaf_nodes){
            Map<String,Stack<Node>> visited_nodes_branch=
                    GetVisitedNodesPerParadigm(leaf, dic, notclosedcategories);
            for(Map.Entry<String,Stack<Node>> pair: visited_nodes_branch.entrySet()){
                if(pair.getValue().size()>1){
                    Node highest_node_in_tree=pair.getValue().pop();
                    if(hierarchical_nodes.containsKey(highest_node_in_tree)){
                        if(hierarchical_nodes.get(highest_node_in_tree).
                                containsKey(pair.getKey())){
                            hierarchical_nodes.get(highest_node_in_tree).
                                    get(pair.getKey()).addAll(pair.getValue());
                        }
                        else{
                            Set<Node> relatednodes=new HashSet<Node>(pair.getValue());
                            hierarchical_nodes.get(highest_node_in_tree).put(
                                    pair.getKey(), relatednodes);
                        }
                    }
                    else{
                        Set<Node> relatednodes=new HashSet<Node>(pair.getValue());
                        Map<String,Set<Node>> tmp_map=new HashMap<String, Set<Node>>();
                        tmp_map.put(pair.getKey(), relatednodes);
                        hierarchical_nodes.put(highest_node_in_tree,tmp_map);
                    }
                }
            }
        }
        Set<String> non_ambiguous_paradigms=new HashSet<String>();
        for(Pardef p: dic.pardefs.elements){
            Paradigm par=new Paradigm(p, dic);
            if(!par.isClosedCategory()){
                non_ambiguous_paradigms.add(par.getName());
            }
        }
        non_ambiguous_paradigms.add(dicpath);
        int ambiguous_nodes=0;
        for(Map.Entry<Node, Map<String,Set<Node>>> hierarchical_entry: 
                hierarchical_nodes.entrySet()){
            for(Map.Entry<String,Set<Node>> nodes: hierarchical_entry.getValue().entrySet()){
                ambiguous_nodes+=nodes.getValue().size();
                non_ambiguous_paradigms.remove(nodes.getKey());
            }
        }
        int total_number_pars_node=NumberOfPairsNodeSuffix.NumberOfNodes(st).getFirst();
        System.out.print(ambiguous_nodes);
        System.out.print(" ");
        System.out.print(non_ambiguous_paradigms.size());
        System.out.print(" ");
        System.out.println(total_number_pars_node);
    }
}
