/*
 * UFF Project Semantic Learning
 */

package br.uff.dl.rules.evaluation;

import br.uff.dl.rules.rules.Rule;

/**
 *
 * @author Victor Guimarães
 */
public class F1ScoreMeasure implements RuleMeasurer {

    public final String className = this.getClass().getName();
    
    private double precision(int positivesCovered, int negativesCovered) {
        return (double) positivesCovered / (positivesCovered + negativesCovered);
    }
    
    private double recall(int positives, int positivesCovered) {
        return (double) positivesCovered / positives;
    }
    
    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        return (2 * precision(positivesCovered, negativesCovered) * recall(positives, positivesCovered)) / (precision(positivesCovered, negativesCovered) + recall(positives, positivesCovered)) ;
    }
    
}
