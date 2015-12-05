/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util.answerpool;

import br.uff.dl.rules.rules.Rule;
import java.util.Comparator;

/**
 * Class to compare rules based on the rule body's size.
 *
 * @author Victor Guimarães
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
