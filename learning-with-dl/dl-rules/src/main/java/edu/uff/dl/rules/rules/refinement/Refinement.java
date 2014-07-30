/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.refinement;

import edu.uff.dl.rules.rules.avaliation.EvaluatedRule;
import edu.uff.dl.rules.rules.avaliation.RuleMeasurer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Literal;

/**
 * Abstract class which defines the behavior a Refinement class should have. The
 * refinement process is essentially take a rule as base and change it to make
 * it better according with some criteria. If the new rule does not improve
 * significantly, according with the threshold, the process is stopped. Also
 * there is a timeout as a safe measure to stop the process in case it takes too
 * long to infer the rules.
 *
 * @author Victor Guimarães
 */
public abstract class Refinement extends Thread {

    //In parametres
    protected String[] args;
    protected String dlpContent;
    protected EvaluatedRule boundRule;
    protected double threshold;
    protected Set<Literal> positiveSamples;
    protected Set<Literal> negativeSamples;
    protected int timeout;
    protected RuleMeasurer ruleMeasure;

    //Out parametres
    Map<Integer, EvaluatedRule> refinedRules;

    /**
     * Constructor with all needed parameters.
     *
     * @param args the DReW's arguments.
     * @param dlpContent the DLP's content.
     * @param boundRule a bound rule to refine.
     * @param threshold a threshold improviment.
     * @param positiveSamples a set of positive examples.
     * @param negativeSamples a set of negative examples.
     * @param timeout a timeout to infer each rule.
     * @param ruleMeasure a measurer of rules.
     */
    public Refinement(String[] args, String dlpContent, EvaluatedRule boundRule, double threshold, Set<Literal> positiveSamples, Set<Literal> negativeSamples, int timeout, RuleMeasurer ruleMeasure) {
        super();
        this.args = args;
        this.dlpContent = dlpContent;
        this.boundRule = boundRule;
        this.threshold = threshold;
        this.positiveSamples = positiveSamples;
        this.negativeSamples = negativeSamples;
        this.timeout = timeout;
        this.ruleMeasure = ruleMeasure;

        this.refinedRules = new HashMap<>();
    }

    @Override
    public void run() {
        refine();
    }

    /**
     * The abstract method that should do all the refinement process. This
     * method should be implemented according with the refinement strategy.
     */
    public abstract void refine();

    /**
     * Getter for the DLP's content.
     *
     * @return the DLP's content.
     */
    public String getDLPContent() {
        return dlpContent;
    }

    /**
     * Getter for the bound rule.
     *
     * @return the bound rule.
     */
    public EvaluatedRule getBoundRule() {
        return boundRule;
    }

    /**
     * Getter for the threshold.
     *
     * @return the threshold.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Getter for the set of positive examples.
     *
     * @return the set of positive examples.
     */
    public Set<Literal> getPositiveSamples() {
        return positiveSamples;
    }

    /**
     * Getter for the set of negative examples.
     *
     * @return the set of negative examples.
     */
    public Set<Literal> getNegativeSamples() {
        return negativeSamples;
    }

    /**
     * Getter for the timeout.
     *
     * @return the timeout.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Getter for the {@link RuleMeasurer}.
     *
     * @return the {@link RuleMeasurer}.
     */
    public RuleMeasurer getRuleMeasure() {
        return ruleMeasure;
    }

    /**
     * Getter for the refined rules. The refined rules are a {@link Map} with
     * all the rules generated in the process. It means, all the intermediary
     * rules created during the process.
     *
     * @return the refined rules.
     */
    public Map<Integer, EvaluatedRule> getRefinedRules() {
        return refinedRules;
    }

}
