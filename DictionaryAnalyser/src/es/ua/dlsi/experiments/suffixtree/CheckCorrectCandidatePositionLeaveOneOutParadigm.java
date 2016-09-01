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

package es.ua.dlsi.experiments.suffixtree;

import dics.elements.dtd.*;
import dictools.utils.DictionaryReader;
import es.ua.dlsi.bilingual.ParadigmRelationship;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.lexicalinformation.LexicalForms;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.monolingual.Suffix;
import es.ua.dlsi.querying.Vocabulary;
import es.ua.dlsi.sortedsetofcandidates.NotInListException;
import es.ua.dlsi.sortedsetofcandidates.SortedSetOfCandidates;
import es.ua.dlsi.suffixtree.Dix2suffixtree;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that implements the methods for performing an experiment for 
 * @author miquel
 */
public class CheckCorrectCandidatePositionLeaveOneOutParadigm {
    
    public static boolean LookForMostAdequateSubParadigm(List<String> lexicalinfo,
            int initpos, String paradigm, Dictionary dic){
        Pardef pdef=dic.pardefs.getParadigmDefinition(paradigm);
        for(E e: pdef.elements){
            int partialpos=-1;
            for(DixElement de: e.children){
                if(de instanceof P){
                    R r=((P)de).r;
                    int pos=initpos;
                    boolean correct=true;
                    for(DixElement subde: r.children){
                        if(subde instanceof S){
                            S s=(S)subde;
                            if(!lexicalinfo.get(pos).equals(s.name)){
                                correct=false;
                                break;
                            }
                        }
                        pos++;
                    }
                    if(correct){
                        if(pos==lexicalinfo.size()) {
                            return true;
                        }
                        else {
                            partialpos=pos;
                        }
                    }
                }
                else if(de instanceof Par){
                    if(partialpos>-1){
                        return LookForMostAdequateSubParadigm(lexicalinfo, partialpos,
                                paradigm, dic);
                    }
                }
            }
        }
        return false;
    }

    public static String LookForMostAdequateParadigm(List<String> lexicalinfo,
            Set<String> paradigms, Dictionary dic){
        for(String p: paradigms){
            Pardef pdef=dic.pardefs.getParadigmDefinition(p);
            for(E e: pdef.elements){
                int partialpos=-1;
                for(DixElement de: e.children){
                    if(de instanceof P){
                        R r=((P)de).r;
                        int pos=0;
                        boolean correct=true;
                        for(DixElement subde: r.children){
                            if(subde instanceof S){
                                S s=(S)subde;
                                if(!lexicalinfo.get(pos).equals(s.name)){
                                    correct=false;
                                    break;
                                }
                            }
                            pos++;
                        }
                        if(correct){
                            if(pos==lexicalinfo.size()) {
                                return p;
                            }
                            else {
                                partialpos=pos;
                            }
                        }
                    }
                    else if(de instanceof Par){
                        if(partialpos>-1){
                            boolean correct=LookForMostAdequateSubParadigm(
                                    lexicalinfo, partialpos, p, dic);
                            if(correct) {
                                return p;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String GetParadigmsFromLexForm(String lemma, List<String> lexinfo,
            Dictionary dic, LexicalForms lf){
        for(Section s: dic.sections){
            for(E e: s.elements){
                String stem=null;
                String par=null;
                for(DixElement de: e.children){
                    if(de instanceof P){
                        stem=((P)de).r.getValueNoTags();
                    }
                    else if(de instanceof I){
                        stem=((I)de).getValueNoTags();
                    }
                    else if(de instanceof Par){
                        par=((Par)de).name;
                    }
                }
                if(stem!=null && par!=null){
                    Pardef p=dic.pardefs.getParadigmDefinition(par);
                    for(DixElement depar: p.elements.get(0).children){
                        if(depar instanceof P){
                            StringBuilder newlemma=new StringBuilder(stem);
                            newlemma.append(((P)depar).r.getValueNoTags());
                            if(lemma.equals(newlemma.toString())){
                                boolean possible=true;
                                List<String> newlexinfo=new LinkedList<String>();
                                for(DixElement delexinfo: ((P)depar).r.children){
                                    if(delexinfo instanceof S){
                                        String symbol=((S)delexinfo).name;
                                        newlexinfo.add(symbol);
                                    }
                                }
                                for(int i=0;i<lexinfo.size();i++){
                                    if(!lexinfo.get(i).equals(newlexinfo.get(i))){
                                        possible=false;
                                        break;
                                    }
                                }
                                if(possible){
                                    return par;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static String GetCounterParadigmFromLexForm(String lemma, Paradigm par,
            List<String> lexinfo, Dictionary biling_dic, Dictionary monoling_dic,
            LexicalForms lf, boolean reverse){
        for(Section sec : biling_dic.sections) {
            for (E e : sec.elements) {
                if(!e.isMultiWord()){
                    for(DixElement de: e.children){
                        if(de instanceof I){
                            I i=(I)de;
                            String newstem=i.getValueNoTags();
                            if(newstem.equals(lemma)){
                                List<String> lstmp=new LinkedList<String>();
                                boolean possible=true;
                                int counter=0;
                                for(DixElement lexinfode: i.children){
                                    if(lexinfode instanceof S){
                                        String symbol=((S)lexinfode).name;
                                        if(!lexinfo.get(counter).equals(symbol)){
                                            possible=false;
                                            break;
                                        }
                                        counter++;
                                    }
                                }
                                if(possible){
                                    for(DixElement lexinfode: i.children){
                                        if(lexinfode instanceof S){
                                            String symbol=((S)lexinfode).name;
                                            if(symbol.equals("GD")) {
                                                lstmp.add("m");
                                            }
                                            else if(symbol.equals("ND")) {
                                                lstmp.add("sg");
                                            }
                                            else {
                                                lstmp.add(symbol);
                                            }
                                        }
                                    }
                                    int lexnodes;
                                    List<String> finallexinfo=new LinkedList<String>();
                                    for(lexnodes=0;lexnodes<lstmp.size();lexnodes++) {
                                        finallexinfo.add(lstmp.get(lexnodes));
                                    }
                                    for(;lexnodes<lexinfo.size();lexnodes++) {
                                        finallexinfo.add(lexinfo.get(lexnodes));
                                    }
                                    String result=lf.GetParadigmFromLexForm(((I)i).getValueNoTags(), finallexinfo);
                                    //String result=LookForMostAdequateParadigm(lstmp,lpar,monoling_dic);
                                    if(result!=null) {
                                        return result;
                                    }
                                }
                            }
                        } else if(de instanceof P){
                            if(reverse){
                                String newstem=((P)de).l.getValueNoTags();
                                if(newstem.equals(lemma)){
                                    List<String> lstmp=new LinkedList<String>();
                                    boolean possible=true;
                                    int counter=0;
                                    for(DixElement lexinfode: ((P)de).l.children){
                                        if(lexinfode instanceof S){
                                            String symbol=((S)lexinfode).name;
                                            if(!lexinfo.get(counter).equals(symbol)){
                                                possible=false;
                                                break;
                                            }
                                            counter++;
                                        }
                                    }
                                    if(possible){
                                        for(DixElement lexinfode: ((P)de).r.children){
                                            if(lexinfode instanceof S){
                                                String symbol=((S)lexinfode).name;
                                                if(symbol.equals("GD")) {
                                                    lstmp.add("m");
                                                }
                                                else if(symbol.equals("ND")) {
                                                    lstmp.add("sg");
                                                }
                                                else {
                                                    lstmp.add(symbol);
                                                }
                                            }
                                        }
                                        int lexnodes;
                                        List<String> finallexinfo=new LinkedList<String>();
                                        for(lexnodes=0;lexnodes<lstmp.size();lexnodes++) {
                                            finallexinfo.add(lstmp.get(lexnodes));
                                        }
                                        for(;lexnodes<lexinfo.size();lexnodes++) {
                                            finallexinfo.add(lexinfo.get(lexnodes));
                                        }
                                        String result=lf.GetParadigmFromLexForm(((P)de).r.getValueNoTags(), finallexinfo);
                                        //String result=LookForMostAdequateParadigm(lstmp,lpar,monoling_dic);
                                        if(result!=null) {
                                            return result;
                                        }
                                    }
                                }
                            }
                            else{
                                String newstem=((P)de).r.getValueNoTags();
                                if(newstem.equals(lemma)){
                                    List<String> lstmp=new LinkedList<String>();
                                    boolean possible=true;
                                    int counter=0;
                                    for(DixElement lexinfode: ((P)de).r.children){
                                        if(lexinfode instanceof S){
                                            String symbol=((S)lexinfode).name;
                                            if(!lexinfo.get(counter).equals(symbol)){
                                                possible=false;
                                                break;
                                            }
                                            counter++;
                                        }
                                    }
                                    if(possible){
                                        for(DixElement lexinfode: ((P)de).l.children){
                                            if(lexinfode instanceof S){
                                                String symbol=((S)lexinfode).name;
                                                if(symbol.equals("GD")) {
                                                    lstmp.add("m");
                                                }
                                                else if(symbol.equals("ND")) {
                                                    lstmp.add("sg");
                                                }
                                                else {
                                                    lstmp.add(symbol);
                                                }
                                            }
                                        }
                                        int lexnodes;
                                        List<String> finallexinfo=new LinkedList<String>();
                                        for(lexnodes=0;lexnodes<lstmp.size();lexnodes++) {
                                            finallexinfo.add(lstmp.get(lexnodes));
                                        }
                                        for(;lexnodes<lexinfo.size();lexnodes++) {
                                            finallexinfo.add(lexinfo.get(lexnodes));
                                        }
                                        String result=lf.GetParadigmFromLexForm(((P)de).l.getValueNoTags(), finallexinfo);
                                        //String result=LookForMostAdequateParadigm(lexinfo,lpar,monoling_dic);
                                        if(result!=null) {
                                            return result;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option oleftdic = parser.addStringOption('l',"right-dic");
        CmdLineParser.Option orightdic = parser.addStringOption('r',"left-dic");
        CmdLineParser.Option obilingdic = parser.addStringOption('b',"biling-dic");
        CmdLineParser.Option oremove1entry = parser.addBooleanOption("remove-1entrypars");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option oreverse = parser.addBooleanOption("reverse");
        CmdLineParser.Option onotclosedcats = parser.addBooleanOption("remove-closedcats");
        CmdLineParser.Option ovocabularypath = parser.addStringOption('v',"vocabulary");
        CmdLineParser.Option opathjavaobjects = parser.addStringOption('p',"path-objects");

        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        String rightdic=(String)parser.getOptionValue(orightdic,null);
        String leftdic=(String)parser.getOptionValue(oleftdic,null);
        String bilingdic=(String)parser.getOptionValue(obilingdic,null);
        String output=(String)parser.getOptionValue(ooutput,null);
        String vocabularypath=(String)parser.getOptionValue(ovocabularypath,null);
        String pathjavaobjects=(String)parser.getOptionValue(opathjavaobjects,null);
        boolean remove1entry=(Boolean)parser.getOptionValue(oremove1entry,false);
        boolean reverse=(Boolean)parser.getOptionValue(oreverse,false);
        boolean notclosedcats=(Boolean)parser.getOptionValue(onotclosedcats,false);

        //Preparing output stream
        PrintWriter pw;
        if(output!=null){
            try{
                pw=new PrintWriter(output);
            } catch(FileNotFoundException ex){
                System.err.println("Error while traying to write output file '"+output+"'.");
                pw=new PrintWriter(System.out);
            }
        } else{
            System.err.println("Warning: output file not defined. Output redirected to standard output.");
            pw=new PrintWriter(System.out);
        }

        //Reading the vocabulary
        Vocabulary vocabulary=null;
        try {
            vocabulary=new Vocabulary(vocabularypath);
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: File '"+vocabularypath+"' could not be found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("Error while reading file '"+vocabularypath+"' could not be found.");
            System.exit(-1);
        }

        DictionaryReader dicReader = new DictionaryReader(leftdic);
        Dictionary dic_left=dicReader.readDic();
        dicReader = new DictionaryReader(rightdic);
        Dictionary dic_right=dicReader.readDic();
        Dictionary biling_dic;
        dicReader = new DictionaryReader(bilingdic);
        biling_dic = dicReader.readDic();
        LexicalForms lf_left=new LexicalForms(dic_left);
        LexicalForms lf_right=new LexicalForms(dic_right);
        Dix2suffixtree d2s;
        Dictionary dic;
        if(reverse){
            dic=dic_left;
        }
        else{
            dic=dic_right;
        }
        d2s=new Dix2suffixtree(dic);
        if(d2s==null){
            System.err.println("There was a problem when creating the suffix tree. Check the dictionaries provided.");
            System.exit(-1);
        }

        //Loop that goes all over the entries of the dictionary
        for(Section s: dic.sections){
            for(int i=0;i<s.elements.size();i++){
                E e=s.elements.remove(i);
                //If the entry is a multiword is discarded
                if(e.isMultiWord()){
                    System.err.println("Multiword: "+e.toString());
                }
                else{
                    //Getting the stema nd paradign of the entry
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    if(candidate.getStem()!=null && candidate.getParadigm()!=null){
                        
                        String stem=candidate.getStem();
                        String bestsurfaceform;
                        List<String> bestlexinfo;
                        Pardef p= dic.pardefs.getParadigmDefinition(candidate.getParadigm());
                        Paradigm paradigm=new Paradigm(p, dic);
                        //If indicated, entries generating forms from a closed category may be discarded
                        if(!notclosedcats || !paradigm.isClosedCategory()){
                            //Choosing the most frequent surface form in the vocabulary
                            bestsurfaceform=vocabulary.GetMostFrequentSurfaceForm(stem, paradigm);
                            //If no one of the surface forms appear in the vocabulary:
                            if(bestsurfaceform==null){
                                System.err.println("Warning: no occurrence for word with stem "
                                        +stem+" and paradigm "+paradigm.getName());
                                //Random form
                                bestsurfaceform=stem+paradigm.getSuffixes().iterator().next().getSuffix();
                            }
                            //The lexical information of one of the suffixes generating the surface form is taken randomly
                            Map<String,Set<Suffix>> expansion=candidate.GetExpansion(dic);
                            Set<Suffix> suffixes_generating_bestform=expansion.get(bestsurfaceform);
                            bestlexinfo=suffixes_generating_bestform.iterator().next().getLexInfo();
                            
                            String lemma=candidate.GetLemma(dic);
                            //If the lemma cannot be found, the system stops working
                            if(candidate.GetLemma(dic) ==null){
                                System.err.println("Error: lemma cannot be generated for stem "+stem+
                                        " and paradigm "+paradigm.getName());
                                System.exit(-1);
                            }
                            String counterpar;
                            if(reverse) {
                                counterpar=GetCounterParadigmFromLexForm(lemma, paradigm,
                                        bestlexinfo, biling_dic, dic_right, lf_right, reverse);
                            }
                            else {
                                counterpar=GetCounterParadigmFromLexForm(lemma, paradigm,
                                        bestlexinfo, biling_dic, dic_left, lf_left, reverse);
                            }

                            ParadigmRelationship parrelationship=new
                                    ParadigmRelationship(biling_dic, lf_left,
                                    lf_right, reverse, remove1entry, notclosedcats);
                            SortedSetOfCandidates candidates=d2s.CheckNewWordCatLex(
                                    bestsurfaceform, vocabulary, pathjavaobjects,
                                    parrelationship.get(counterpar), notclosedcats);
                            SortedSetOfCandidates candidates2=d2s.CheckNewWordCatLex(
                                    bestsurfaceform, vocabulary, pathjavaobjects,
                                    null, notclosedcats);
                            try{
                                int nobilingpos=candidates2.GetCandidatePosition(candidate);
                                int bilingpos=candidates.GetCandidatePosition(candidate);
                                pw.println(stem+";"+paradigm.getName()+";"+nobilingpos+";"+bilingpos);
                                s.elements.add(i, e);
                            }catch(NotInListException ex){
                                System.err.println("Warning: candidate "+candidate.toString()+" is not in the sorted list of candidates.");
                            }
                        }
                    }
                    else{
                        System.err.println("Closed category: "+e.toString());
                    }
                }
            }
        }
        pw.close();
    }
}
