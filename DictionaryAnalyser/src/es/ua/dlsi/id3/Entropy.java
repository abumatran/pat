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

import es.ua.dlsi.monolingual.EquivalentCandidates;

/**
 * Class that provides a set of methods for computing entropy and entropy gain
 * for a set of instances. This class is used by the ID3 tree to compute the
 * entropy gain when building the tree.
 * @author Miquel Esplà Gomis
 */
public class Entropy {
    
    /** Value of the minimum probability possible to avoid 0.0 in products */
    static public double minimum_probability=Double.MIN_NORMAL;
    
    /**
     * Method that computes the total entropy for a set of instances.
     * @param data Collection of instances for which the entropy will be computed.
     * @return Returns the value of the entropy for a collection of instances.
     */
    public static double ProbabilityMass(InstanceCollection data) {
        // Checking empty set
        if(data.isEmpty()) {
            return 0;
        }

        double total = 0;
        //Getting the summation of all the probabilities in the set
        for(int j = 0; j < data.getInstances().size(); j++) {
            Instance record = data.getInstances().get(j);
            total+=record.probability;
        }
        return total;
    }
    
    /**
     * Method that computes the total entropy for a set of instances.
     * @param data Collection of instances for which the entropy will be computed.
     * @return Returns the value of the entropy for a collection of instances.
     */
    public static double Entropy(InstanceCollection data) {
        double entropy = 0;

        // Checking empty set
        if(data.isEmpty()) {
            return 0;
        }
        
        double probability_mass=ProbabilityMass(data);

        for(EquivalentCandidates instance_class: data.getInstanceClasses()) {
            double count = 0;
            //Getting the probability for a given candidate
            for(int j = 0; j < data.getInstances().size(); j++) {
                Instance record = data.getInstances().get(j);
                if(record.getInstanceClass().equals(instance_class)) {
                    count+=record.probability;
                }
            }

            // Computing the entropy
            double probability = count / probability_mass;
            if(count > 0) {
                entropy += (-1) * probability * (Math.log(probability) / Math.log(2));
            }
        }
        return entropy;
    }

    /**
     * Method that computes the entropy gain for a given node in an ID3 tree.
     * This method computes the entropy gain for a given node in an ID3 tree
     * when choosing the question used to split the set of instances.
     * @param rootEntropy Entropy of the current node of the tree.
     * @param true_entropy Entropy of the true-side sub-tree.
     * @param false_entropy Entropy of the false-side sub-tree.
     * @param true_prob Probability of true sub-tree.
     * @param false_prob Probability of false sub-tree.
     * @return Returns the entropy gain
     */
    public static double calculateGain(double rootEntropy, double root_probability_mass,
            InstanceCollection subset_true, InstanceCollection subset_false) {
        //double gain = rootEntropy -(true_entropy * ((double)true_size/total_size)
        //        +false_entropy * ((double)false_size/total_size));
        double gain = rootEntropy -(Entropy(subset_true) *
                ProbabilityMass(subset_true)/root_probability_mass
                +Entropy(subset_false) * 
                ProbabilityMass(subset_false)/root_probability_mass);
        return gain;
    }
}
