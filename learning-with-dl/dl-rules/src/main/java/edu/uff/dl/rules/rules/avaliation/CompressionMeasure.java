/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.avaliation;

import edu.uff.dl.rules.rules.Rule;

/**
 * Class to measure a rule based on its size, it means, the number of literals
 * on the rule's body. The measure function is <br><br>(p - n - L)<br><br>
 * where:
 * <br> p is the number of covered positive examples.
 * <br> n is the number of covered negative examples.
 * <br> L is the number of literals on the rule's body.
 *
 * @author Victor Guimarães
 */
public class CompressionMeasure implements RuleMeasurer {

    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        return positivesCovered - negativesCovered - rule.getBody().size();
    }

}
