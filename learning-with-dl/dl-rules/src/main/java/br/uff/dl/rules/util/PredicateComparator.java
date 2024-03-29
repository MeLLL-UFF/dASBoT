/*
 * UFF Project Semantic Learning
 */

package br.uff.dl.rules.util;

import br.uff.dl.rules.datalog.ConcreteLiteral;

import java.util.Comparator;

/**
 *
 * @author Victor Guimarães
 */
public class PredicateComparator implements Comparator<ConcreteLiteral> {

    @Override
    public int compare(ConcreteLiteral o1, ConcreteLiteral o2) {
        return o1.getPredicate().compareTo(o2.getPredicate());
    }
    
}
