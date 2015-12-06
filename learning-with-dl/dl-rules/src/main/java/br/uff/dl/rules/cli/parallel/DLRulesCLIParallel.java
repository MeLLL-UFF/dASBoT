/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.cli.parallel;

import br.uff.dl.rules.cli.DLRulesCLI;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import br.uff.dl.rules.util.Box;
import br.uff.dl.rules.util.DReWDefaultArgs;
import br.uff.dl.rules.util.FileContent;
import br.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to call the program by command line interface (Parallel Version). This
 * class can be used to generate rules for each example on the trainer file,
 * refine and evaluate the rules against test fold using cross validation.
 * <br><br>
 * Parallel Version of {@link br.uff.dl.rules.cli.DLRulesCLI}
 * <br><br>
 * (Not Working Yet)
 *
 * @author Victor Guimarães
 */
public class DLRulesCLIParallel extends DLRulesCLI {

    public static final String RULE_THREAD_NAME_PREFIX = "Rule-Thread-";
    public static final String REFINEMENT_THREAD_NAME_PREFIX = "Refinement-Thread-";

    public int numberOfThreads = 4;

    protected double sumDuration;

    protected double minDuration = Double.MAX_VALUE;
    protected int minRule;

    protected double maxDuration;
    protected int maxRule;

    protected int totalInferedRules;

    @Override
    public Queue<String> parseArguments(String[] args) throws FileNotFoundException {
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
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
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
        Box<Long> begin = new Box<>(null), end = new Box(null);

        GenerateRuleParallel[] threads = new GenerateRuleParallel[numberOfThreads];

        ConcurrentLinkedQueue<Integer> rulesQueue = new ConcurrentLinkedQueue<>();
        try {
            Time.getTime(begin);
            int size = FileContent.getExamplesLiterals(positiveTrainExample).size();
            for (int i = 0; i < size; i++) {
                rulesQueue.add(i);
            }

            File newOWLFile;
            int owlPathIndex = DReWDefaultArgs.getOWLFilepath(drewArgs);
            String threadName;

            for (int i = 0; i < threads.length; i++) {
                try {
                    threadName = RULE_THREAD_NAME_PREFIX + i;
                    newOWLFile = new File(drewArgs[owlPathIndex] + "." + threadName);
                    FileUtils.copyFile(new File(drewArgs[owlPathIndex]), newOWLFile);

                    threads[i] = new GenerateRuleParallel(threadName, dlvPath, newOWLFile.getAbsolutePath(), dlpContent, positiveTrainExample, negativeTrainExample, templateContent, outputDirectory, timeout, generateRuleMeasure, depth, recursiveRuleAllowed, rulesQueue);
                    threads[i].start();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            long totalDiffTime = 0;
            List<EvaluatedRuleExample> evaluatedRuleExamples = new ArrayList<>();
            for (int i = 0; i < threads.length; i++) {
                try {
                    threads[i].join();

                    newOWLFile = new File(drewArgs[owlPathIndex] + "." + threads[i].getName());
                    if (newOWLFile.exists()) {
                        newOWLFile.delete();
                    }
                    newOWLFile = new File(drewArgs[owlPathIndex] + "." + threads[i].getName() + DLV_FILE_SUFIX);
                    if (newOWLFile.exists()) {
                        newOWLFile.delete();
                    }

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
                } catch (NullPointerException ex) {
                    Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            Collections.sort(evaluatedRuleExamples, new EvaluatedRuleComparator());
            Time.getTime(end);
            double speedup = (double) totalDiffTime / (numberOfThreads * (end.getContent() - begin.getContent()));

            try (PrintStream outStream = new PrintStream(outputDirectory + "statistics.txt")) {
                outStream.println("Total of " + totalInferedRules + " infered rule(s).\n");
                outStream.println("Max time:\t\t" + maxDuration + "\tfor rule " + maxRule);
                outStream.println("Min time:\t\t" + minDuration + "\tfor rule " + minRule);
                outStream.println("Avg time:\t\t" + (sumDuration / (double) totalInferedRules));
                outStream.println("Total processor time:\t" + Time.getDiference(totalDiffTime));
                outStream.println("Total spent time:\t" + Time.getDiference(begin, end));
                outStream.println("Number of threads:\t" + numberOfThreads);
                outStream.println("Speedup:\t\t" + speedup);
                outStream.println("\n");
                printMeasure(evaluatedRuleExamples, outStream);
            } catch (IOException ex) {
                Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void refinement() {
        //(PrintStream outStream = new PrintStream(new FileOutputStream(outRefinement + "statistics.txt", true)))
        try {
            StringBuilder sb = new StringBuilder();

            Box<Long> begin = new Box<>(null), end = new Box(null);
            Time.getTime(begin);
            sb.append("Begin time:\t").append(Time.getTime(begin)).append("\n\n");

            Set<Literal> positiveExamples, negativeExamples;
            positiveExamples = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeExamples = FileContent.getExamplesLiterals(negativeTrainExample);

            RefinementParallel[] threads = new RefinementParallel[numberOfThreads];

            ConcurrentLinkedQueue<File> ruleFiles = new ConcurrentLinkedQueue<>();
            File[] listFiles = (new File(outER)).listFiles(new RuleERFilter());
            ruleFiles.addAll(Arrays.asList(listFiles));

            File newOWLFile;
            int owlPathIndex = DReWDefaultArgs.getOWLFilepath(drewArgs);
            String threadName;
            String[] newDrewArgs;

            for (int i = 0; i < threads.length; i++) {
                try {
                    threadName = REFINEMENT_THREAD_NAME_PREFIX + i;
                    newOWLFile = new File(drewArgs[owlPathIndex] + "." + threadName);
                    FileUtils.copyFile(new File(drewArgs[owlPathIndex]), newOWLFile);
                    newDrewArgs = Arrays.copyOf(drewArgs, drewArgs.length);
                    newDrewArgs[owlPathIndex] = newOWLFile.getAbsolutePath();

                    threads[i] = new RefinementParallel(threadName, newDrewArgs, dlpContent, owlFilepath, positiveExamples, negativeExamples, outRefinement, outRefinementAll, timeout, threshold, refinementRuleMeasure, generic, ruleFiles);
                    threads[i].setRefinementClass(refinementClass);
                    threads[i].start();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            long totalDiffTime = 0;
            for (int i = 0; i < threads.length; i++) {
                try {
                    threads[i].join();
                    newOWLFile = new File(drewArgs[owlPathIndex] + "." + threads[i].getName());
                    if (newOWLFile.exists()) {
                        newOWLFile.delete();
                    }
                    newOWLFile = new File(drewArgs[owlPathIndex] + "." + threads[i].getName() + DLV_FILE_SUFIX);
                    if (newOWLFile.exists()) {
                        newOWLFile.delete();
                    }

                    totalDiffTime += threads[i].getTotalDiffTime();
                    sb.append("---------- ").append(threads[i].getName()).append(" ----------\n\n");
                    sb.append(threads[i].getDescription()).append("\n\n\n\n");
                } catch (NullPointerException ex) {
                    Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            sb.append("Refinement Threshold: ").append(threshold).append("\n\n");
            sb.append("End time:\t\t").append(Time.getTime(end)).append("\n");

            double speedup = (double) totalDiffTime / (numberOfThreads * (end.getContent() - begin.getContent()));

            sb.append("Total processor time:\t").append(Time.getDiference(totalDiffTime)).append("\n");
            sb.append("Total spent time:\t").append(Time.getDiference(begin, end)).append("\n");
            sb.append("Number of threads:\t").append(numberOfThreads).append("\n");
            sb.append("Speedup:\t\t").append(speedup).append("\n");
            sb.append("Total time:\t\t").append(Time.getDiference(begin, end)).append("\n");

            FileUtils.writeStringToFile(new File(outRefinement + "statistics.txt"), sb.toString().trim(), true);
        } catch (IOException | ParseException | InterruptedException ex) {
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
