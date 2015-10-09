/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.evaluation;

import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.Rule;

/**
 *
 * @author Victor
 */
public class RelativeCompressionMeasure implements RuleMeasurer {

    public final String className = this.getClass().getName();
    
    private double maxLiterais;

    public RelativeCompressionMeasure() {
    }

    public RelativeCompressionMeasure(double maxLiterais) {
        this.maxLiterais = maxLiterais;
    }

    @Override
    public double getRuleMeasure(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered) {
        return (positivesCovered / positives) - (negativesCovered / negatives) - (rule.getBody().size() / maxLiterais);
    }

    public double getMaxLiterais() {
        return maxLiterais;
    }

    public void setMaxLiterais(double maxLiterais) {
        this.maxLiterais = maxLiterais;
    }

}
