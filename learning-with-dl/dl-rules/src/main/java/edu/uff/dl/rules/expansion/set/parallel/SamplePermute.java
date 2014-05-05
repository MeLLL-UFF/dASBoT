/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansion.set.parallel;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class SamplePermute extends Permute {

    protected ConcreteLiteral sample;
    protected List<Constant> sampleTerms;

    public SamplePermute(ConcurrentLinkedQueue<List<Term>> roots, Collection<? extends Constant> individuals, int listSize, ConcreteLiteral sample) {
        super(roots, individuals, listSize);
        this.sample = sample;
        this.sampleTerms = loadSampleTerms(sample);
    }

    @Override
    protected List<List<Term>> permuteIndividuals(List<List<Term>> append, Collection<? extends Constant> individuals, int listSize) {
        List<Term> l;

        int size = listSize - (append != null ? append.get(0).size() : listSize);

        List<List<Term>> resp = new ArrayList<>(append);
        List<List<Term>> noRelevants = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<List<Term>> aux = new ArrayList<List<Term>>();

            for (List<Term> list : resp) {
                for (Constant ind : individuals) {
                    l = new ArrayList<>(list);
                    if (i == size - 1) {
                        if (isRelevant(sampleTerms, l) || isRelevant(sampleTerms, ind)) {
                            l.add(ind);
                            aux.add(l);
                        } else {
                            noRelevants.add(l);
                            break;
                        }
                    } else {
                        l.add(ind);
                        aux.add(l);
                    }
                }
            }

            resp = aux;
        }
        if (!noRelevants.isEmpty()) {
            resp.addAll(permuteIndividuals(noRelevants, sampleTerms, listSize));
        }

        return resp;
    }
    
    private boolean isRelevant(Collection<Constant> sampleTerms, List<Term> list) {
        if (sampleTerms == null || list == null)
            return false;
        boolean intersection = false;

        for (Term term : sampleTerms) {
            if (list.contains(term)) {
                intersection = true;
                break;
            }
        }

        return intersection;
    }

    private boolean isRelevant(Collection<Constant> sampleTerms, Constant term) {
        if (sampleTerms == null || term == null)
            return false;

        return (sampleTerms.contains(term));
    }
    
    private List<Constant> loadSampleTerms(ConcreteLiteral sample) {
        List<Constant> resp = new ArrayList<>();
        for (Term term : sample.getTerms()) {
            if (term instanceof Constant) {
                resp.add((Constant) term);
            } else {
                resp.add(new Constant(term.getName()));
                System.err.println("Constant Error!");
            }
        }
        return resp;
    }
}
