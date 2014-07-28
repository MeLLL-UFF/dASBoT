/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.DataLogRule;
import edu.uff.dl.rules.util.SimpleGenerator;
import edu.uff.dl.rules.exception.VariableGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class that generates a rule based on a example and a given Expansion Answer
 * Set. This class has a list of examples and pick a example at random to based
 * the rule on. If you wish to create a rule based on a specific example, you
 * should pass a single-element list.
 * <br><br>
 * This class was designed to generate a set of rules from several examples
 * picked at random. But, for now, it generates only a rule.
 *
 * @author Victor Guimarães
 */
@ComponentAnn(name = "AnswerRule", shortName = "AnswerRule", version = 0.1)
public class AnswerRule implements Component {

    private List<ConcreteLiteral> examples;

    private List<ConcreteLiteral> uncoveredExamples;
    private List<ConcreteLiteral> coveredExamples;

    private List<ConcreteLiteral> answerSet;

    private Random randomGenerator = new Random();

    private Set<DataLogRule> rules;

    private int transitivityDepth = 1;

    /**
     * Constructor without parameters. Needed to load the class from a file
     * (Spring). Just allocates the variables.
     */
    public AnswerRule() {
        this.uncoveredExamples = new ArrayList<>(examples);
        this.coveredExamples = new ArrayList<>();
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param examples the list of examples.
     * @param answerSet the expansion answer set.
     */
    public AnswerRule(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet) {
        this();
        this.examples = examples;
        this.answerSet = answerSet;
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
    public AnswerRule(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet, int transitivityDepth) {
        this(examples, answerSet);
        this.transitivityDepth = transitivityDepth;
    }

    /**
     * Method that does all the rule generation.
     *
     * @throws ComponentInitException in case something goes wrong.
     */
    @Override
    public void init() throws ComponentInitException {
        rules = new HashSet<>();
        rules.add(getRule());
    }

    /**
     * Gets a rule based on a random example.
     *
     * @return the rule.
     */
    public Rule getRule() {
        ConcreteLiteral example = pickExampleAtRandom();

        List<? extends ConcreteLiteral> relevants = getRelevants(example);

        System.out.println("Rule based on example: " + example);
        System.out.println("");

        VariableGenerator v = new SimpleGenerator();

        Map<Term, String> map = new HashMap<>();
        List<Term> terms;
        List<ConcreteLiteral> rel = new ArrayList<>();
        DataLogLiteral l;

        DataLogLiteral s;
        s = new DataLogLiteral(example.getHead(), example.getTerms(), example.isNegative());
        s.setFailed(true);
        if (!relevants.remove(s)) {
            return null;
        }

        for (ConcreteLiteral con : relevants) {
            terms = new ArrayList<>();
            for (Term term : con.getTerms()) {
                if (!map.containsKey(term)) {
                    map.put(term, v.getNextName());
                }

                terms.add(new Constant(map.get(term)));
            }
            l = new DataLogLiteral(con.getHead(), terms, con.isNegative());
            l.setFailed(con.hasFailed());
            rel.add(l);
        }

        terms = new ArrayList<>();
        for (Term term : example.getTerms()) {
            terms.add(new Constant(map.get(term)));
        }
        s = new DataLogLiteral(example.getHead(), terms, example.isNegative());

        s.setFailed(false);

        Rule r = new SafeRule(s, rel);

        return r;
    }

    /**
     * Gets a list of relevant literals for the given example.
     *
     * @param example the example.
     * @return a list of relevant literals.
     */
    private List<? extends ConcreteLiteral> getRelevants(ConcreteLiteral example) {
        if (this.answerSet == null || this.examples == null)
            return null;

        List<ConcreteLiteral> relevants = new ArrayList<>();

        Set<Term> terms = new HashSet<>(example.getTerms());

        for (ConcreteLiteral pred : answerSet) {
            if (pred.isNegative() == example.isNegative() && pred.getHead().equals(example.getHead())
                    && pred.getTerms().size() == terms.size()
                    && terms.containsAll(pred.getTerms())) {

                relevants.add(pred);
            }
        }

        if (relevants.isEmpty())
            return null;

        //getTransitivity(relevants);
        int count = transitivityDepth;
        if (count == 0)
            count = getTransitivity(relevants);
        while (count > 0) {
            if (transitivityDepth == 0) {
                count = getTransitivity(relevants);
            } else {
                getTransitivity(relevants);
                count--;
            }

        }

        return relevants;
    }

    /**
     * Loads the relevant literals by transitivity onto the given relevant
     * collection. The transitivity's deep is based on the
     * {@link #transitivityDepth}.
     *
     * @param relevants the relevant collection
     * @return the number of relevants appended to the given collection.
     */
    private int getTransitivity(Collection<ConcreteLiteral> relevants) {
        List<ConcreteLiteral> append = new ArrayList<>();
        List<Term> t;

        for (ConcreteLiteral pred : relevants) {
            for (ConcreteLiteral unc : answerSet) {

                if (pred.isNegative() == unc.isNegative() && !pred.sameAs(unc) && !relevants.contains(unc)) {
                    t = pred.getTerms();

                    for (Term uncTerm : unc.getTerms()) {
                        if (t.contains(uncTerm)) {
                            append.add(unc);
                            break;
                        }
                    }

                }
            }

        }
        relevants.addAll(append);

        return append.size();
    }

    /**
     * Pick a example at random.
     *
     * @return the example.
     */
    private ConcreteLiteral pickExampleAtRandom() {
        return uncoveredExamples.get(randomGenerator.nextInt(uncoveredExamples.size()));
    }

    /**
     * Getter for a measure based on the relative number of examples covered by
     * the rule.
     *
     * @return the measure.
     */
    public double getMeasure() {
        return coveredExamples.size() / examples.size();
    }

    /**
     * Setter for the examples.
     *
     * @param examples the examples.
     */
    public void setExamples(List<ConcreteLiteral> examples) {
        this.examples = examples;
    }

    /**
     * Setter for the Expansion Answer Set.
     *
     * @param answerSet the Exapansion Answer Set.
     */
    public void setAnswerSet(List<ConcreteLiteral> answerSet) {
        this.answerSet = answerSet;
    }

    /**
     * Getter for the rules.
     *
     * @return the rules.
     */
    public Set<DataLogRule> getRules() {
        return rules;
    }

}
