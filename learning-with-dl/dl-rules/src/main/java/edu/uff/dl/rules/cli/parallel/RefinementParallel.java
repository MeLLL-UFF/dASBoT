/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.rules.avaliation.EvaluatedRule;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRuleExample;
import edu.uff.dl.rules.rules.avaliation.LaplaceMeasure;
import edu.uff.dl.rules.rules.avaliation.RuleMeasurer;
import edu.uff.dl.rules.rules.refinement.Refinement;
import edu.uff.dl.rules.rules.refinement.TopDownBoundedRefinement;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.DReWDefaultArgs;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Literal;

/**
 * Class used by {@link DLRulesCLIParallel} to handle the refinement.
 *
 * @author Victor Guimarães
 */
public class RefinementParallel extends Thread {

    private String owlFilepath;
    private Set<Literal> positiveExamples, negativeExamples;
    private String dlpContent;
    private String outRefinement;
    private String outRefinementAll;
    private int timeout;

    private Queue<File> listFiles;

    /**
     * The constructor of the class with the needed parameters.
     *
     * @param owlFilepath a path for an owl file (future use).
     * @param positiveExamples a set with the positive examples.
     * @param negativeExamples a set with the negative examples.
     * @param dlpContent all the backgroung knowledge.
     * @param outRefinement the output directory for the final refinemented
     * rules.
     * @param outRefinementAll the output directory for all refinemented rules.
     * @param timeout a timeout for the rule's inferences.
     * @param listFiles a concurrent queue with the files of the rules which
     * must be refined.
     */
    public RefinementParallel(String owlFilepath, Set<Literal> positiveExamples, Set<Literal> negativeExamples, String dlpContent, String outRefinement, String outRefinementAll, int timeout, Queue<File> listFiles) {
        this.owlFilepath = owlFilepath;
        this.positiveExamples = positiveExamples;
        this.negativeExamples = negativeExamples;
        this.dlpContent = dlpContent;
        this.outRefinement = outRefinement;
        this.outRefinementAll = outRefinementAll;
        this.timeout = timeout;
        this.listFiles = listFiles;
    }

    /**
     * Method to do the refinement by consuming the queue of files until the queue is empty.
     * <br>Do not call this method direct, call {@link #start} to run this on another thread. 
     */
    @Override
    public void run() {
        RuleMeasurer ruleMeasure = new LaplaceMeasure();
        String[] args = DReWDefaultArgs.ARGS;
        args[2] = owlFilepath;
        double threshold = 0.01;
        Box<Long> b = new Box<>(null), e = new Box(null);
        File file;
        while (!listFiles.isEmpty()) {
            file = listFiles.remove();
            if (file.isFile() && file.getName().startsWith("rule") && file.getName().endsWith(".txt")) {
                try {
                    System.out.println(Time.getTime(b));
                    System.out.println("File: " + file.getName());
                    EvaluatedRuleExample genericRuleExample;

                    genericRuleExample = new EvaluatedRuleExample(file);

                    Refinement r = new TopDownBoundedRefinement(args, dlpContent, genericRuleExample, threshold, positiveExamples, negativeExamples, timeout, ruleMeasure);
                    r.start();
                    r.join();

                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String outPath = outRefinementAll + fileName + "_";

                    Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                    Set<Integer> keys = rules.keySet();
                    Time.getTime(e);

                    File outputFile;
                    Integer biggestKey = 0;

                    for (Integer key : keys) {
                        outputFile = new File(outPath + key + ".txt");
                        rules.get(key).serialize(outputFile);
                        if (key > biggestKey) {
                            biggestKey = key;
                        }
                    }

                    outPath = outRefinement + fileName;
                    outputFile = new File(outPath + ".txt");
                    rules.get(biggestKey).serialize(outputFile);

                    System.out.println(Time.getTime(e));
                    double dif = e.getContent() - b.getContent();
                    dif /= 1000;
                    System.out.println("Total time for file(" + file.getName() + "): " + dif + "s");
                    System.out.println("\n");
                } catch (FileNotFoundException | InterruptedException ex) {
                    Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

}
