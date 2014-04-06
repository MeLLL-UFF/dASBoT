/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.expansion.set.parallel;

import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.expansion.set.ExpansionAnswerSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class ParalleExpansionAnswerSet extends ExpansionAnswerSet {
    
    protected int nThreads;

    public ParalleExpansionAnswerSet() {
        this.nThreads = 2;
    }

    public ParalleExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> samples, Set<? extends Constant> individuals, Set<? extends DataLogPredicate> predicates, int nThreads) throws ComponentInitException {
        super(answerSet, samples, individuals, predicates);
        this.nThreads = nThreads;
    }
    
    @Override
    protected List<List<Term>> permuteIndividuals(final List<List<Term>> append, final Collection<? extends Constant> individuals, int listSize) {
        ConcurrentLinkedQueue<List<Term>> roots;
        roots = new ConcurrentLinkedQueue<>(append);
        
        int realThreads = Math.min(this.nThreads, roots.size());
        
        Permute[] permutes = new Permute[realThreads];
        Thread[] threads = new Thread[realThreads];
        
        List<List<Term>> resp = new LinkedList<>();
        
        for (int i = 0; i < realThreads; i++) {
            permutes[i] = new Permute(roots, individuals, listSize);
            threads[i] = new Thread(permutes[i]);
            threads[i].start();
        }
        
        for (int i = 0; i < realThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ParalleExpansionAnswerSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (int i = 0; i < realThreads; i++) {
            resp.addAll(permutes[i].getLists());
        }
        
        return resp;
    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }
    
}
