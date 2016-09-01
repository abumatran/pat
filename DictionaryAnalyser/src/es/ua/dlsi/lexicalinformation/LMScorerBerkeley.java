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

package es.ua.dlsi.lexicalinformation;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Miquel Esplà Gomis
 */
public class LMScorerBerkeley {
    private NgramLanguageModel<String> lm;
        
    public LMScorerBerkeley(String lm_file){
        lm = LmReaders.readArrayEncodedLmFromArpa(lm_file, false);
    }
    
    public double Score(String sentence){
        List<String> words = Arrays.asList(sentence.trim().split("\\s+"));
        double logProb = lm.scoreSentence(words);
        return Math.exp((-logProb)*Math.log(10)/words.size());
    }
}
