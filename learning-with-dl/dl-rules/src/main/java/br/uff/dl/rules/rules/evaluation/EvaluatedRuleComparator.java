/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.evaluation;

import java.util.Comparator;

/**
 * Class to compare rules, this class is used to sort the rules from the better
 * to the worst.
 *
 * @author Victor Guimarães
 */
public class EvaluatedRuleComparator implements Comparator<EvaluatedRule> {

    @Override
    public int compare(EvaluatedRule o1, EvaluatedRule o2) {
        if (o1.getMeasure() < o2.getMeasure()) {
            return 1;
        } else if (o1.getMeasure() > o2.getMeasure()) {
            return -1;
        } else {
            return 0;
        }
    }

}
