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

package es.ua.dlsi.querying;

import es.ua.dlsi.monolingual.EquivalentCandidates;
import java.util.Stack;

/**
 * Abstract class for managing a set of candidates stem/paradigm that allows
 * iteratively querying an oracle to determine which of the possible candidates
 * is the right one. This is an abstract class defining the basic elements of
 * any structure for helping a user (or any other oracle) determining the best
 * candidate stem/paradigm in a list of stem/paradigm candidates. The class
 * contains a set of functions for getting a surface form to be queried to the
 * oracle and a function for accepting/rejecting the surface form. In addition,
 * all the forms queried and the decision taken by the oracle about them are
 * stored in a stack and, therefore, undo operations can be performed during the
 * process of querying
 * @author Miquel Esplà Gomis
 */
public abstract class GenericCandidateStructure {
    
    /**
     * Stack of accepted/rejected surface forms and the repercussion that those
     * decision has on the structure. Stack of accepted/rejected surface forms
     * and the repercussion that those decision has on the structure
     */
    protected Stack<GenericDecision> operations;
    
    public GenericCandidateStructure(GenericCandidateStructure structure){
        this.operations=(Stack<GenericDecision>)structure.operations.clone();
    }
    
    /**
     * Default constructor of the class. This constructor only initialises the
     * stack of operations
     */
    public GenericCandidateStructure(){
        operations=new Stack<GenericDecision>();
    }
    
    /**
     * Method that undoes the last operation stored in the stack. Method that
     * undoes the last operation stored in the stack
     */
    abstract public void GoBack();
    
    /**
     * Method that validates a surface word form queried by the structure. Method
     * that validates a surface word form queried by the structure
     * @param form Surface word form to be validated
     */
    abstract public void AcceptForm(String form);
    
    /**
     * Method that invalidates a surface word form queried by the structure.
     * Method that invalidates a surface word form queried by the structure
     * @param form 
     */
    abstract public void RejectForm(String form);
    
    /**
     * Method that returns the next surface form to be asked to the oracle.
     * Method that returns the next surface form to be queried to the oracle in 
     * a sequence of queries
     * @return Returns the next surface form to be asked to the oracle
     */
    abstract public String getNextSurfaceFormToAsk();
    
    /**
     * Method that returns the solution (the most likely candidate) at a certain
     * point of the querying process. Method that returns the solution (the most
     * likely candidate) at a certain point of the querying process
     * @return Returns the most likely candidate at a certain point of the
     * querying process
     */
    abstract public EquivalentCandidates getSolution();
}
