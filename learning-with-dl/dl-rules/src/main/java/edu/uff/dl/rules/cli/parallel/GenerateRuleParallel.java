/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.cli.DLRulesCLI;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.DLExamplesRules;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 *
 * @author Victor Guimarães
 */
public class GenerateRuleParallel extends Thread {

    public String dlvPath;
    public String owlFilepath;
    public String dlpContent;
    public String positiveTrainExample;
    public String negativeTrainExample;
    public String templateContent;
    public String outputDirectory;
    public int timeout;
    public RuleMeasurer generateRuleMeasure;

    public int depth;
    public boolean recursiveRuleAllowed;

    public ConcurrentLinkedQueue<Integer> rulesQueue;

    protected List<EvaluatedRuleExample> evaluatedRuleExamples;

    protected long totalDiffTime;
    protected double sumDuration;

    protected double minDuration = Double.MAX_VALUE;
    protected int minRule;

    protected double maxDuration;
    protected int maxRule;

    protected int totalInferedRules;

    public GenerateRuleParallel() {
    }

    public GenerateRuleParallel(String dlvPath, String owlFilepath, String dlpContent, String positiveTrainExample, String negativeTrainExample, String templateContent, String outputDirectory, int timeout, RuleMeasurer generateRuleMeasure, int depth, boolean recursiveRuleAllowed, ConcurrentLinkedQueue<Integer> rulesQueue) {
        this.dlvPath = dlvPath;
        this.owlFilepath = owlFilepath;
        this.dlpContent = dlpContent;
        this.positiveTrainExample = positiveTrainExample;
        this.negativeTrainExample = negativeTrainExample;
        this.templateContent = templateContent;
        this.outputDirectory = outputDirectory;
        this.timeout = timeout;
        this.generateRuleMeasure = generateRuleMeasure;
        this.depth = depth;
        this.recursiveRuleAllowed = recursiveRuleAllowed;
        this.rulesQueue = rulesQueue;
    }

    public GenerateRuleParallel(String name, String dlvPath, String owlFilepath, String dlpContent, String positiveTrainExample, String negativeTrainExample, String templateContent, String outputDirectory, int timeout, RuleMeasurer generateRuleMeasure, int depth, boolean recursiveRuleAllowed, ConcurrentLinkedQueue<Integer> rulesQueue) {
        super(name);
        this.dlvPath = dlvPath;
        this.owlFilepath = owlFilepath;
        this.dlpContent = dlpContent;
        this.positiveTrainExample = positiveTrainExample;
        this.negativeTrainExample = negativeTrainExample;
        this.templateContent = templateContent;
        this.outputDirectory = outputDirectory;
        this.timeout = timeout;
        this.generateRuleMeasure = generateRuleMeasure;
        this.depth = depth;
        this.recursiveRuleAllowed = recursiveRuleAllowed;
        this.rulesQueue = rulesQueue;
    }

    @Override
    public void run() {
        try {
            File outER = new File(outputDirectory, "ER");
            DLExamplesRules run;

            Box<Long> begin = new Box<>(null), end = new Box(null);
            double duration;
            EvaluatedRule er = null;
            EvaluatedRuleExample evaluatedRuleExample;
            File fOut;
            String ruleName;
            List<ConcreteLiteral> examples;
            ConcreteLiteral example;
            Time.getTime(begin);
            DReWReasoner reasoner = new DReWReasoner(dlvPath, owlFilepath, dlpContent, positiveTrainExample, templateContent, null);
            reasoner.setDepth(depth);
            reasoner.setRecursiveRuleAllowed(recursiveRuleAllowed);
            reasoner.init();

            evaluatedRuleExamples = new ArrayList<>();
            PrintStream outStream = null;
            Integer index;
            while (!rulesQueue.isEmpty()) {
                index = rulesQueue.poll();
                ruleName = "rule" + index + ".txt";
                try {
                    outStream = new PrintStream(outputDirectory + ruleName);
                    reasoner.setOutStream(outStream);
                    run = new DLExamplesRules(dlpContent, reasoner, positiveTrainExample, negativeTrainExample, outStream);
                    run.setOffset(index);
                    run.start();
                    run.join(timeout * 1000);

                    if (run.getState() == Thread.State.TERMINATED) {
                        duration = run.getDuration();

                        sumDuration += duration;
                        if (duration < minDuration) {
                            minDuration = duration;
                            minRule = index;
                        }

                        if (duration > maxDuration) {
                            maxDuration = duration;
                            maxRule = index;
                        }

                        totalInferedRules++;

                        outStream.println("It takes " + duration + "s to finish!");
                        er = run.getEvaluatedRule();
                    } else {
                        run.interrupt();
                        outStream.println("Stoped on " + timeout + "s!");
                    }

                    if (er != null) {
                        evaluatedRuleExample = new EvaluatedRuleExample(er, run.getExamples().get(0));
                    } else {
                        examples = run.getExamples();

                        if (examples.size() == 1) {
                            example = examples.get(0);
                        } else {
                            example = examples.get(run.getOffset());
                        }
                        FileContent.getRuleFromString(run.getAnwserSetRule().getAnswerRule().getRule().toString());
                        evaluatedRuleExample = new EvaluatedRuleExample(run.getAnwserSetRule().getAnswerRule().getRule(), 0, 0, 0, 0, generateRuleMeasure, example);
                    }

                    fOut = new File(outER, ruleName);
                    evaluatedRuleExample.serialize(fOut);

                    evaluatedRuleExamples.add(evaluatedRuleExample);
//                } catch (InterruptedException | FileNotFoundException | NullPointerException | ParseException ex) {
                } catch (Exception ex) {
                    if (outStream != null) {
                        outStream.println(ex.getClass() + ": " + ex.getMessage());
                    }
                } finally {
                    if (outStream != null) {
                        outStream.close();
                    }
                }
            }
            Time.getTime(end);
            totalDiffTime = end.getContent() - begin.getContent();
        } catch (ComponentInitException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getDlvPath() {
        return dlvPath;
    }

    public void setDlvPath(String dlvPath) {
        this.dlvPath = dlvPath;
    }

    public String getOwlFilepath() {
        return owlFilepath;
    }

    public void setOwlFilepath(String owlFilepath) {
        this.owlFilepath = owlFilepath;
    }

    public String getDlpContent() {
        return dlpContent;
    }

    public void setDlpContent(String dlpContent) {
        this.dlpContent = dlpContent;
    }

    public String getPositiveTrainExample() {
        return positiveTrainExample;
    }

    public void setPositiveTrainExample(String positiveTrainExample) {
        this.positiveTrainExample = positiveTrainExample;
    }

    public String getNegativeTrainExample() {
        return negativeTrainExample;
    }

    public void setNegativeTrainExample(String negativeTrainExample) {
        this.negativeTrainExample = negativeTrainExample;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RuleMeasurer getGenerateRuleMeasure() {
        return generateRuleMeasure;
    }

    public void setGenerateRuleMeasure(RuleMeasurer generateRuleMeasure) {
        this.generateRuleMeasure = generateRuleMeasure;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isRecursiveRuleAllowed() {
        return recursiveRuleAllowed;
    }

    public void setRecursiveRuleAllowed(boolean recursiveRuleAllowed) {
        this.recursiveRuleAllowed = recursiveRuleAllowed;
    }

    public ConcurrentLinkedQueue<Integer> getLinkedQueue() {
        return rulesQueue;
    }

    public void setLinkedQueue(ConcurrentLinkedQueue<Integer> linkedQueue) {
        this.rulesQueue = linkedQueue;
    }

    public List<EvaluatedRuleExample> getEvaluatedRuleExamples() {
        return evaluatedRuleExamples;
    }

    public void setEvaluatedRuleExamples(List<EvaluatedRuleExample> evaluatedRuleExamples) {
        this.evaluatedRuleExamples = evaluatedRuleExamples;
    }

    public long getTotalDuration() {
        return totalDiffTime;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDiffTime = totalDuration;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public int getMinRule() {
        return minRule;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public int getMaxRule() {
        return maxRule;
    }

    public long getTotalDiffTime() {
        return totalDiffTime;
    }

    public int getTotalInferedRules() {
        return totalInferedRules;
    }

    public double getSumDuration() {
        return sumDuration;
    }

}
