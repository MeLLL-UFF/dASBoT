/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.refinement;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.rules.Rule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRule;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    protected Set<Literal> positiveExamples;
    protected Set<Literal> negativeExamples;
    protected int timeout;
    protected RuleMeasurer ruleMeasure;
    protected PrintStream outStream;

    //Out parametres
    Map<Integer, EvaluatedRule> refinedRules;

    public Refinement() {
        super();
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param args the DReW's arguments.
     * @param dlpContent the DLP's content.
     * @param boundRule a bound rule to refine.
     * @param threshold a threshold improviment.
     * @param positiveExamples a set of positive examples.
     * @param negativeExamples a set of negative examples.
     * @param timeout a timeout to infer each rule.
     * @param ruleMeasure a measurer of rules.
     */
    public Refinement(String[] args, String dlpContent, EvaluatedRule boundRule, double threshold, Set<Literal> positiveExamples, Set<Literal> negativeExamples, int timeout, RuleMeasurer ruleMeasure, PrintStream outStream) {
        super();
        this.args = args;
        this.dlpContent = dlpContent;
        this.boundRule = boundRule;
        this.threshold = threshold;
        this.positiveExamples = positiveExamples;
        this.negativeExamples = negativeExamples;
        this.timeout = timeout;
        this.ruleMeasure = ruleMeasure;

        this.outStream = outStream;
    }

    @Override
    public void run() {
        this.refinedRules = new HashMap<>();
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
        return positiveExamples;
    }

    /**
     * Getter for the set of negative examples.
     *
     * @return the set of negative examples.
     */
    public Set<Literal> getNegativeSamples() {
        return negativeExamples;
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

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getDlpContent() {
        return dlpContent;
    }

    public void setDlpContent(String dlpContent) {
        this.dlpContent = dlpContent;
    }

    public PrintStream getOutStream() {
        return outStream;
    }

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    public void setBoundRule(EvaluatedRule boundRule) {
        this.boundRule = boundRule;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setPositiveExamples(Set<Literal> positiveExamples) {
        this.positiveExamples = positiveExamples;
    }

    public void setNegativeExamples(Set<Literal> negativeExamples) {
        this.negativeExamples = negativeExamples;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setRuleMeasure(RuleMeasurer ruleMeasure) {
        this.ruleMeasure = ruleMeasure;
    }
    
    /**
     * Getter for all the terms from a rule. A Term is any constant or variable
     * that appear on the rule, including its head.
     *
     * @param r the rule.
     * @return a set with all the rule's terms.
     */
    public static Set<Term> getAllTermsFromRule(Rule r) {
        Set<Term> answer = new HashSet<>();
        answer.addAll(r.getHead().getTerms());

        if (r.getBody() == null || r.getBody().isEmpty())
            return answer;

        for (ConcreteLiteral literal : r.getBody()) {
            answer.addAll(literal.getTerms());
        }

        return answer;
    }
    
}
