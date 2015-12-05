/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import java.util.Set;
import java.util.SortedSet;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLPStandard;

/**
 * Class originally idealized to extends the DL-Learner language. (Not used yet)
 *
 * @author Victor Guimarães.
 */
@ComponentAnn(name = "PosNegLPRules", shortName = "rulesLP", version = 0.1)
public class PosNegLPRules extends PosNegLPStandard {

    protected Set<AtomTerm> positiveAtoms;
    protected Set<AtomTerm> negativeAtoms;

    // currently uncovered positive examples
    protected Set<AtomTerm> uncoveredPositiveAtoms;

    /**
     * Constructor used in case that positive and negative examples are provided
     * when this component is initialized
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
     * Constructor used in case that positive and negative examples are provided
     * when this component is initialized
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

    /**
     * Constructor used to accord with a super constructor.
     *
     * @param reasoningService
     */
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

    /**
     * Getter for the positive atoms.
     *
     * @return the positive atoms.
     */
    public Set<AtomTerm> getPositiveAtoms() {
        return positiveAtoms;
    }

    /**
     * Setter for the positive atoms.
     *
     * @param positiveAtoms the positive atoms.
     */
    public void setPositiveAtoms(Set<AtomTerm> positiveAtoms) {
        this.positiveAtoms = positiveAtoms;
    }

    /**
     * Getter for the negative atoms.
     *
     * @return the negative atoms.
     */
    public Set<AtomTerm> getNegativeAtoms() {
        return negativeAtoms;
    }

    /**
     * Setter for the negative atoms.
     *
     * @param negativeAtoms the negative atoms.
     */
    public void setNegativeAtoms(Set<AtomTerm> negativeAtoms) {
        this.negativeAtoms = negativeAtoms;
    }

    /**
     * Setter for the unconvered positive atoms.
     *
     * @param uncoveredPositiveAtoms the unconvered positive atoms.
     */
    public void setUncoveredPositiveAtoms(Set<AtomTerm> uncoveredPositiveAtoms) {
        this.uncoveredPositiveAtoms = uncoveredPositiveAtoms;
    }

}
