/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.evaluation;

import br.uff.dl.rules.rules.Rule;

/**
 *
 * @author Victor
 */
public class RelativeCompressionMeasure implements RuleMeasurer {

    public final String className = this.getClass().getName();
    
    private double maxLiterals;

    public RelativeCompressionMeasure() {
    }

    public RelativeCompressionMeasure(double maxLiterals) {
        this.maxLiterals = maxLiterals;
    }

    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        if (rule == null || rule.getBody() == null) {
            return 0.0;
        }
        return (positivesCovered / positives) - (negativesCovered / negatives) - (rule.getBody().size() / maxLiterals);
    }

    public double getMaxLiterals() {
        return maxLiterals;
    }

    public void setMaxLiterals(double maxLiterals) {
        this.maxLiterals = maxLiterals;
    }

}
