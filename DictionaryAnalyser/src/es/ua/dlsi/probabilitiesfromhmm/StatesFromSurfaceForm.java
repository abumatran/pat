/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.probabilitiesfromhmm;

import dics.elements.dtd.Dictionary;
import dics.elements.dtd.E;
import dics.elements.dtd.Section;
import es.ua.dlsi.entries.DicEntry;
import es.ua.dlsi.lexicalinformation.ClosedCategories;
import es.ua.dlsi.monolingual.Candidate;
import es.ua.dlsi.monolingual.Paradigm;
import es.ua.dlsi.suffixtree.Node;
import es.ua.dlsi.suffixtree.SuffixTree;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author miquel
 */
public class StatesFromSurfaceForm {
    
    Map<String,Set<String>> surface_forms_paradigms;
    Map<String,Set<String>> lowercase_surface_forms_paradigms;
    SuffixTree suffixtree;
    
    public static boolean IsAmbiguous(String parname, Node node){
        if(node!=null){
            Node curnode=node.GetParent();
            while(curnode!=null){
                if(curnode.getParadigmNames()!=null){
                    if(curnode.getParadigmNames().contains(parname)){
                        return true;
                    }
                }
                curnode=curnode.GetParent();
            }
            return false;
        }
        else
            return false;
    }
    
    public StatesFromSurfaceForm(Dictionary dic, SuffixTree suffixtree){
        this.suffixtree=suffixtree;

        surface_forms_paradigms=new HashMap<String,Set<String>>();
        lowercase_surface_forms_paradigms=new HashMap<String,Set<String>>();
        for(Section s: dic.sections){
            for(E e: s.elements){
                if(!e.isMultiWord() && !ClosedCategories.isClosedCategory(e, dic)){
                    Candidate candidate=DicEntry.GetStemParadigm(e);
                    if(candidate!=null){
                        Paradigm par=new Paradigm(candidate.getParadigm(), dic);
                        if(!par.isMultiword()){
                            Set<String> surfaceforms=candidate.GetSurfaceForms(dic);
                            for(String form: surfaceforms){
                                try{
                                    String suffix=form.substring(candidate.getStem().length());
                                    Node node=suffixtree.NodeMatchingSuffix(suffix);
                                    if(!node.getParadigmNames().contains(candidate.getParadigm())){
                                        System.err.println("Error: wrong node detction 1.");
                                        System.exit(-1);
                                    }
                                    if(surface_forms_paradigms.containsKey(form)){
                                        if(IsAmbiguous(candidate.getParadigm(),node)){
                                            surface_forms_paradigms.get(form).add(
                                                    candidate.getParadigm()+"|"+suffix);
                                        }
                                        else{
                                            surface_forms_paradigms.get(form).add(
                                                    candidate.getParadigm());
                                        }
                                    }
                                    else{
                                        Set<String> parlist=new HashSet<String>();
                                        if(IsAmbiguous(candidate.getParadigm(),node)){
                                            parlist.add(candidate.getParadigm()+"|"+suffix);
                                        }
                                        else{
                                            parlist.add(candidate.getParadigm());
                                        }
                                        surface_forms_paradigms.put(form, parlist);
                                    }

                                    String lowerform=form.toLowerCase();
                                    suffix=lowerform.substring(candidate.getStem().length());
                                    node=suffixtree.NodeMatchingSuffix(suffix);
                                    if(!lowerform.equals(form)){
                                        if(!lowercase_surface_forms_paradigms.containsKey(
                                                lowerform)){
                                            Set<String> parlist=new HashSet<String>();
                                            if(IsAmbiguous(candidate.getParadigm(),node)){
                                                parlist.add(candidate.getParadigm()+"|"+
                                                        lowerform.substring(
                                                        candidate.getStem().length()));
                                            }
                                            else{
                                                parlist.add(candidate.getParadigm());
                                            }
                                            lowercase_surface_forms_paradigms.put(
                                                    lowerform, parlist);
                                        }
                                        else{
                                            if(IsAmbiguous(candidate.getParadigm(), node)){
                                                lowercase_surface_forms_paradigms.get(
                                                        lowerform).add(candidate.getParadigm()
                                                        +"|"+lowerform.substring(
                                                        candidate.getStem().length()));
                                            }
                                            else{
                                                lowercase_surface_forms_paradigms.get(
                                                        lowerform).add(candidate.getParadigm());
                                            }
                                        }
                                    }
                                }
                                catch(NullPointerException npe){
                                    npe.printStackTrace(System.err);
                                    System.err.println("Error when processing word '"+
                                            surfaceforms+"'");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public Set<String> GetStatesFromSurfaceForm(String surfaceform){
        System.err.print(surfaceform+"\t"+surfaceform.toLowerCase()+"\t");
        if(this.surface_forms_paradigms.containsKey(surfaceform)){
            for (String tag: this.surface_forms_paradigms.get(surfaceform))
                System.err.print(tag+"\t");
            System.err.println();
            return this.surface_forms_paradigms.get(surfaceform);
        }
        else if(this.lowercase_surface_forms_paradigms.containsKey(surfaceform.toLowerCase())){System.err.print(surfaceform+"\t"+surfaceform.toLowerCase()+"\t");
            for (String tag: this.lowercase_surface_forms_paradigms.get(surfaceform.toLowerCase()))
                System.err.print(tag+"\t");
            System.err.println();
            return this.lowercase_surface_forms_paradigms.get(surfaceform.toLowerCase());
        }
        else{
            Set<Candidate> candidates=this.suffixtree.SegmentWord(surfaceform);
            Set<String> states=new HashSet<String>();
            for(Candidate c: candidates){
                if(!c.getReftotree().getParadigmNames().contains(c.getParadigm())){
                    System.err.println("Error: wrong node detction 2.");
                    System.exit(-1);
                }
                if(IsAmbiguous(c.getParadigm(), c.getReftotree())){
                    states.add(c.getParadigm()+"|"+surfaceform.
                            substring(c.getStem().length()));
                }
                else{
                     states.add(c.getParadigm());
                }
            }
            if(states.isEmpty())
                states.add(Lexicon.UNK);
            /*for (String tag: states)
                System.err.print(tag+"\t");
            System.err.println();*/
            return states;
        }
    }
    
    public String GetLongestSuffix(String surfaceform){
        Set<Candidate> candidates=this.suffixtree.SegmentWord(surfaceform);

        int shorteststem=surfaceform.length();
        for(Candidate c: candidates){
            if(c.getStem().length()<shorteststem){
                shorteststem=c.getStem().length();
            }
        }
        if(shorteststem==surfaceform.length())
            return "<EMPTYSTRING>";
        else
            return surfaceform.substring(shorteststem);
    }
    
    public void PrintAllSurfaceformsStates(PrintWriter pw){
        for(Map.Entry<String,Set<String>> form: surface_forms_paradigms.entrySet()){
            pw.print(form.getKey());
            pw.print("\t");
            pw.print(form.getValue().size());
            pw.print("\t");
            StringBuilder sb=new StringBuilder();
            for(String state: form.getValue()){
                sb.append(state);
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length()-1);
            pw.println(sb);
            pw.flush();
        }
        
        for(Map.Entry<String,Set<String>> form:
                lowercase_surface_forms_paradigms.entrySet()){
            pw.print(form.getKey());
            pw.print("\t");
            pw.print(form.getValue().size());
            pw.print("\t");
            StringBuilder sb=new StringBuilder();
            for(String state: form.getValue()){
                sb.append(state);
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length()-1);
            pw.println(sb);
            pw.flush();
        }
    }
}
