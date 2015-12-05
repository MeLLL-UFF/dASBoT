/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.evaluation;

import br.uff.dl.rules.rules.Rule;

/**
 * Class to measure a rule based on the Laplace. The measure function is
 * <br><br> (p + 1) / (p + n + 2)<br><br>
 * where:
 * <br> P is the number of positive examples.
 * <br> p is the number of covered positive examples.
 * <br> N is the number of negative examples.
 * <br> n is the number of covered negative examples.
 *
 * @author Victor Guimarães
 */
public class LaplaceMeasure implements RuleMeasurer {

    public final String className = this.getClass().getName();
    
    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        return ((double) (positivesCovered + 1)) / ((double) (positivesCovered + negativesCovered + 2));
    }

}
