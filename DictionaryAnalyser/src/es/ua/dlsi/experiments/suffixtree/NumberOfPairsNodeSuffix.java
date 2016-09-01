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
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.suffixtree.Node;
import es.ua.dlsi.suffixtree.SuffixTree;
import es.ua.dlsi.utils.CmdLineParser;
import es.ua.dlsi.utils.Pair;
import java.util.Stack;

/**
 * Class that contains an only main method for running the extraction of the suffix-tree
 * from the surface forms in the dictionary.
 * @author Miquel Esplà i Gomis
 */
public class NumberOfPairsNodeSuffix {

    public static Pair<Integer,Integer> NumberOfNodes(SuffixTree suffixtree){
        int pairs_node_suffix=0;
        int nodes=0;
        Stack<Node> nodestack=new Stack<Node>();
        nodestack.add(suffixtree.rootnode);
        while(!nodestack.isEmpty()){
            Node newnode=nodestack.pop();
            if(newnode.getParadigmNames()!=null){
                nodes++;
                pairs_node_suffix+=newnode.getParadigmNames().size();
            }
            if(newnode.getChildren()!=null)
                nodestack.addAll(newnode.getChildren().values());
        }
        return new Pair(pairs_node_suffix,nodes);
    }
    
    /**
     * Main method that can be called to print the suffix tree
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option odicpath = parser.addStringOption('d',"dictionary");

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

        DictionaryReader dicReader = new DictionaryReader(dicpath);
        Dictionary dic = dicReader.readDic();
        Dix2suffixtree d2s=new Dix2suffixtree(dic);
        SuffixTree suffixtree=d2s.getSuffixTree();
        
        Pair<Integer,Integer> result=NumberOfNodes(suffixtree);
        int pairs_node_suffix=result.getFirst();
        int nodes=result.getSecond();
        
        System.out.print("Number of pairs suffix/node: ");
        System.out.println(pairs_node_suffix);
        System.out.print("Average number of pairs paradigm/node: ");
        System.out.println(pairs_node_suffix/(double)nodes);
    }
}
