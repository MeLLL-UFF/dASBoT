/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uff.dllearnerensemble;

import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLP;

/**
 *
 * @author Bruno
 */
public class Bagging {
    private AbstractCELA la;
    private PosNegLP lp;
    int bags;
    
    public Bagging(AbstractCELA la, PosNegLP lp, int bags) {
        this.la = la;
        this.lp = lp;
        this.bags = bags;
    }
    
    public void run() {   
        Individual[] individual;
        Random rnd = new Random();
        HashSet<Individual> positiveExamples = new HashSet<Individual>();
        HashSet<Individual> negativeExamples = new HashSet<Individual>();
        HashSet<Individual> bagPositiveExamples = new HashSet<Individual>();
        HashSet<Individual> bagNegativeExamples = new HashSet<Individual>();
        
        positiveExamples.addAll(lp.getPositiveExamples());
        negativeExamples.addAll(lp.getNegativeExamples());
        
        for (int i = 1; i <= bags; i++) {
            System.out.println("Bag: " + i + "\n");
            
            individual = new Individual[positiveExamples.size()];
            positiveExamples.toArray(individual);

            bagPositiveExamples.clear();
            for (Individual ind : individual) {
                bagPositiveExamples.add(individual[rnd.nextInt(individual.length)]);
            }

            individual = new Individual[negativeExamples.size()];
            negativeExamples.toArray(individual);

            bagNegativeExamples.clear();
            for (Individual ind : individual) {
                bagNegativeExamples.add(individual[rnd.nextInt(individual.length)]);
            }
            
            lp.getPositiveExamples().clear();
            lp.getPositiveExamples().addAll(bagPositiveExamples);
            lp.getNegativeExamples().clear();
            lp.getNegativeExamples().addAll(bagNegativeExamples);
            
            try {
                lp.init();
                la.init();
            } catch (ComponentInitException ex) {
                Logger.getLogger(Bagging.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            la.start();
        }
    }
}
