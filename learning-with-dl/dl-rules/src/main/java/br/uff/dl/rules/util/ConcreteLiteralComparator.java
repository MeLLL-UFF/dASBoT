/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import br.uff.dl.rules.datalog.ConcreteLiteral;

import java.util.Comparator;

/**
 * Class to compare instances of {@link ConcreteLiteral}. Used to sort sets of
 * it alphabetically.
 *
 * @author Victor Guimarães.
 */
public class ConcreteLiteralComparator implements Comparator<ConcreteLiteral> {

    @Override
    public int compare(ConcreteLiteral o1, ConcreteLiteral o2) {
        if (o1.hasFailed() == o2.hasFailed())
            return 0;
        if (o1.hasFailed())
            return 1;
        return -1;
    }

}
