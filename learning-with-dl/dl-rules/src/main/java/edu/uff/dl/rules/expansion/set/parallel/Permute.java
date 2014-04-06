/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansion.set.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class Permute implements Runnable {

    protected ConcurrentLinkedQueue<List<Term>> roots;
    protected Collection<? extends Constant> individuals;
    protected int listSize;
    protected List<List<Term>> lists;

    public Permute(ConcurrentLinkedQueue<List<Term>> roots, Collection<? extends Constant> individuals, int listSize) {
        this.roots = roots;
        this.individuals = individuals;
        this.listSize = listSize;
        lists = new ArrayList<>();
    }

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

    public List<List<Term>> getLists() {
        return lists;
    }

}
