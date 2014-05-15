/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansion.set;

import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class SampleExpansionAnswerSet extends ExpansionAnswerSet {

    protected ConcreteLiteral sample;
    protected Collection<Constant> sampleTerms;
    protected int offSet = 0;
    
    public SampleExpansionAnswerSet() {
        
    }

    public SampleExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> samples, TypeTemplate individualsClasses) throws ComponentInitException {
        super(answerSet, samples, individualsClasses);
    }

    @Override
    protected List<List<Term>> permuteIndividuals(final Collection<? extends Constant> individuals, int listSize) {
        List<List<Term>> resp = new ArrayList<List<Term>>();

        List<Term> l;
        for (Constant ind : individuals) {
            l = new ArrayList<>();
            l.add(ind);
            if (listSize == 1) {
                if (isRelevant(getSampleTerms(), l))
                    resp.add(l);
            } else {
                resp.add(l);
            }
        }

        return permuteIndividuals(resp, individuals, listSize);
    }

    @Override
    protected List<List<Term>> permuteIndividuals(final List<List<Term>> append, final Collection<? extends Constant> individuals, int listSize) {

        List<Term> l;

        int size = listSize - (append != null && ! append.isEmpty() ? append.get(0).size() : listSize);

        List<List<Term>> resp = new ArrayList<>(append);
        List<List<Term>> noRelevants = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<List<Term>> aux = new ArrayList<List<Term>>();

            for (List<Term> list : resp) {
                for (Constant ind : individuals) {
                    l = new ArrayList<>(list);
                    if (i == size - 1) {
                        if (isRelevant(getSampleTerms(), l) || isRelevant(getSampleTerms(), ind)) {
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
            resp.addAll(permuteIndividuals(noRelevants, getSampleTerms(), listSize));
            //noRelevants.addAll(resp);
            //resp = noRelevants;

            //System.out.println(noRelevants);
        }

        //resp.addAll(noRelevants);
        return resp;
    }

    private static boolean isRelevant(Collection<Constant> sampleTerms, List<Term> list) {
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

    private static boolean isRelevant(Collection<Constant> sampleTerms, Constant term) {
        if (sampleTerms == null || term == null)
            return false;

        return (sampleTerms.contains(term));
    }

    @Override
    protected List<List<Term>> getGeneralPermuteMap(int key) {

        if (!generalPermuteMap.containsKey(key)) {
//            if (generalPermuteMap.isEmpty()) {
//                generalPermuteMap.put(1, permuteIndividuals(individualsClasses.getIndividuals(), 1));
//            }

            generalPermuteMap.put(key, permuteIndividuals(individualsClasses.getIndividuals(), key));
        }

        return generalPermuteMap.get(key);
    }

    private List<Constant> loadSampleTerms() {
        List<Constant> resp = new ArrayList<>();
        for (Term term : getSample().getTerms()) {
            if (term instanceof Constant) {
                resp.add((Constant) term);
            } else {
                resp.add(new Constant(term.getName()));
                System.err.println("Constant Error!");
            }
        }
        return resp;
    }

    protected ConcreteLiteral getSample() {
        if (sample == null) {
            sample = getSamples().get(0);
        }

        return sample;
    }

    public List<ConcreteLiteral> getSamples() {
        return samples.subList(offSet, offSet + 1);
    }

    protected Collection<Constant> getSampleTerms() {
        if (sampleTerms == null) {
            sampleTerms = loadSampleTerms();
        }
        return sampleTerms;
    }

    public void setSample(ConcreteLiteral sample) {
        this.sample = sample;
    }

    public int getOffSet() {
        return offSet;
    }

    public void setOffSet(int offSet) {
        if (offSet < this.samples.size()) {
            this.offSet = offSet;
        }
    }
    
}
