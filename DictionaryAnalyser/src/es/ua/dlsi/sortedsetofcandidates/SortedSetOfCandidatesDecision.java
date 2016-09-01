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

package es.ua.dlsi.sortedsetofcandidates;

import es.ua.dlsi.querying.GenericDecision;
import es.ua.dlsi.querying.RankedCandidate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class that contains the information about the decision taken about a given
 * surface word while the process of querying a user about the best paradigm for
 * a given surface form.
 * @author Miquel Esplà Gomis
 */
public class SortedSetOfCandidatesDecision extends GenericDecision{
    
    /**
     * List of removed candidates in the list of sorted candidates which have
     * been removed because of this decision.
     */
    Set<RankedCandidate> removedcandidates;
    
    public SortedSetOfCandidatesDecision(SortedSetOfCandidatesDecision decission){
        super(decission);
        for(RankedCandidate rc: decission.removedcandidates)
            this.removedcandidates.add(new RankedCandidate(rc));
    }

    /**
     * Method that returns the list of candidates that have been removed because
     * of the decision.
     * @return Returns the list of removed candidates that have been removed 
     * because of the decision.
     */
    public Set<RankedCandidate> getRemovedcandidates() {
        return removedcandidates;
    }

    /**
     * Overloaded constructor of the class. This overloaded constructor builds
     * the object and defines the surface form for which the decision is taken
     * and the kind of decision (rejection or acceptation)
     * @param rejected If this variable is <code>true</code>, the decision is to
     * reject the surface form; otherwise, it is to accept the surface form
     * @param surfaceform Surface form on which the decision is taken
     */
    public SortedSetOfCandidatesDecision(boolean rejected, String surfaceform){
        super(rejected,surfaceform);
        this.removedcandidates=new LinkedHashSet<RankedCandidate>();
    }

    /**
     * Method that adds a new candidate to the list of removed candidates because
     * of the decision.
     * @param c Candidate to be added to the list of removed candidates
     */
    public void addCandidate(RankedCandidate c){
        this.removedcandidates.add(c);
    }
}
