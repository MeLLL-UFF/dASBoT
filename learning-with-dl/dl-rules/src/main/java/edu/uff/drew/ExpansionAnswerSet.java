/*
 * UFF Project Semantic Learning
 */
package edu.uff.drew;

import edu.uff.dl.rules.HeadPredicate;
import java.util.ArrayList;
import java.util.HashSet;
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

    private Set<Literal> answerSet;

    private Set<String> individuals;
    private Set<HeadPredicate> predicates;

    private List<Literal> expansionSet;

    private Set<String> answerSetAsString;
    
    public ExpansionAnswerSet() {
    }

    public ExpansionAnswerSet(Set<Literal> answerSet, Set<String> individuals, Set<HeadPredicate> predicates) throws ComponentInitException {
        this.answerSet = answerSet;
        this.individuals = individuals;
        this.predicates = predicates;
        init();
    }

    @Override
    public void init() throws ComponentInitException {
        answerSetAsString = new HashSet<>();
        
        for (Literal l : answerSet) {
            answerSetAsString.add(l.toString());
        }
        
        expansionSet = new ArrayList<>();
        Literal lit;
        for (HeadPredicate hp : predicates) {
            List<List<Term>> list = permuteIndividuals(individuals, hp.getArity());
            for (List<Term> terms : list) {
                Term[] t = listToArray(terms);
                lit = new Literal(hp.getHead(), t);
                if (! answerSetAsString.contains(lit.toString())) {
                    expansionSet.add(lit);
                }
                
                
                lit = new Literal(hp.getHead(), t);
                lit.setNegative(true);
                if (! answerSetAsString.contains(lit.toString())) {
                    expansionSet.add(lit);
                }
                
            }
        }
    }
    
    private Term[] listToArray(List<Term> l) {
        Term[] resp = new Term[l.size()];
        int i = 0;
        for (Term t : l) {
            resp[i] = t;
            i++;
        }
        
        return resp;
    }

    public List<List<Term>> permuteIndividuals(Set<String> individuals, int listSize) {
        List<List<Term>> resp = new ArrayList<List<Term>>();

        List<Term> l;
        for (String ind : individuals) {
            l = new ArrayList<>();
            l.add(new Constant(ind));
            resp.add(l);
        }

        for (int i = 0; i < listSize - 1; i++) {
            List<List<Term>> aux = new ArrayList<List<Term>>();

            for (List<Term> list : resp) {
                for (String ind : individuals) {
                    l = new ArrayList<>(list);
                    l.add(new Constant(ind));
                    aux.add(l);
                }
            }

            resp = aux;
        }

        return resp;
    }

    public Set<Literal> getAnswerSet() {
        return answerSet;
    }

    public void setAnswerSet(Set<Literal> answerSet) {
        this.answerSet = answerSet;
    }

    public Set<String> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(Set<String> individuals) {
        this.individuals = individuals;
    }

    public Set<HeadPredicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(Set<HeadPredicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (Literal l : answerSet) {
            sb.append(l);
            sb.append(", ");
        }

        for (Literal l : expansionSet) {
            sb.append("not ");
            sb.append(l);
            sb.append(", ");
        }

        String resp = sb.toString().trim();
        resp = resp.substring(0, resp.length() - 1);

        resp = resp + " }";

        return resp;
    }

}
