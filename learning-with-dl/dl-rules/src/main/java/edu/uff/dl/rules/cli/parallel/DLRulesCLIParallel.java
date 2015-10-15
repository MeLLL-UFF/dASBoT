/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.cli.DLRulesCLI;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.util.FileContent;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to call the program by command line interface (Parallel Version). This
 * class can be used to generate rules for each example on the trainer file,
 * refine and evaluate the rules against test fold using cross validation.
 * <br><br>
 * Parallel Version of {@link edu.uff.dl.rules.cli.DLRulesCLI}
 * <br><br>
 * (Not Working Yet)
 *
 * @author Victor Guimarães
 */
public class DLRulesCLIParallel extends DLRulesCLI {

    protected int numberOfThreads = 8;

    @Override
    protected Queue<String> parseArguments(String[] args) throws FileNotFoundException {
        Queue<String> queue = super.parseArguments(args);
        
        int lNumberOfThreads = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : 0);
        
        this.setNumberOfThreads(lNumberOfThreads);
        
        return queue;
    }
    
    

    /**
     * Main function, used to start the program.
     *
     * @param args the parameters needed for the program execution.<br>-rule to
     * generate the rules (optional),<br>-ref to refine the rules
     * (optional),<br>-cv to cross validate the rules (optional),<br>an integer
     * number of bk files (omitted = 1),<br>a set of paths for the bk files,
     * according with the number of files previous setted,<br>-tp to use
     * template (file to type the individuos according with its relationships)
     * (optional),<br>if -tp was used, the path of the template file,<br>an
     * output directory for the program's output,<br>a timeout for the rule's
     * inferences,<br>the cross validation directory with the folds,<br>the
     * fold's prefix name,<br>the number of folds,
     * <br>the number of threads that should be created simultaneously (usually,
     * the number of available cores).
     * @throws FileNotFoundException in case of a file path does not exist.
     */
    public static void main(String[] args) throws FileNotFoundException {
        try {
            DLRulesCLIParallel dlrcli = new DLRulesCLIParallel();
            dlrcli.parseArguments(args);

            dlrcli.init();
        } catch (NoSuchElementException | NumberFormatException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The constructor of the class with the needed parameters.
     *
     * @param dlpFilepaths a set of paths for the bk files.
     * @param owlFilepath a path for an owl file (future use).
     * @param positiveTrainFilepath a set of positive examples.
     * @param negativeTrainFilepath a set of negative examples.
     * @param outputDirectory an output directory for the program's output.
     * @param timeout a timeout for the rule's inferences.
     * @param templateFilepath if -tp was used, the path of the template file.
     * @param cvDirectory the cross validation directory with the folds.
     * @param cvPrefix the fold's prefix name.
     * @param cvNumberOfFolds the number of folds.
     * @param numberOfThreads the number of threads that should be created.
     * simultaneously (usually, the number of available cores).
     * @throws FileNotFoundException in case of any file path does not exist.
     */
    public DLRulesCLIParallel() {
    }

    /**
     * Handle all the generation part. For each example on the trainer file, try
     * to create a rule. If succeeded, store the rule on the ER directory on the
     * serialized version of the <code>{@link EvaluatedRuleExample}</code>
     */
    protected void generateRuleForEachExample() {
        try {
            int size = FileContent.getExamplesLiterals(positiveTrainExample).size();

            RuleParallel[] rps = new RuleParallel[numberOfThreads];

            Queue<Integer> examples[] = new ConcurrentLinkedQueue[numberOfThreads];

            for (int i = 0; i < examples.length; i++) {
                examples[i] = new ConcurrentLinkedQueue<>();
            }

            for (int i = 0; i < size; i++) {
                examples[i % numberOfThreads].add(i);
            }
            String name;
            for (int i = 0; i < rps.length; i++) {
                name = "RuleParalle-Thread-" + i;
                rps[i] = new RuleParallel(name, owlFilepath, dlpContent, positiveTrainExample, negativeTrainExample, templateContent, outER, timeout, examples[i]);
                rps[i].start();
            }

            for (int i = 0; i < rps.length; i++) {
                rps[i].join();
                System.out.println(rps[i] + "\n");
            }

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        if (numberOfThreads > 0) {
            this.numberOfThreads = numberOfThreads;
        }
    }

}
