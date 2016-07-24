/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.evaluation;

import br.uff.dl.rules.rules.Rule;

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

    public final String className = this.getClass().getName();
    
    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        if (rule == null || rule.getBody() == null) {
            return 0.0;
        }

        return positivesCovered - negativesCovered - rule.getBody().size();
    }

}
