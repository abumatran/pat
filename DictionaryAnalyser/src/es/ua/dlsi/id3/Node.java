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

package es.ua.dlsi.id3;

import es.ua.dlsi.monolingual.EquivalentCandidates;

/**
 * Node in the ID3 tree. This node class contains the information of a node
 * in an ID3, which is actually a sub-tree.
 * @author Miquel Esplà Gomis.
 */

public class Node {
    /** Static counter of nodes, it is used to generate the node ID */
    static private int nodeidcounter=0;
    
    /** Parent node */
    private Node parent;
    
    /** ID of the node */
    private String id;
    
    /** Node children for the elements including the test attribute */
    public Node true_children;
    
    /** Node children for the elements not including the test attribute */
    public Node false_children;
    
    /** Equivalent candidates corresponding to the node */
    public EquivalentCandidates candidates;
    
    /** Data at this node (before splitting it into the two parts */
    private InstanceCollection data;
    
    /** Entropy of the node */
    private double entropy;
    
    /** Test attribute defining the splitting of the two sub-nodes */
    private String testAttribute;

    /**
     * Default constructor of the class.
     */
    public Node() {
        this.data = null;
        this.entropy=0.0;
        this.parent=null;
        this.true_children=null;
        this.false_children=null;
        this.candidates=null;
        this.testAttribute=null;
        this.id="node"+nodeidcounter;
        nodeidcounter++;
    }
    
    /**
     * Method that returns the ID of the node.
     * @return Returns the ID of the node
     */
    public String getID(){
        return this.id;
    }

    /**
     * Method that sets a new parent for the node.
     * @param parent New parent for the node
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Method that returns the parent of the node.
     * @return Returns the parent of the node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Method that returns the set of equivalent candidates assigned to the node.
     * @return Returns the set of equivalent candidates assigned to the node
     */
    public EquivalentCandidates getCandidate() {
        return candidates;
    }

    /**
     * Method that sets a new set of data for the node.
     * @param data New set of data for the node
     */
    public void setData(InstanceCollection data) {
        this.data = data;
    }

    /**
     * Method that returns the data assigned to the node.
     * @return Returns the data assigned to the node
     */
    public InstanceCollection getData() {
        return data;
    }

    /**
     * Method that sets a new entropy for the node.
     * @param entropy New entropy for the node
     */
    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    /**
     * Method that returns the entropy of the model.
     * @return Returns the entropy of the model
     */
    public double getEntropy() {
        return entropy;
    }

    /**
     * Method that sets the two sub-nodes for the current node.
     * @param t sub-node for the positive-test-attribute node
     * @param f sub-node for the negative-test-attribute node
     */
    public void setChildren(Node t, Node f) {
        this.true_children = t;
        this.false_children = f;
    }

    /**
     * Method that returns the positive-test-attribute sub-node.
     * @return Returns the positive-test-attribute sub-node
     */
    public Node getTrueChildren() {
        return true_children;
    }

    /**
     * Method that returns the negative-test-attribute sub-node.
     * @return Returns the negative-test-attribute sub-node
     */
    public Node getFalseChildren() {
        return false_children;
    }

    /**
     * Method that sets a new test attribute.
     * @param testAttribute New test attribute
     */
    public void setTestAttribute(String testAttribute) {
        this.testAttribute = testAttribute;
    }

    /**
     * Method that returns the test attribute.
     * @return Returns the test attribute
     */
    public String getTestAttribute() {
        return testAttribute;
    }
}
