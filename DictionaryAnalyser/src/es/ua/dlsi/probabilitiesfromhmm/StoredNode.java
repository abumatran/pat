/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.probabilitiesfromhmm;

import es.ua.dlsi.suffixtree.Node;
import java.util.Set;

/**
 *
 * @author miquel
 */
    
public class StoredNode{
    public StoredNode(Node current_node, Set<String> visited_paradigms,
            Set<String> possible_paradigms){
        this.current_node=current_node;
        this.visited_paradigms=visited_paradigms;
        this.possible_paradigms=possible_paradigms;
    }

    public Node current_node;

    public Set<String> visited_paradigms;
    
    public Set<String> possible_paradigms;
}