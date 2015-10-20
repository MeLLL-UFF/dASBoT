/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.cli.DLRulesCLI;
import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.rules.refinement.Refinement;
import edu.uff.dl.rules.rules.refinement.TopDownBoundedRefinement;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.Time;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Literal;

/**
 * Class used by {@link DLRulesCLIParallel} to handle the refinement.
 *
 * @author Victor Guimarães
 */
public class RefinementParallel extends Thread {

    public String[] drewArgs;
    public String dlpContent;
    public String owlFilepath;
    public Set<Literal> positiveExamples, negativeExamples;

    public String outRefinement;
    public String outRefinementAll;

    public int timeout;
    public double threshold;
    public RuleMeasurer refinementRuleMeasure;
    public boolean generic;

    public ConcurrentLinkedQueue<File> ruleFiles;

    protected String description;
    protected long totalDiffTime;

    public void setProperties(String[] drewArgs, String dlpContent, String owlFilepath, Set<Literal> positiveExamples, Set<Literal> negativeExamples, String outRefinement, String outRefinementAll, int timeout, double threshold, RuleMeasurer refinementRuleMeasure, boolean generic, ConcurrentLinkedQueue<File> ruleFiles) {
        this.drewArgs = drewArgs;
        this.dlpContent = dlpContent;
        this.owlFilepath = owlFilepath;
        this.positiveExamples = positiveExamples;
        this.negativeExamples = negativeExamples;
        this.outRefinement = outRefinement;
        this.outRefinementAll = outRefinementAll;
        this.timeout = timeout;
        this.threshold = threshold;
        this.refinementRuleMeasure = refinementRuleMeasure;
        this.generic = generic;
        this.ruleFiles = ruleFiles;
    }

    public RefinementParallel() {
    }
    
    public RefinementParallel(String[] drewArgs, String dlpContent, String owlFilepath, Set<Literal> positiveExamples, Set<Literal> negativeExamples, String outRefinement, String outRefinementAll, int timeout, double threshold, RuleMeasurer refinementRuleMeasure, boolean generic, ConcurrentLinkedQueue<File> ruleFiles) {
        setProperties(drewArgs, dlpContent, owlFilepath, positiveExamples, negativeExamples, outRefinement, outRefinementAll, timeout, threshold, refinementRuleMeasure, generic, ruleFiles);
    }

    public RefinementParallel(String name, String[] drewArgs, String dlpContent, String owlFilepath, Set<Literal> positiveExamples, Set<Literal> negativeExamples, String outRefinement, String outRefinementAll, int timeout, double threshold, RuleMeasurer refinementRuleMeasure, boolean generic, ConcurrentLinkedQueue<File> ruleFiles) {
        super(name);
        setProperties(drewArgs, dlpContent, owlFilepath, positiveExamples, negativeExamples, outRefinement, outRefinementAll, timeout, threshold, refinementRuleMeasure, generic, ruleFiles);
    }

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
    /**
     * Method to do the refinement by consuming the queue of files until the
     * queue is empty.
     * <br>Do not call this method direct, call {@link #start} to run this on
     * another thread.
     */
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void run() {
        File ruleFile;
        EvaluatedRuleExample serializeRule;
        Box<Long> b = new Box<>(null), e = new Box(null);
        Box<Long> begin = new Box<>(null), end = new Box(null);
        StringBuilder sb = new StringBuilder();
        long diff;
        
        Time.getTime(begin);
        while (!ruleFiles.isEmpty()) {
            try {
                ruleFile = ruleFiles.poll();
                if (ruleFile == null) {
                    break;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream outStream = new PrintStream(baos);

                Time.getTime(b);

                EvaluatedRuleExample genericRuleExample;

                genericRuleExample = new EvaluatedRuleExample(ruleFile);

                Refinement r = new TopDownBoundedRefinement(drewArgs, dlpContent, genericRuleExample, threshold, positiveExamples, negativeExamples, timeout, refinementRuleMeasure, outStream);
                r.start();
                r.join();
                String fileName = ruleFile.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                String outPath = outRefinementAll + fileName + "_";

                Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                List<Integer> keys = new ArrayList<>(rules.keySet());
                if (keys.isEmpty()) {
                    continue;
                }

                Collections.sort(keys);

                File outputFile;

                for (Integer key : keys) {
                    outputFile = new File(outPath + key + ".txt");
                    serializeRule = new EvaluatedRuleExample(rules.get(key), genericRuleExample.getExample());
                    serializeRule.serialize(outputFile);
                }

                outPath = outRefinement + fileName;
                outputFile = new File(outPath + ".txt");

                int refinedRuleIndex = keys.size() - 1;
                serializeRule = new EvaluatedRuleExample(rules.get(keys.get(refinedRuleIndex)), genericRuleExample.getExample(), refinementRuleMeasure);
                double localMeasure = serializeRule.getMeasure();
                if (generic) {
                    EvaluatedRuleExample otherRule;
                    double otherMeasure;
                    for (int i = refinedRuleIndex - 1; i > -1; i--) {
                        otherRule = new EvaluatedRuleExample(rules.get(keys.get(i)), genericRuleExample.getExample(), refinementRuleMeasure);
                        otherMeasure = otherRule.getMeasure();
                        if (otherMeasure == localMeasure) {
                            localMeasure = otherMeasure;
                            serializeRule = otherRule;
                        }
                    }
                }

                serializeRule.serialize(outputFile);

                Time.getTime(e);

                sb.append(Time.getTime(e)).append("\n");
                diff = e.getContent() - b.getContent();
                
                
                sb.append("\n").append(baos.toString("UTF8")).append("\n\n");

                sb.append("Total time for file(").append(ruleFile.getName()).append("): ").append((double) diff / 1000).append("s\n");
                sb.append("\n\n");
            } catch (IOException | InterruptedException | NullPointerException ex) {
                Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        Time.getTime(end);
        totalDiffTime = end.getContent() - begin.getContent();
        
        description = sb.toString().trim();

    }
    
    public String getDescription() {
        return description;
    }

    public long getTotalDiffTime() {
        return totalDiffTime;
    }
    
}
