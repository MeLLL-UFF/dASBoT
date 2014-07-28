/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansionset.parallel;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class that does only the permutation part of the Expansion Answer Set
 * creation.
 * <br> This class does the permutation based only on relevants permutations for
 * a specified example.
 * <br> This class is used by the parallels versions of the Exapansion Answer
 * Sets based on examples like {@link ExampleExpansionAnswerSetParallel}.
 * <br> This class works by consuming the list of roots untill it is empty.
 *
 * @author Victor Guimarães
 */
public class ExamplePermute extends Permute {

    protected ConcreteLiteral example;
    protected List<Constant> exampleTerms;

    /**
     * Constructor with all needed parameters.
     *
     * @param roots the permutation's roots.
     * @param individuals the collection of individuals to permute.
     * @param listSize the permutation's size.
     * @param example the based example.
     */
    public ExamplePermute(ConcurrentLinkedQueue<List<Term>> roots, Collection<? extends Constant> individuals, int listSize, ConcreteLiteral example) {
        super(roots, individuals, listSize);
        this.example = example;
        this.exampleTerms = getTermsFromExample(example);
    }

    /**
     * Generate the permutation list based on the given individuals with the
     * given list size.
     * <br>The list size is basically the number of individuals in a single
     * permutation possibility. In other words, is the predicate's arity.
     * <br>Is used recursively by
     * {@link #permuteIndividuals(java.util.Collection, int)} to generates the
     * list by appending the parcial results into another list.
     *
     * @param append the appendable list.
     * @param individuals the collection of individuals.
     * @param listSize the list size.
     * @return the permutation list.
     */
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
                        if (isRelevant(exampleTerms, l) || isRelevant(exampleTerms, ind)) {
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
            resp.addAll(permuteIndividuals(noRelevants, exampleTerms, listSize));
        }

        return resp;
    }

    /**
     * Method to define if the specified permutation is relevante to the based
     * example.
     *
     * @param exampleTerms the example's terms.
     * @param list the specified permutation.
     * @return true if it is relevant, false otherwise.
     */
    private boolean isRelevant(Collection<Constant> exampleTerms, List<Term> list) {
        if (exampleTerms == null || list == null)
            return false;
        boolean intersection = false;

        for (Term term : exampleTerms) {
            if (list.contains(term)) {
                intersection = true;
                break;
            }
        }

        return intersection;
    }

    /**
     * Method to define if a term is relevante to the based example.
     *
     * @param exampleTerms the example's terms.
     * @param term the term.
     * @return true if it is relevant, false otherwise.
     */
    private boolean isRelevant(Collection<Constant> exampleTerms, Constant term) {
        if (exampleTerms == null || term == null)
            return false;

        return (exampleTerms.contains(term));
    }

    /**
     * Gets the terms from the based example.
     *
     * @return the terms from the based example.
     */
    private List<Constant> getTermsFromExample(ConcreteLiteral example) {
        List<Constant> resp = new ArrayList<>();
        for (Term term : example.getTerms()) {
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
