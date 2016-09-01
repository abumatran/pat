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
import java.util.Arrays;

/**
 * Class that implements a wrapper  
 * @author Miquel Esplà Gomis
 */
public class SentenceAnalyser {
    
    static public String AnalyseSentence(String sentence, String apertium_mode){
        Process apertium_destxt=null;
        Process bash=null;
        StringBuilder analysed=new StringBuilder();
        //In this section, we analyse the sentence using apertium and surround the
        //surface form with variable prefix in order to keep it unknown
        try {
            //Apertium deformatter
            apertium_destxt = new ProcessBuilder("apertium-destxt").start();
        } catch (IOException ex) {
            System.err.println("Error while trying to run apertim-destxt.");
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        try{
            //Apertium analyser
            bash = new ProcessBuilder("bash", apertium_mode).start();
        } catch (IOException ex) {
            System.err.println("Error while trying to run apertim-destxt.");
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        PrintWriter apertium_pw=new PrintWriter(apertium_destxt.getOutputStream());
        BufferedReader apertium_br=new BufferedReader(new InputStreamReader(apertium_destxt.getInputStream()));
        BufferedReader apertium_br_err=new BufferedReader(new InputStreamReader(bash.getErrorStream()));
        apertium_pw.print(sentence);
        apertium_pw.close();
        try {
            String deformatted=apertium_br.readLine();
            PrintWriter bash_pw=new PrintWriter(bash.getOutputStream());
            bash_pw.write(deformatted);
            bash_pw.close();
            BufferedReader bash_br=new BufferedReader(new InputStreamReader(bash.getInputStream()));
            int char_read;
            char[] buffer=new char[deformatted.length()];
            while((char_read=bash_br.read(buffer,0,deformatted.length()))!=-1){
                char[] sub_buffer=Arrays.copyOfRange(buffer, 0, char_read);
                analysed.append(sub_buffer);
            }
            String err;
            while((err=apertium_br_err.readLine())!=null){
                System.err.println(err);
            }
        } catch (IOException ex) {
            System.err.println("Error while trying to run apertim-destxt.");
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        return analysed.toString();
    }
}
