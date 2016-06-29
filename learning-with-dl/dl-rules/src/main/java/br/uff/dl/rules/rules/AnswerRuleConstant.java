/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class that generates a rule based on a example and a given Expansion Answer
 * Set and a Template.
 * <br> This class uses the template to keep constant on the generalized rule.
 * <br> This class has a list of examples and pick a example at random to based
 * the rule on. If you wish to create a rule based on a specific example, you
 * should pass a single-element list.
 * <br><br>
 * This class was designed to generate a set of rules from several examples
 * picked at random. But, for now, it generates only a rule.
 *
 * @deprecated this function is implemented on {@link AnswerRule} by setting the
 * recursive as false.
 *
 * @author Victor Guimarães
 */
public class AnswerRuleConstant extends AnswerRule {

    /**
     * Constructor with all needed parameters.
     *
     * @param examples the list of examples.
     * @param answerSet the expansion answer set.
     */
    public AnswerRuleConstant(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet) {
        super(examples, answerSet, System.out);
    }

    /**
     * Constructor with all needed parameters and a transitivity depth parameter
     * (optional). <br> The transitive depth defines how deep should be consider
     * a transitivity relation between the predicates to it be considered as
     * relevant.
     * <br> The transitivity is setted by default as 1. Increase this value
     * might increase the complexity exponentially.
     *
     * @param examples the list of examples.
     * @param answerSet the expansion answer set.
     * @param transitivityDepth the transitivity depth.
     */
    public AnswerRuleConstant(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet, int transitivityDepth) {
        super(examples, answerSet, transitivityDepth, System.out);
    }

    @Override
    protected Set<? extends ConcreteLiteral> getRelevant(ConcreteLiteral example) {
        Set<? extends ConcreteLiteral> relevants = super.getRelevant(example);

        Iterator<? extends ConcreteLiteral> it = relevants.iterator();

        while (it.hasNext()) {
            if (it.next().getPredicate().equals(example.getPredicate())) {
                it.remove();
            }
        }

        return relevants;
    }

}
