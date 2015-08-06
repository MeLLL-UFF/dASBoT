/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.evaluation;

import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.Rule;

/**
 *
 * @author Victor
 */
public class RelativeCompressionMeasure implements RuleMeasurer {

    private double maxLiterais;

    public RelativeCompressionMeasure(double maxLiterais) {
        this.maxLiterais = maxLiterais;
    }

    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        return (positivesCovered / positives) - (negativesCovered / negatives) - (rule.getBody().size() / maxLiterais);
    }

}
