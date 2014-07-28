/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansionset.parallel;

import edu.uff.dl.rules.expansionset.ExpansionAnswerSet;
import edu.uff.dl.rules.expansionset.IndividualTemplate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class responsable for create the Expamsion Answer Set by a given Answer Set.
 * <br> This class's performance can be dramatically improved by giving a
 * {@link IndividualTemplate} to typify the individuals from the problem.
 * <br> Parallel Version
 * 
 * @author Victor Guimarães
 */
public class ExpansionAnswerSetParallel extends ExpansionAnswerSet {

    protected int nThreads;

    /**
     * Constructor with only the variables allocation.
     * <br>Needed to load this class from a file (Spring).
     * <br>Sets the number of threads as 2.
     */
    public ExpansionAnswerSetParallel() {
        this.nThreads = 2;
    }

    /**
     * Constructor with all the needed variable to do the process.
     *
     * @param answerSet the answer set from a DReW's result.
     * @param examples a collection of examples of the problem.
     * @param individualsClasses a {@link IndividualTemplate} to typify the
     * individual. This class is needed, if you do not have a template, create a
     * instance of this class by passing a empty file.
     * @param nThreads the number of threads that should be created.
     */
    public ExpansionAnswerSetParallel(Collection<? extends Literal> answerSet, Collection<? extends Literal> examples, IndividualTemplate individualsClasses, int nThreads) throws ComponentInitException {
        super(answerSet, examples, individualsClasses);
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
                Logger.getLogger(ExpansionAnswerSetParallel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 0; i < realThreads; i++) {
            resp.addAll(permutes[i].getLists());
        }

        return resp;
    }

    /**
     * Getter for the number of threads. The number of threads is the number of
     * concurrent threads will be created during the process. Usually uses the
     * number of avaliable cores for the program.
     *
     * @return the number of threads.
     */
    public int getNThreads() {
        return nThreads;
    }

    /**
     * Setter for the number of threads. The number of threads is the number of
     * concurrent threads will be created during the process. Usually uses the
     * number of avaliable cores for the program.
     *
     * @param nThreads the number of threads.
     */
    public void setNThreads(int nThreads) {
        this.nThreads = nThreads;
    }

}
