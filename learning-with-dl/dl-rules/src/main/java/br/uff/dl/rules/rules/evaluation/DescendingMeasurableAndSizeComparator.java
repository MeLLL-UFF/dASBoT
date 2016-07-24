/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.evaluation;

/**
 *
 * @author Victor Guimarães
 */
public class DescendingMeasurableAndSizeComparator extends DescendingMeasurableComparator {

    @Override
    public int compare(MeasurableRule o1, MeasurableRule o2) {
        int compare = super.compare(o1, o2);
        if (compare == 0) {
            return Integer.compare(o1.getRule().getBody().size(), o2.getRule().getBody().size());
        } else {
            return -compare;
        }
    }

}
