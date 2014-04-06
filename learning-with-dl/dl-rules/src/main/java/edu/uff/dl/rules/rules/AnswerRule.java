/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.DataLogRule;
import edu.uff.dl.rules.util.SimpleGenerator;
import edu.uff.dl.rules.util.VariableGenerator;
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
 *
 * @author Victor
 */
@ComponentAnn(name = "AnswerRule", shortName = "AnswerRule", version = 0.1)
public class AnswerRule implements Component {

    private List<ConcreteLiteral> samples;

    private List<ConcreteLiteral> uncoveredSamples;
    private List<ConcreteLiteral> coveredSamples;

    private List<ConcreteLiteral> answerSet;

    private Random randomGenerator = new Random();

    private Set<DataLogRule> rules;
    
    private int transitivityDepth = 1;

    public AnswerRule() {
        this(null, null);
    }

    public AnswerRule(List<ConcreteLiteral> samples, List<ConcreteLiteral> answerSet) {
        this.samples = samples;

        this.uncoveredSamples = new ArrayList<>(samples);
        this.coveredSamples = new ArrayList<>();

        this.answerSet = answerSet;
    }
    
    public AnswerRule(List<ConcreteLiteral> samples, List<ConcreteLiteral> answerSet, int transitivityDepth) {
        this(samples, answerSet);
        this.transitivityDepth = transitivityDepth;
    }

    @Override
    public void init() throws ComponentInitException {
        rules = new HashSet<>();
        rules.add(getRule());
    }

    private DataLogRule getRule() {//List<ConcreteDataLogPredicate> relevants, ConcreteDataLogPredicate sample) {
        ConcreteLiteral sample = pickSampleAtRandom();
        System.out.println("Rule based on sample: " + sample);
        System.out.println("");
        List<? extends ConcreteLiteral> relevants = getRelevants(sample);

        VariableGenerator v = new SimpleGenerator();
        
        Map<Term, String> map = new HashMap<>();
        List<Term> terms;
        List<ConcreteLiteral> rel = new ArrayList<>();
        DataLogLiteral l;
        
        DataLogLiteral s;
        s = new DataLogLiteral(sample.getHead(), sample.getTerms(), sample.isNegative());
        s.setFailed(true);
        if (!relevants.remove(s)) {
            return null;
        }
        
        for (ConcreteLiteral con : relevants) {
            terms = new ArrayList<>();
            for (Term term : con.getTerms()) {
                if (! map.containsKey(term)) {
                    map.put(term, v.getNextName());
                }
                
                terms.add(new Constant(map.get(term)));
            }
            l = new DataLogLiteral(con.getHead(), terms, con.isNegative());
            l.setFailed(con.hasFailed());
            rel.add(l);
        }
        
        
        terms = new ArrayList<>();
        for (Term term : sample.getTerms()) {
            terms.add(new Constant(map.get(term)));
        }
        s = new DataLogLiteral(sample.getHead(), terms, sample.isNegative());
        
        s.setFailed(false);

        Rule r = new SafeRule(s, rel);

        return r;
    }
    
    private List<? extends ConcreteLiteral> getRelevants(ConcreteLiteral sample) {
        if (this.answerSet == null || this.samples == null)
            return null;

        List<ConcreteLiteral> relevants = new ArrayList<>();

        Set<Term> terms = new HashSet<>(sample.getTerms());

        for (ConcreteLiteral pred : answerSet) {
            if (pred.isNegative() == sample.isNegative() && pred.getHead().equals(sample.getHead())
                    && pred.getTerms().size() == terms.size()
                    && terms.containsAll(pred.getTerms())) {

                relevants.add(pred);
            }
        }

        if (relevants.isEmpty())
            return null;

        //getTransitivity(relevants);
        int count = transitivityDepth;
        if (count == 0) count = getTransitivity(relevants);
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

    private ConcreteLiteral pickSampleAtRandom() {
        return uncoveredSamples.get(randomGenerator.nextInt(uncoveredSamples.size()));
    }

    public double getMeasure() {
        return coveredSamples.size() / samples.size();
    }

    public void setSamples(List<ConcreteLiteral> samples) {
        this.samples = samples;
    }

    public void setAnswerSet(List<ConcreteLiteral> answerSet) {
        this.answerSet = answerSet;
    }

    public Set<DataLogRule> getRules() {
        return rules;
    }

}
