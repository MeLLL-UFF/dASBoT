/*
 * UFF Project Semantic Learning
 */
package edu.uff.expansion.set;

import edu.northwestern.at.utils.SortedArrayList;
import edu.uff.util.SimpleGenerator;
import edu.uff.util.VariableGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import org.semanticweb.drew.dlprogram.model.Variable;

/**
 *
 * @author Victor
 */
@ComponentAnn(name = "AnswerRule", shortName = "AnswerRule", version = 0.1)
public class AnswerRule implements Component {

    private List<ConcreteDataLogPredicate> samples;

    private List<ConcreteDataLogPredicate> uncoveredSamples;
    private List<ConcreteDataLogPredicate> coveredSamples;

    private List<ConcreteDataLogPredicate> answerSet;

    private Random randomGenerator = new Random();

    private Set<DataLogRule> rules;
    
    private int transitivityDepth;

    public AnswerRule() {
        this(null, null);
    }

    public AnswerRule(List<ConcreteDataLogPredicate> samples, List<ConcreteDataLogPredicate> answerSet) {
        this.samples = samples;

        this.uncoveredSamples = new ArrayList<>(samples);
        this.coveredSamples = new ArrayList<>();

        this.answerSet = answerSet;
        this.transitivityDepth = 1;
    }
    
    public AnswerRule(List<ConcreteDataLogPredicate> samples, List<ConcreteDataLogPredicate> answerSet, int transitivityDepth) {
        this(samples, answerSet);
        this.transitivityDepth = transitivityDepth;
    }

    @Override
    public void init() throws ComponentInitException {
        rules = new HashSet<>();
        rules.add(getRule());
    }

    private DataLogRule getRule() {//List<ConcreteDataLogPredicate> relevants, ConcreteDataLogPredicate sample) {
        ConcreteDataLogPredicate sample = sortSample();
        System.out.println("Rule based on sample: " + sample);
        System.out.println("");
        List<? extends ConcreteDataLogPredicate> relevants = getRelevants(sample);

        VariableGenerator v = new SimpleGenerator();
        
        Map<Term, String> map = new HashMap<>();
        List<Term> terms;
        List<ConcreteDataLogPredicate> rel = new ArrayList<>();
        DataLogLiteral l;
        
        DataLogLiteral s;
        s = new DataLogLiteral(sample.getHead(), sample.getTerms(), sample.isNegative());
        s.setFailed(true);
        if (!relevants.remove(s)) {
            return null;
        }
        
        for (ConcreteDataLogPredicate con : relevants) {
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
    
    private List<? extends ConcreteDataLogPredicate> getRelevants(ConcreteDataLogPredicate sample) {
        if (this.answerSet == null || this.samples == null)
            return null;

        List<ConcreteDataLogPredicate> relevants = new ArrayList<>();

        Comparator<Term> comparator = new TermComparator();
        List<Term> terms = new SortedArrayList<>(sample.getTerms(), comparator);

        for (ConcreteDataLogPredicate pred : answerSet) {
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

    private int getTransitivity(Collection<ConcreteDataLogPredicate> relevants) {
        List<ConcreteDataLogPredicate> append = new ArrayList<>();
        List<Term> t;

        for (ConcreteDataLogPredicate pred : relevants) {
            for (ConcreteDataLogPredicate unc : answerSet) {

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

    private ConcreteDataLogPredicate sortSample() {
        return uncoveredSamples.get(randomGenerator.nextInt(uncoveredSamples.size()));
    }

    public double getMeasure() {
        return coveredSamples.size() / samples.size();
    }

    public void setSamples(List<ConcreteDataLogPredicate> samples) {
        this.samples = samples;
    }

    public void setAnswerSet(List<ConcreteDataLogPredicate> answerSet) {
        this.answerSet = answerSet;
    }

    public Set<DataLogRule> getRules() {
        return rules;
    }

}
