/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.cli.DLRulesCLI;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.rules.refinement.Refinement;
import edu.uff.dl.rules.rules.refinement.TopDownBoundedRefinement;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Literal;
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

    public int numberOfThreads = 4;

    protected long totalDiffTime;
    protected double sumDuration;

    protected double minDuration = Double.MAX_VALUE;
    protected int minRule;

    protected double maxDuration;
    protected int maxRule;

    protected int totalInferedRules;

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

    public DLRulesCLIParallel() {
    }

    /**
     * Handle all the generation part. For each example on the trainer file, try
     * to create a rule. If succeeded, store the rule on the ER directory on the
     * serialized version of the <code>{@link EvaluatedRuleExample}</code>
     */
    @Override
    protected void generateRuleForEachExample() {
        GenerateRuleParallel[] threads = new GenerateRuleParallel[numberOfThreads];

        ConcurrentLinkedQueue<Integer> rulesQueue = new ConcurrentLinkedQueue<>();
        try {
            int size = FileContent.getExamplesLiterals(positiveTrainExample).size();
            for (int i = 0; i < size; i++) {
                rulesQueue.add(i);
            }

            for (int i = 0; i < threads.length; i++) {
                threads[i] = new GenerateRuleParallel(dlvPath, owlFilepath, dlpContent, positiveTrainExample, negativeTrainExample, templateContent, outputDirectory, timeout, generateRuleMeasure, depth, recursiveRuleAllowed, rulesQueue);
                threads[i].start();
            }

            List<EvaluatedRuleExample> evaluatedRuleExamples = new ArrayList<>();
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
                evaluatedRuleExamples.addAll(threads[i].getEvaluatedRuleExamples());
                sumDuration += threads[i].getSumDuration();
                if (minDuration < threads[i].getMinDuration()) {
                    minDuration = threads[i].getMinDuration();
                    minRule = threads[i].getMinRule();
                }

                if (maxDuration > threads[i].getMaxDuration()) {
                    maxDuration = threads[i].getMaxDuration();
                    maxRule = threads[i].getMaxRule();
                }

                totalInferedRules += threads[i].getTotalInferedRules();
                totalDiffTime += threads[i].getTotalDiffTime();
            }

            Collections.sort(evaluatedRuleExamples, new EvaluatedRuleComparator());

            try (PrintStream outStream = new PrintStream(outputDirectory + "statistics.txt")) {
                outStream.println("Total of " + totalInferedRules + " infered rule(s)");
                outStream.println("Max time:\t" + maxDuration + "\tfor rule " + maxRule);
                outStream.println("Min time:\t" + minDuration + "\tfor rule " + minRule);
                outStream.println("Avg time:\t" + (sumDuration / (double) totalInferedRules));
                outStream.println("Total time:\t" + Time.getDiference(totalDiffTime));
                outStream.println("\n");
                printMeasure(evaluatedRuleExamples, outStream);
            } catch (IOException ex) {
                Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void refinement() {
        try (PrintStream outStream = new PrintStream(new FileOutputStream(outRefinement + "statistics.txt", true))) {
            Box<Long> b = new Box<>(null), e = new Box(null);
            Time.getTime(b);
            String beginTime = "Begin time:\t" + Time.getTime(b);

            Set<Literal> positiveExamples, negativeExamples;
            positiveExamples = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeExamples = FileContent.getExamplesLiterals(negativeTrainExample);
            EvaluatedRuleExample serializeRule;

            RefinementParallel[] threads = new RefinementParallel[numberOfThreads];

            ConcurrentLinkedQueue<File> ruleFiles = new ConcurrentLinkedQueue<>();
            File[] listFiles = (new File(outER)).listFiles(new RuleERFilter());
            ruleFiles.addAll(Arrays.asList(listFiles));

            for (int i = 0; i < threads.length; i++) {
                threads[i] = new RefinementParallel("Refinement-Thread-" + i, drewArgs, dlpContent, owlFilepath, positiveExamples, negativeExamples, outRefinement, outRefinementAll, timeout, threshold, refinementRuleMeasure, generic, ruleFiles);
                threads[i].start();
            }
            
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
                sb.append(threads[i].getName()).append("\n\n");
                sb.append(threads[i].getDescription()).append("\n\n\n");
            }

            outStream.println(beginTime);
            outStream.println("Refinement Threshold: " + threshold);
            
            outStream.println(sb.toString().trim() + "\n");
            
            outStream.println("End time:\t" + Time.getTime(e));
            outStream.println("Total time:\t" + Time.getDiference(b, e));
        } catch (FileNotFoundException | ParseException | InterruptedException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
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

class RuleERFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        if (!pathname.isFile()) {
            return false;
        }
        if (!pathname.getName().startsWith("rule")) {
            return false;
        }
        if (!pathname.getName().endsWith(".txt")) {
            return false;
        }
        
        return true;
    }

}
