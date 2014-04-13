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
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;
import org.semanticweb.drew.dlprogram.model.Predicate;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
@ComponentAnn(name = "ExpansionAnswerSet", shortName = "expset", version = 0.1)
public class ExpansionAnswerSet implements Component {

    protected List<ConcreteLiteral> answerSet;
    protected List<ConcreteLiteral> samples;

    protected List<DataLogLiteral> expansionSet;

    protected Map<List<Term>, List<List<Term>>> permuteMap;
    protected Map<Integer, List<List<Term>>> generalPermuteMap;
    protected TypeTemplate individualsClasses;

    public ExpansionAnswerSet() {
        this.permuteMap = new HashMap<>();
        this.generalPermuteMap = new HashMap<>();
    }

    public ExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> samples, TypeTemplate individualsClasses) throws ComponentInitException {
        this();
        this.answerSet = DataLogLiteral.getListOfLiterals(answerSet);
        this.samples = DataLogLiteral.getListOfLiterals(samples);

        this.individualsClasses = individualsClasses;
    }

    @Override
    public void init() throws ComponentInitException {
        answerSet.removeAll(getSamples());

        expansionSet = new ArrayList<>();

        Collection<Clause> facts;
        for (DataLogPredicate pred : individualsClasses.getProgramPredicates()) {
            facts = individualsClasses.getTemplateFactsForPredicate(pred);
            if (facts != null && ! facts.isEmpty()) {
                loadLiteralsFromFacts(expansionSet, facts);
            } else {
                System.out.println(pred);
                loadLiteralsFromFacts(expansionSet, facts, pred.getHead(), getGeneralPermuteMap(pred.getArity()));
            }
        }

        //loadLiteralsFromFacts(expansionSet, individualsClasses.getTemplateFacts());
    }

    protected void loadLiteralsFromFacts(Collection<DataLogLiteral> expansionSet, Collection<Clause> facts) {
        if (expansionSet == null || facts == null || facts.isEmpty())
            return;

        for (Clause c : facts) {
            List<List<Term>> list = getPermuteMap(c.getHead().getTerms());
            loadLiteralsFromFacts(expansionSet, facts, ((NormalPredicate) c.getHead().getPredicate()).getName(), list);
        }
    }

    protected void loadLiteralsFromFacts(Collection<DataLogLiteral> expansionSet, Collection<Clause> facts, String pred, List<List<Term>> list) {
        DataLogLiteral lit;
        for (List<Term> terms : list) {

            lit = new DataLogLiteral(pred, terms);
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

    protected List<List<Term>> getPermuteMap(List<Term> key) {

        if (!permuteMap.containsKey(key)) {
            List<List<Term>> resp = permuteIndividuals(individualsClasses.getIndividualsGroups().get(key.get(0).getName()), 1);
            resp.addAll(permuteIndividuals(individualsClasses.getIndividualsGroups().get(TypeTemplate.OTHER_INDIVIDUALS), 1));
            List<List<Term>> aux;
            for (int i = 1; i < key.size(); i++) {
                aux = permuteIndividuals(resp, individualsClasses.getIndividualsGroups().get(key.get(i).getName()), i + 1);
                aux.addAll(permuteIndividuals(resp, individualsClasses.getIndividualsGroups().get(TypeTemplate.OTHER_INDIVIDUALS), i + 1));
                resp = aux;
            }

            permuteMap.put(key, resp);
        }

        return permuteMap.get(key);
    }

    protected List<List<Term>> getGeneralPermuteMap(int key) {
        if (!generalPermuteMap.containsKey(key)) {
            if (generalPermuteMap.isEmpty()) {
                generalPermuteMap.put(1, permuteIndividuals(individualsClasses.getIndividuals(), 1));
            }
            if (key > 1)
                generalPermuteMap.put(key, permuteIndividuals(getGeneralPermuteMap(key - 1), individualsClasses.getIndividuals(), key));
        }

        return generalPermuteMap.get(key);
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

    public void setIndividualsClasses(TypeTemplate individualsClasses) {
        this.individualsClasses = individualsClasses;
    }
    
}
