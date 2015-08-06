/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.evaluation;

import edu.uff.dl.rules.rules.Rule;

/**
 * Interface to describe a measure function to measure how good is a rule based
 * on a determined criteria.
 *
 * @author Victor Guimarães
 */
public interface RuleMeasurer {

    /**
     * Getter for the measure calculation.
     *
     * @param rule the rule.
     * @param positives the number of positive examples.
     * @param negatives the number of negative examples.
     * @param positivesCovered the number of positive examples covered by the rule.
     * @param negativesCovered the number of negative examples covered by the rule (false positive).
     * @return the measure.
     */
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered);

}
