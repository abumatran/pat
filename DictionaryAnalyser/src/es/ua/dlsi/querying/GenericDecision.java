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

/**
 * Generic decision object, representing a decision taken about a given surface
 * word form abut rejecting it or validating it. This generic decision is aimed
 * at storing the information related to a decision about validating or rejecting
 * a given surface word form queried by a <code>GenericCandidateStructure</code>
 * object to an oracle
 * @author Miquel Esplà Gomis
 */
public class GenericDecision{
    /**
     * Flag that is <code>true</code> if the decision is to reject a given surface
     * word form and <code>false</code> if it is to accept it
     */
    boolean rejected;

    /**
     * Surface word forms on which the decision is taken
     */
    String surfaceform;

    /**
     * Function that indicates if the decision is about rejecting a word or not.
     * Function that indicates if the decision is about rejecting a word or not
     * @return The method returns <code>true</code> if the decision is about
     * rejecting a surface word form and <code>false</code> if it is about
     * accepting it
     */
    public boolean isRejected() {
        return rejected;
    }

    /**
     * Method that returns the surface word form on which the decision is taken.
     * Method that returns the surface word form on which the decision is taken
     * @return Returns the surface word form on which the decision is taken
     */
    public String getSurfaceform() {
        return surfaceform;
    }

    /**
     * Default constructor of the class.
     * Default constructor of the class
     * @param rejected Flag indicating if the decision is to reject the surface
     * word form or if it is to accept it
     * @param surfaceform Surface word form on which the decision is taken
     */
    public GenericDecision(boolean rejected, String surfaceform){
        this.rejected=rejected;
        this.surfaceform=surfaceform;
    }
    
    public GenericDecision(GenericDecision decision){
        this.rejected=decision.rejected;
        this.surfaceform=decision.surfaceform;
    }
}
