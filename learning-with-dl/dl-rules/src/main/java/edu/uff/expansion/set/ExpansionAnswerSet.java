/*
 * UFF Project Semantic Learning
 */
package edu.uff.expansion.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
@ComponentAnn(name = "ExpansionAnswerSet", shortName = "expset", version = 0.1)
public class ExpansionAnswerSet implements Component {

    private List<ConcreteDataLogPredicate> answerSet;
    private List<ConcreteDataLogPredicate> samples;

    private Set<? extends Constant> individuals;
    private Set<? extends DataLogPredicate> predicates;

    private List<DataLogLiteral> expansionSet;

    public ExpansionAnswerSet() {
    }

    public ExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> samples, Set<? extends Constant> individuals, Set<? extends DataLogPredicate> predicates) throws ComponentInitException {
        this.answerSet = DataLogLiteral.getListOfLiterals(answerSet);
        this.samples = DataLogLiteral.getListOfLiterals(samples);
        this.individuals = individuals;
        this.predicates = predicates;

        init();
    }

    @Override
    public void init() throws ComponentInitException {
        answerSet.removeAll(samples);

        expansionSet = new ArrayList<>();
        DataLogLiteral lit;
        for (DataLogPredicate hp : predicates) {
            List<List<Term>> list = permuteIndividuals(individuals, hp.getArity());
            for (List<Term> terms : list) {
                lit = new DataLogLiteral(hp.getHead(), terms);
                if (!answerSet.contains(lit)) {
                    lit.setFailed(true);
                    expansionSet.add(lit);
                }

                lit = lit.clone();
                //lit.setFailed(false);
                lit.setNegative(true);
                if (!answerSet.contains(lit)) {
                    lit.setFailed(true);
                    expansionSet.add(lit);
                }
            }
        }
    }

    public List<List<Term>> permuteIndividuals(Set<? extends Constant> individuals, int listSize) {
        List<List<Term>> resp = new ArrayList<List<Term>>();

        List<Term> l;
        for (Constant ind : individuals) {
            l = new ArrayList<>();
            l.add(ind);
            resp.add(l);
        }

        for (int i = 0; i < listSize - 1; i++) {
            List<List<Term>> aux = new ArrayList<List<Term>>();

            for (List<Term> list : resp) {
                for (Constant ind : individuals) {
                    l = new ArrayList<>(list);
                    l.add(ind);
                    aux.add(l);
                }
            }

            resp = aux;
        }

        return resp;
    }

    public List<ConcreteDataLogPredicate> getExpansionSet() {
        List<ConcreteDataLogPredicate> resp = new ArrayList<>(expansionSet.size() + answerSet.size());
        resp.addAll(answerSet);
        resp.addAll(expansionSet);
        return resp;
    }

    public void setAnswerSet(List<ConcreteDataLogPredicate> answerSet) {
        this.answerSet = answerSet;
    }

    public Set<? extends Constant> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(Set<Constant> individuals) {
        this.individuals = individuals;
    }

    public Set<? extends DataLogPredicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(Set<? extends DataLogPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (ConcreteDataLogPredicate l : answerSet) {
            sb.append(l);
            sb.append(", ");
        }

        for (ConcreteDataLogPredicate l : expansionSet) {
            //sb.append("not ");
            sb.append(l);
            sb.append(", ");
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");
        
        return sb.toString().trim();
    }

    public List<ConcreteDataLogPredicate> getSamples() {
        return samples;
    }
    
}
