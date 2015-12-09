/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.evaluation;

/**
 *
 * @author Victor Guimarães
 */
public class EvaluatedRuleAndSizeComparator extends EvaluatedRuleComparator {

    @Override
    public int compare(EvaluatedRule o1, EvaluatedRule o2) {
        int compare = super.compare(o1, o2);
        if (compare == 0) {
            return Integer.compare(o1.getRule().getBody().size(), o2.getRule().getBody().size());
        } else {
            return -compare;
        }
    }

}
