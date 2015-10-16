/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.rules.DLExamplesRules;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.ComponentInitException;

/**
 * Class used by {@link DLRulesCLIParallel} to handle the rule's generation.
 *
 * @author Victor Guimarães
 * @deprecated 
 */
public class RuleParallel extends Thread {

    private String owlFilepath;
    private String dlpContent;
    private String positiveTrainExample;
    private String negativeTrainExample;
    private String templateContent;
    private String outER;
    private int timeout;

    private Queue<Integer> examples;

    private StringBuilder description;
    private int count = 0;
    private double min = Double.MAX_VALUE, max = 0, sun = 0;

    /**
     * The constructor of the class with the needed parameters.
     *
     * @param name a name for the thread.
     * @param owlFilepath a path for an owl file (future use).
     * @param dlpContent all the backgroung knowledge.
     * @param positiveTrainExample a set with the positive examples.
     * @param negativeTrainExample a set with the negative examples.
     * @param templateContent the content of the template file.
     * @param outER the output directory for the rules.
     * @param timeout a timeout for the rule's inferences.
     * @param examples a concurrent queue with the base example number to create
     * the rules with.
     */
    public RuleParallel(String name, String owlFilepath, String dlpContent, String positiveTrainExample, String negativeTrainExample, String templateContent, String outER, int timeout, Queue<Integer> examples) {
        super(name);
        this.owlFilepath = owlFilepath;
        this.dlpContent = dlpContent;
        this.positiveTrainExample = positiveTrainExample;
        this.negativeTrainExample = negativeTrainExample;
        this.templateContent = templateContent;
        this.outER = outER;
        this.timeout = timeout;
        this.examples = examples;
    }

    /**
     * Method to create the rules by consuming the queue of example's numbers
     * until the queue is empty.
     * <br>Do not call this method direct, call {@link #start} to run this on
     * another thread.
     */
    @Override
    public void run() {
        Box<Long> begin = new Box<>(null), end = new Box(null);
        Time.getTime(begin);

        double aux = 0;
        int maxR = 0, minR = 0;

        try {
            DLExamplesRules run;

            EvaluatedRule er;
            EvaluatedRuleExample ere;
            File fOut;
            String ruleName;
            int i;

            DReWReasoner reasoner = new DReWReasoner(owlFilepath, dlpContent, positiveTrainExample, templateContent, System.out);
            reasoner.init();
            PrintStream outStream;
            while (!examples.isEmpty()) {
                i = examples.remove();
                ruleName = "rule" + i + ".txt";
                outStream = new PrintStream(ruleName);
                try {
                    run = new DLExamplesRules(dlpContent, reasoner, positiveTrainExample, negativeTrainExample, outStream);
                    run.setOffset(i);
                    run.start();
                    run.join(timeout * 1000);

                    if (run.getState() == Thread.State.TERMINATED) {
                        aux = run.getDuration();
                        sun += aux;
                        if (aux < min) {
                            min = aux;
                            minR = i;
                        }

                        if (aux > max) {
                            max = aux;
                            maxR = i;
                        }

                        count++;
                        outStream.println("It takes " + aux + "s to finish!");
                        er = run.getEvaluatedRule();
                        ere = new EvaluatedRuleExample(er, run.getExamples().get(0));
                        if (run.getEvaluatedRule() != null) {
                            fOut = new File(outER + ruleName);
                            ere.serialize(fOut);
                        }
                    } else {
                        run.interrupt();
                        outStream.println("Stoped on " + timeout + "s!");
                    }

                } catch (InterruptedException | FileNotFoundException ex) {
                    Logger.getLogger(RuleParallel.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    outStream.close();
                }
            }
        } catch (ComponentInitException ex) {
            Logger.getLogger(RuleParallel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RuleParallel.class.getName()).log(Level.SEVERE, null, ex);
        } //To change body of generated methods, choose Tools | Templates.

        Time.getTime(end);

        description = new StringBuilder();
        description.append(this.getName());
        description.append("Total of ").append(count).append("rule(s)").append("\n");
        description.append("Max time:\t").append(max).append("\tfor rule ").append(maxR).append("\n");
        description.append("Min time:\t").append(min).append("\tfor rule ").append(minR).append("\n");
        description.append("Avg time:\t").append(sun / (double) count).append("\n");
        description.append("Total time:\t").append(Time.getDiference(begin.getContent(), end.getContent()));
    }

    /**
     * Get the class description. This description is an overall statics of the
     * rules generated by this class.
     *
     * @return an overall statistic by this class.
     */
    @Override
    public String toString() {
        return description.toString().trim();
    }

    /**
     * A getter for the number of rules generated by this class.
     *
     * @return the number of rules generated by this class.
     */
    public int getCount() {
        return count;
    }

    /**
     * A getter for the time that does took to be generated the fastest rule.
     *
     * @return the minimum time.
     */
    public double getMin() {
        return min;
    }

    /**
     * A getter for the time that does took to be generated the slowest rule.
     *
     * @return the maximum time.
     */
    public double getMax() {
        return max;
    }

    /**
     * A getter for the total time that does took to be generated the rules by
     * this class.
     *
     * @return the total time.
     */
    public double getSun() {
        return sun;
    }

}
