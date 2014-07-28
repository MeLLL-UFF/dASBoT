/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.util;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import java.util.Comparator;

/**
 *
 * @author Victor
 */
public class ConcreteLiteralComparator implements Comparator<ConcreteLiteral> {

    @Override
    public int compare(ConcreteLiteral o1, ConcreteLiteral o2) {
        if (o1.hasFailed() == o2.hasFailed()) return 0;
        if (o1.hasFailed()) return 1;
        return -1;
    }
    
}
