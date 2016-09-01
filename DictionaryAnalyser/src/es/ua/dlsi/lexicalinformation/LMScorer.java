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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 *
 * @author Miquel Esplà Gomis
 */
public class LMScorer {
    private Process scorer;
    
    private BufferedReader scorer_br;
    
    private PrintWriter scorer_pw;
    
    //public BufferedReader scorer_br_err;
    
    public void StartLMScorer(String scorer_bin_path, String lm_file){
        try {
            //Apertium deformatter
            scorer = new ProcessBuilder(scorer_bin_path, lm_file).start();
        } catch (IOException ex) {
            System.err.println("Error while trying to run the LM scorer: "+scorer_bin_path);
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        scorer_br=new BufferedReader(new InputStreamReader(scorer.getInputStream()));
        
        scorer_pw=new PrintWriter(scorer.getOutputStream());
        //scorer_br_err=new BufferedReader(new InputStreamReader(scorer.getErrorStream()));
    }
    
    public double Score(String sentence){
        
        double score=-1;
        //In this section, we analyse the sentence using apertium and surround the
        //surface form with variable prefix in order to keep it unknown
        scorer_pw.println(sentence);
        scorer_pw.flush();
        try {
            String score_txt=scorer_br.readLine();
            System.err.println(score_txt);
            score=Double.parseDouble(score_txt.split(" ")[1].split(":")[1]);
        } catch (IOException ex) {
            System.err.println("Error while trying to run apertim-destxt.");
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        return score;
    }
    
    public void StopLMScorer(){
        try{
            scorer_br.close();
            scorer_pw.close();
        }
        catch(IOException ex){}
        scorer.destroy();
    }
}
