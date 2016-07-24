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
public class DescendingMeasurableComparator implements Comparator<MeasurableRule> {

    @Override
    public int compare(MeasurableRule o1, MeasurableRule o2) {
        return -Double.compare(o1.getMeasure(), o2.getMeasure());
    }

}
