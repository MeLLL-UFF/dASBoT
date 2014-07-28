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
 *
 * @author Victor
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
    
    public abstract void refine();

    public String getDLPContent() {
        return dlpContent;
    }

    public EvaluatedRule getBoundRule() {
        return boundRule;
    }

    public double getThreshold() {
        return threshold;
    }

    public Set<Literal> getPositiveSamples() {
        return positiveSamples;
    }

    public Set<Literal> getNegativeSamples() {
        return negativeSamples;
    }

    public int getTimeout() {
        return timeout;
    }

    public RuleMeasurer getRuleMeasure() {
        return ruleMeasure;
    }

    public Map<Integer, EvaluatedRule> getRefinedRules() {
        return refinedRules;
    }
    
}
