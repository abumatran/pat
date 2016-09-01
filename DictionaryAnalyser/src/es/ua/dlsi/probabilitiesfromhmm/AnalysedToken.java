/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.probabilitiesfromhmm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author miquel
 */
public final class AnalysedToken {
    
    private String suffix;
    
    private Set<String> tags;
    
    public static AnalysedToken getEOSAnalysedToken(){
        return new AnalysedToken(Lexicon.EOS, new HashSet<String>(
                Arrays.asList(Lexicon.EOS)));
    }

    public String getSuffix() {
        return suffix;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setTags(Set<String> tags) {
        this.tags=tags;
    }

    public AnalysedToken(String suffix, Set<String> tags) {
        setTags(tags);
        setSuffix(suffix);
    }
}
