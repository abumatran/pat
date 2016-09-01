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

package es.ua.dlsi.suffixtree;

import es.ua.dlsi.monolingual.Candidate;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Class that implements a suffix tree. The suffix tree implemented in this class
 * can be used to get the possible candidates generating a surface form.
 * @author Miquel Esplà i Gomis
 */
public class SuffixTree implements Serializable{
    /** Root node of the suffix tree */
    public Node rootnode;

    /** Map between the characters in the tree and the memory position. This variable
     is intended to be useful in terms of eficiency when building the tree.*/
    static private HashMap<Character,Character> chars=new HashMap<Character,Character>();

    /**
     * Class constructor.
     */
    public SuffixTree(){
        rootnode=new Node(null,null);
    }

    /**
     * Method that, given a character, returns the memory position in the characters map.
     * This method is intended to minimise the ammount of memory used by the tree.
     * To do so, every time that a new character is to be added to the tree, instead
     * of adding a new character object, a reference to a map with characters is added.
     * @param character Character from which the memory position is to be get.
     * @return Position in memory of the character to be added to the tree.
     */
    static public Character GetCharObject(char character){
        Character c=chars.get(character);
        if(c==null){
            c=new Character(character);
            chars.put(c,c);
            return c;
        }
        else {
            return c;
        }
    }

    /**
     * Method that adds a new word to the tree.
     * @param word Word to be added to the tree
     * @param startingsuffixpos Position of the word in which the suffix starts
     * @param paradigm Paradigm which generates this word
     */
    public void AddWord(String word, int startingsuffixpos, String paradigm){
        if(startingsuffixpos==word.length()){
            rootnode.addParadigmName(paradigm);
            //rootnode.InsertWord(word, word.length()-1, false);
        }
        else {
            rootnode.InsertWord(word, word.length()-1, startingsuffixpos, paradigm, false);
        }
    }

    /**
     * Method that prints the tree in the standard output.
     */
    public void Print(){
        if(rootnode.getChildren()!=null){
            for(char character: rootnode.getChildren().keySet()){
                Node n=rootnode.getChild(character);
                if(n.isStartingsuffix()){
                    System.out.println("+--"+character);
                    if(n.getChildren()!=null) {
                        Print(character+"","|  +--", n);
                    }
                }
                else if(n.getChildren()!=null) {
                    Print(character+"","", n);
                }
                else {
                    System.out.println("+--"+character);
                }
            }
        }
    }

    /**
     * Method that prints a part of the suffix tree. This method prints a part of
     * the tree and is only used by the main <code>Print()</code> method.
     * @param prefix Preffix to add to the new line which will be print.
     * @param treemark Mark of the tree.
     * @param node Current node in the algorighm for printing the tree.
     */
    private void Print(String prefix, String treemark, Node node){
        for(char character: node.getChildren().keySet()){
            Node n=node.getChild(character);
            if(n.isStartingsuffix()){
                System.out.println(treemark+prefix+character);
                if(n.getChildren()!=null) {
                    Print(prefix+character,("|  "+treemark), n);
                }
            }
            else if(n.getChildren()!=null) {
                Print(prefix+character,(treemark), n);
            }
            else {
                System.out.println(treemark+prefix+character);
            }
        }
    }

    /**
     * Method that segments a given word in all the possible combinations stem/suffix
     * given the suffixes in the tree.
     * @param word Word to be segmented.
     * @return Returns a set of candidates stem/paradigm.
     */
    public Set<Candidate> SegmentWord(String word) {
        Set<Candidate> exit=new LinkedHashSet<Candidate>();
        if(this.rootnode.getParadigmNames()!=null){
            for(String s: this.rootnode.getParadigmNames()){
                Candidate candidate=new Candidate(word,s);
                candidate.setReftotree(this.rootnode);
                exit.add(candidate);
            }
        }
        SegmentWord(word, word.length()-1, exit, this.rootnode);
        return exit;
    }
    
    /**
     * Method that returns the node matching a given suffix. Method that returns
     * the node matching a given suffix.
     * @param suffix Suffix to search in the tree.
     * @return Returns the node matching a given suffix
     */
    public Node NodeMatchingSuffix(String suffix) {
        Node currnode=this.rootnode;
        for(int spos=suffix.length()-1; currnode!=null && spos>=0; spos--){
            currnode=currnode.getChild(suffix.charAt(spos));
        }
        return currnode;
    }

    /**
     * Sub method for segmenting a word in all the possible candidates.
     * This method is used by <code>SegmentWord(String word)</code> in a recursive
     * fashion in order to build all the possible candidates stem/paradigm from
     * a given surface form.
     * @param word
     * @param currentposition
     * @param steamparadigm
     * @param currentnode 
     */
    private void SegmentWord(String word, int currentposition,
            Set<Candidate> steamparadigm, Node currentnode) {
        if(currentposition>=0){
            Node node=currentnode.getChild(word.charAt(currentposition));
            if(node!=null){
                SegmentWord(word,currentposition-1,steamparadigm,node);
                if(node.isStartingsuffix() && node.getParadigmNames()!=null){
                    for(String s: node.getParadigmNames()){
                        String stem=word.substring(0,currentposition);
                        //System.out.println(word.substring(0, currentposition)+"/"+word.charAt(currentposition)+": "+s);
                        /*Inflections inf=new Inflections(steam,s);
                        Set<StringBuilder> inflected_words=GenerateAllInflexions(steam,s,dic);
                        for(StringBuilder inflected_word: inflected_words){
                            inf.addInflection(inflected_word.toString());
                        }
                        boolean inserted=false;
                        for(Inflections prev_inf: inflections){
                            if(prev_inf.CompareInflections(inf)){
                                prev_inf.addParadigm(steam,s);
                                inserted=true;
                                break;
                            }
                        }
                        if(!inserted)
                            inflections.add(inf);*/
                        Candidate ncandidate=new Candidate(stem,s);
                        ncandidate.setReftotree(node);
                        steamparadigm.add(ncandidate);
                    }
                }
            }
        }
    }
    
    /**
     * Method that returns the list of leaf nodes in the tree.
     * Method that runs all over the suffix tree and returns a set containing
     * all the leaf nodes (those without descendents).
     * @return Returns a set of leaf nodes
     */
    public Set<Node> GetLeafsSet(){
        Set<Node> exit=new LinkedHashSet<Node>();
        Stack<Node> nodes_to_visit=new Stack<Node>();
        
        nodes_to_visit.add(this.rootnode);
        while(!nodes_to_visit.isEmpty()){
            Node current_node=nodes_to_visit.pop();
            if(current_node.getChildren()==null){
                exit.add(current_node);
            }
            else{
                nodes_to_visit.addAll(current_node.getChildren().values());
            }
        }
        return exit;
    }
}
