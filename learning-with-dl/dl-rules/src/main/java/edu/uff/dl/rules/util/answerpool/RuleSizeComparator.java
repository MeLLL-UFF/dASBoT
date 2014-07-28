/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.util.answerpool;

import edu.uff.dl.rules.rules.Rule;
import java.util.Comparator;

/**
 *
 * @author Victor
 */
public class RuleSizeComparator implements Comparator<Rule> {

    @Override
    public int compare(Rule o1, Rule o2) {
        if (o1.getBody().size() == o2.getBody().size())
            return 0;
        else if (o1.getBody().size() < o2.getBody().size())
            return 1;
        else 
            return -1;
            
    }
    
}
