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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class that represents a node in the suffix tree.
 * @author Miquel Esplà i Gomis
 */
public class Node implements Serializable{
    /** Map representing the three: for every character in the key, there is a new node */
    private HashMap<Character,Node> children;

    /** Flag that indicates that, going from this node to the root, a suffix can be built */
    private boolean startingsuffix;

    /** Set of names of paradigms that produce the the suffix */
    private Set<String> paradigm;
    
    /** Parent node */
    private Node parent;
    
    /** Character of the node */
    private Character character;
    
    /** List of ambiguous paradigms for the current node. An ambiguous paradigm
     * is a paradigm that generates two suffixes or more suffixes which are respective
     * suffixes. This means that a given wourd could be assigned to the same paradigm
     * cutting in different parts of the word. The paradigms in this list are those
     * that are ambiguous for this node (suffix).
     */
    //private Set<String> ambiguous;
    
    /*public Set<String> getAmbiguousParadigms(){
        return ambiguous;
    }
    
    public void addAnAmbiguousParadigm(String p){
        addParadigmName(p);
        if(ambiguous==null) {
            this.ambiguous=new LinkedHashSet<String>();
        }
        ambiguous.add(p);
    }*/

    /**
     * Method that returns the set of names of the paradigms producing the suffix.
     * @return Returns the set of names of the paradigms producing the suffix.
     */
    public Set<String> getParadigmNames(){
        return paradigm;
    }

    /**
     * Method that adds a new name of paradigms to the list of paradigms generating the suffix.
     * @param name Name of the new paradigm to be added to the list.
     */
    public void addParadigmName(String name){
        if(paradigm==null) {
            this.paradigm=new LinkedHashSet<String>();
        }
        paradigm.add(name.intern());
    }

    /**
     * Constructor of the class.
     */
    public Node(Node parent, Character character){
        this.startingsuffix=false;
        this.paradigm=null;
        this.children=null;
        this.parent=parent;
        this.character=character;
        //this.ambiguous=null;
    }

    /**
     * Method that adds a new clid node to the current node.
     * @param character Character that drives to the next node in the suffix tree.
     * @param n Node to be added
     */
    public void AddChild(char character, Node n){
        if(children==null) {
            this.children=new HashMap<Character, Node>();
        }
        children.put(SuffixTree.GetCharObject(character),n);
    }

    /**
     * Method that returns the map with all the children hanging from the current node.
     * @return Returns a hash map with the characters to be added to the current suffix and the
     * new nodes hanging from them.
     */
    public HashMap<Character, Node> getChildren(){
        return children;
    }

    /**
     * Method that returns a given sub-node given the character which drives to it.
     * @param character Character for choosing the node to return.
     * @return Sub-node chosen.
     */
    public Node getChild(char character){
        if(children==null) {
            return null;
        }
        else {
            return children.get(character);
        }
    }

    /**
     * Method that indicates if the current node is the initial position of a suffix.
     * @return Returns the the value of the variable <code>startingsuffix</code>.
     */
    public boolean isStartingsuffix() {
        return startingsuffix;
    }

    /**
     * Method that sets the value of the variable <code>startingsuffix</code>.
     * @param startingsuffix New value for the variable <code>startingsuffix</code>
     */
    public void setStartingsuffix(boolean startingsuffix) {
        this.startingsuffix = startingsuffix;
    }

    /**
     * Method that adds a new word in the suffix tree. This method adds a new word
     * in the suffix tree by recursively decomposing its suffix. Once all the suffix
     * has been added to the tree, the stem is discarded.
     * @param word The whole word to be added to the tree.
     * @param currentpos The current position in the addition; the addition starts
     * in the last position of the character string and it runs over the word decreasing
     * one position in every iteration until the complete suffix is added to the tree. 
     * @param startingsuffixpos Variable which indicates in which position of the word the suffix starts.
     * @param paradigm Paradigm which generates the surface form to be added.
     * @param debug Flag that indicates if the debug information should be printed.
     */
    public void InsertWord(String word, int currentpos, int startingsuffixpos, String paradigm, boolean debug){
        if(currentpos>=0){
            Node n=getChild(word.charAt(currentpos));
            if(n!=null){
                if(debug) {
                    System.out.println("node "+word.charAt(currentpos)+" found");
                }
                if(startingsuffixpos==currentpos){
                    if(debug) {
                        System.out.println("it is an starting suffix position");
                    }
                    n.setStartingsuffix(true);
                    //n.InsertWord(word, currentpos-1, debug);
                    n.addParadigmName(paradigm);
                }
                else{
                    n.InsertWord(word, currentpos-1, startingsuffixpos, paradigm, debug);
                }
            }
            else {
                if(debug) {
                    System.out.println("node "+word.charAt(currentpos)+" not found");
                }
                Node newnode=new Node(this,word.charAt(currentpos));
                if(startingsuffixpos==currentpos){
                    if(debug) {
                        System.out.println("it is an starting suffix position");
                    }
                    newnode.setStartingsuffix(true);
                    //newnode.InsertWord(word, currentpos-1, debug);
                    newnode.addParadigmName(paradigm);
                }
                else{
                    newnode.InsertWord(word, currentpos-1, startingsuffixpos, paradigm, debug);
                }
                AddChild(word.charAt(currentpos),newnode);
                if(debug) {
                    System.out.println(word.charAt(currentpos)+" added");
                }
            }
        }
    }

    /**
     * Method that returns the parent node of the current node in the suffix tree.
     * @return Returns the parent node of the current node in the tree
     */
    public Node GetParent(){
        return this.parent;
    }
    
    /**
     * Method that returns the character corresponding to the current node in
     * the suffix tree.
     * @return returns the character corresponding to the current node in the
     * suffix tree
     */
    public char GetChar(){
        return this.character;
    }
    
    /**
     * Method that returns the sufix generated by concatenating the characters
     * from the current node to the root node.
     * @return returns the sufix generated by concatenating the characters
     * from the current node to the root node
     */
    public String SufixToRoot(){
        StringBuilder sb=new StringBuilder();
        Node current=this;
        while(current.parent!=null){
            sb.append(current.character);
            current=current.parent;
        }
        return sb.toString();
    }
}
