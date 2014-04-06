/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansion.set;

import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    protected List<ConcreteLiteral> answerSet;
    protected List<ConcreteLiteral> samples;

    protected Set<? extends Constant> individuals;
    protected Set<? extends DataLogPredicate> predicates;

    protected List<DataLogLiteral> expansionSet;

    protected Map<Integer, List<List<Term>>> permuteMap;

    public ExpansionAnswerSet() {
        this.permuteMap = new HashMap<>();
    }

    public ExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> samples, Set<? extends Constant> individuals, Set<? extends DataLogPredicate> predicates) throws ComponentInitException {
        this();
        this.answerSet = DataLogLiteral.getListOfLiterals(answerSet);
        this.samples = DataLogLiteral.getListOfLiterals(samples);
        this.individuals = individuals;
        this.predicates = predicates;
    }

    @Override
    public void init() throws ComponentInitException {
        answerSet.removeAll(getSamples());

        expansionSet = new ArrayList<>();
        DataLogLiteral lit;
        for (DataLogPredicate hp : predicates) {
            List<List<Term>> list = getPermuteMap(hp.getArity());
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

    protected List<List<Term>> getPermuteMap(int key) {

        if (!permuteMap.containsKey(key)) {
            if (permuteMap.isEmpty()) {
                permuteMap.put(1, permuteIndividuals(individuals, 1));
            }
            if (key > 1)
                permuteMap.put(key, permuteIndividuals(getPermuteMap(key - 1), individuals, key));
        }

        return permuteMap.get(key);
    }

    protected List<List<Term>> permuteIndividuals(final Collection<? extends Constant> individuals, int listSize) {
        List<List<Term>> resp = new ArrayList<List<Term>>();

        List<Term> l;
        for (Constant ind : individuals) {
            l = new ArrayList<>();
            l.add(ind);
            resp.add(l);
        }

        return permuteIndividuals(resp, individuals, listSize);
    }

    protected List<List<Term>> permuteIndividuals(final List<List<Term>> append, final Collection<? extends Constant> individuals, int listSize) {
        List<Term> l;
        int size = listSize - (append != null ? append.get(0).size() : listSize);
        //List<List<Term>> resp = copyList(append);
        List<List<Term>> resp = new ArrayList<>(append);

        for (int i = 0; i < size; i++) {
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

    public List<ConcreteLiteral> getExpansionSet() {
        List<ConcreteLiteral> resp = new ArrayList<>();
        resp.addAll(answerSet);
        resp.addAll(expansionSet);
        return resp;
    }

    public void setAnswerSet(List<ConcreteLiteral> answerSet) {
        this.answerSet = answerSet;
    }

    public Set<? extends Constant> getIndividuals() {
        return individuals;
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
        for (ConcreteLiteral l : answerSet) {
            sb.append(l);
            sb.append(", ");
        }

        for (ConcreteLiteral l : expansionSet) {
            //sb.append("not ");
            sb.append(l);
            sb.append(", ");
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");

        return sb.toString().trim();
    }

    public List<ConcreteLiteral> getSamples() {
        return samples;
    }

    public void setSamples(List<ConcreteLiteral> samples) {
        this.samples = samples;
    }

    public void setIndividuals(Set<? extends Constant> individuals) {
        this.individuals = individuals;
    }

}
