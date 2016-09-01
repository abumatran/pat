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

/**
 * 
 * @author Miquel Esplà Gomis
 */
public class AnalysedExampleSentence extends ExampleSentence {
    
    private String word;
    
    private String apertium_mode;
    
    private String analysed;
    
    static private String boundary_mark="ÇÇÇÇÇÇ";
    
    public AnalysedExampleSentence(String sentence, String word, String apertium_mode){
        super(sentence);
        this.apertium_mode=apertium_mode;
        this.word=word;
        this.analysed=SentenceAnalyser.AnalyseSentence(SurroundSentenceWord(),
                apertium_mode).replaceAll("\\^\\*"+boundary_mark+word+boundary_mark+"\\$", "\\^\\*"+word+"\\$");
    }
    
    private String SurroundSentenceWord(){
        String newsentence=this.example.replaceAll("^"+word+" ", boundary_mark
                +word+boundary_mark+" ").replaceAll(" "+word+"$", " "+boundary_mark
                +word+boundary_mark).replaceAll(" "+word+" ", " "+boundary_mark
                +word+boundary_mark+" ");
        //String newsentence=this.example.replaceAll("^*"+word+" ", "^"+boundary_mark
        //        +word+boundary_mark+" ").replaceAll(" "+word+"$", " "+boundary_mark
        //        +word+boundary_mark+"$").replaceAll(" "+word+" ", " "+boundary_mark
        //        +word+boundary_mark+" ");
        return newsentence;
    }
    
    public String GetAnalisysWithGivenOption(String option){
        String result=analysed.replaceAll("\\^\\*"+word+"\\$",
                "\\^\\"+option+"\\$");
        return result;
    }
    
    static public String GetAllTags(String analysed){
        String result=analysed.replaceAll("^\\s*\\^[^<]*", "").replaceAll("\\$\\s*\\[\\]$", "").
                replaceAll("\\$\\s*\\^[^<]*", " ");
        return result;
    }
    
    public String GetAnalysis(){
        return analysed;
    }
}
