/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.evaluation;

import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.Rule;

/**
 * Class to measure a rule based on the Inverted Laplace. The measure function
 * is <br><br>(N - n) / (P + n) - (p + n - 2)<br><br>
 * where:
 * <br> P is the number of positive examples.
 * <br> p is the number of covered positive examples.
 * <br> N is the number of negative examples.
 * <br> n is the number of covered negative examples.
 *
 * @author Victor Guimarães
 */
public class InvertedLaplaceMeasure implements RuleMeasurer {

    public final String className = this.getClass().getName();
    
    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        return ((double) (negatives - negativesCovered + 1))
                / ((double) ((positives + negativesCovered) - (positivesCovered + negativesCovered - 2)));
    }

}
