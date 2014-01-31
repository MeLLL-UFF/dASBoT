/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uff.dl.rules.example;

/**
 *
 * @author Victor
 */

import java.util.Set;
import java.util.SortedSet;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLPStandard;

@ComponentAnn(name = "PosNegLPRules", shortName = "rulesLP", version = 0.1)
public class PosNegLPRules extends PosNegLPStandard {

    protected Set<AtomTerm> positiveAtoms;
    protected Set<AtomTerm> negativeAtoms;

    // currently uncovered positive examples
    protected Set<AtomTerm> uncoveredPositiveAtoms;

    /**
     * Constructor, used in case that positive and negative examples are
     * provided when this component is initialized
     *
     * @param reasoningService Reasoner, provides reasoning service. Used to
     * checking the instance type
     * @param positiveExamples Set of positive examples
     * @param negativeExamples Set of negative examples
     * @param positiveAtoms Set of positive terms
     * @param negativeAtoms Set of negative terms
     */
    public PosNegLPRules(AbstractReasonerComponent reasoningService, SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples, Set<AtomTerm> positiveAtoms, Set<AtomTerm> negativeAtoms) {
        super(reasoningService, positiveExamples, negativeExamples);
        this.positiveAtoms = positiveAtoms;
        this.negativeAtoms = negativeAtoms;
        this.uncoveredPositiveAtoms = positiveAtoms;
    }

    

    /**
     * Constructor, used in case that positive and negative examples are
     * provided when this component is initialized
     *
     * @param reasoningService Reasoner, provides reasoning service. Used to
     * checking the instance type
     * @param positiveAtoms Set of positive terms
     * @param negativeAtoms Set of negative terms
     */
    public PosNegLPRules(AbstractReasonerComponent reasoningService, Set<AtomTerm> positiveAtoms, Set<AtomTerm> negativeAtoms) {    
        super(reasoningService);
        this.positiveAtoms = positiveAtoms;
        this.negativeAtoms = negativeAtoms;
        this.uncoveredPositiveAtoms = positiveAtoms;
    }

    public PosNegLPRules(AbstractReasonerComponent reasoningService) {
        super(reasoningService);
    }

    /**
     * This constructor can be used by SpringDefinition to create bean object
     * Properties of new bean may be initialised later using setters
     */
    public PosNegLPRules() {
        super();
    }

    public Set<AtomTerm> getPositiveAtoms() {
        return positiveAtoms;
    }

    public void setPositiveAtoms(Set<AtomTerm> positiveAtoms) {
        this.positiveAtoms = positiveAtoms;
    }

    public Set<AtomTerm> getNegativeAtoms() {
        return negativeAtoms;
    }

    public void setNegativeAtoms(Set<AtomTerm> negativeAtoms) {
        this.negativeAtoms = negativeAtoms;
    }

    public void setAtomPositiveAtoms(Set<AtomTerm> atomPositiveAtoms) {
        this.uncoveredPositiveAtoms = atomPositiveAtoms;
    }

}
