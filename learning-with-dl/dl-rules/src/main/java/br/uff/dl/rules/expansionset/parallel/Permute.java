/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.expansionset.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class that does only the permutation part of the Expansion Answer Set
 * creation.
 * <br> This class is used by the parallels versions of the Exapansion Answer
 * Sets like {@link ExpansionAnswerSetParallel} and
 * {@link ExampleExpansionAnswerSetParallel}.
 * <br> This class works by consuming the list of roots untill it is empty.
 *
 * @author Victor Guimarães
 */
public class Permute implements Runnable {

    protected ConcurrentLinkedQueue<List<Term>> roots;
    protected Collection<? extends Constant> individuals;
    protected int listSize;
    protected List<List<Term>> lists;

    /**
     * Constructor with all needed parameters.
     *
     * @param roots the permutation's roots.
     * @param individuals the collection of individuals to permute.
     * @param listSize the permutation's size.
     */
    public Permute(ConcurrentLinkedQueue<List<Term>> roots, Collection<? extends Constant> individuals, int listSize) {
        this.roots = roots;
        this.individuals = individuals;
        this.listSize = listSize;
        lists = new ArrayList<>();
    }

    /**
     * Method to do the permutation.
     * <br>Do not call this method direct, call {@link #start} to run this on
     * another thread.
     */
    @Override
    public void run() {
        List<Term> root;
        List<List<Term>> append;
        while (!roots.isEmpty()) {
            try {
                root = roots.remove();
            } catch (NoSuchElementException ex) {
                return;
            }
            append = new ArrayList<>();
            append.add(root);
            lists.addAll(permuteIndividuals(append, individuals, listSize));
        }
    }

    protected List<List<Term>> permuteIndividuals(final List<List<Term>> append, final Collection<? extends Constant> individuals, int listSize) {
        List<Term> l;
        int size = listSize - (append != null ? append.get(0).size() : listSize);

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

    /**
     * Getter for the permutations lists.
     *
     * @return the permutations lists.
     */
    public List<List<Term>> getLists() {
        return lists;
    }

}
