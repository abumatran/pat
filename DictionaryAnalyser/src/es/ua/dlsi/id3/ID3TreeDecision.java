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

import es.ua.dlsi.querying.GenericDecision;

/**
 * Decision object for the ID3 decision tree. This object extends the
 * <code>GenericDecision</code> class, adding specific data for the structure
 * of the ID3 tree. The possible decisions are accept or reject.
 * @author Miquel Esplà Gomis
 */
public class ID3TreeDecision extends GenericDecision{
    
    /** Parent of the node in which the decision is taken. This variable is used
     when undo is chosen. */
    Node parent;

    /**
     * Method that returns the parent node.
     * @return Returns the parent node
     */
    public Node getParentNode() {
        return parent;
    }

    /**
     * Overloaded constructor that builds the new decision. The new decision
     * involves a reject/accept action on a given surface form. The parent node
     * is saved in order to allow an undo if user chooses this option.
     * @param rejected If the value of this variable is <code>true</code>, the
     * decision is to reject the surface form. Otherwise it is to accept it
     * @param surfaceform Surface form on which the decision is taken
     * @param parent Parent node in the tree (used if undo is needed)
     */
    public ID3TreeDecision(boolean rejected, String surfaceform, Node parent){
        super(rejected,surfaceform);
        this.parent=parent;
    }
}
