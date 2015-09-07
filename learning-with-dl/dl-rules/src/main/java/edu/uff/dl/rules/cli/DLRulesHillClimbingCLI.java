/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli;

import edu.uff.dl.rules.exception.TimeoutException;
import edu.uff.dl.rules.evaluation.CompressionMeasure;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.evaluation.LaplaceMeasure;
import edu.uff.dl.rules.rules.evaluation.RuleEvaluator;
import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.refinement.Refinement;
import edu.uff.dl.rules.rules.refinement.TopDownBoundedRefinement;

import static edu.uff.dl.rules.test.App.redirectOutputStream;

import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.CLIArgumentsParser;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to call the program by command line interface using Hill Climb
 * Strategy. This class can be used to generate rules for each example on the
 * trainer file, refine and evaluate the rules against test fold using cross
 * validation.
 *
 * @author Victor Guimarães
 */
public class DLRulesHillClimbingCLI extends DLRulesCLI {

    private Set<Literal> positiveTrain;
    private Set<Literal> negativeTrain;
    private StringBuilder fullDLPContent;

    private int sideWayMoves = -1;
    private double theoryThreshold = 0.0;
    private String theory;

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
     * fold's prefix name.
     * @throws FileNotFoundException in case of a file path does not exist.
     */
    public static void main(String[] args) throws FileNotFoundException {
        CLIArgumentsParser ap = new CLIArgumentsParser(args);
        DLRulesHillClimbingCLI dlrcli;
        try {
            dlrcli = new DLRulesHillClimbingCLI(ap.dlpFilepaths, ap.owlFilepath, ap.positiveTrainFilepath, ap.negativeTrainFilepath, ap.outputDirectory, ap.timeout, ap.templateFilepath, ap.cvDirectory, ap.cvPrefix, ap.cvNumberOfFolds);
            dlrcli.setRule(ap.rule);
            dlrcli.setRefinement(ap.ref);
            dlrcli.setGeneric(ap.generic);
            dlrcli.setCrossValidation(ap.cv);
            dlrcli.setRecursiveRuleAllowed(!ap.noRec);
            dlrcli.setDepth(ap.depth);
            dlrcli.setThreshold(ap.threshold);
            dlrcli.setSideWayMoves(ap.sideWayMoves);
            dlrcli.setTheoryThreshold(ap.theoryThreshold);
            dlrcli.setInitArgs(args);
            dlrcli.init();
        } catch (ParseException ex) {
            Logger.getLogger(DLRulesHillClimbingCLI.class.getName()).log(Level.SEVERE, null, ex);
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
     * @throws FileNotFoundException in case of any file path does not exist.
     * @throws org.semanticweb.drew.dlprogram.parser.ParseException
     */
    public DLRulesHillClimbingCLI(Set<String> dlpFilepaths, String owlFilepath, String positiveTrainFilepath, String negativeTrainFilepath, String outputDirectory, int timeout, String templateFilepath, String cvDirectory, String cvPrefix, int cvNumberOfFolds) throws FileNotFoundException, ParseException {
        super(dlpFilepaths, owlFilepath, positiveTrainFilepath, negativeTrainFilepath, outputDirectory, timeout, templateFilepath, cvDirectory, cvPrefix, cvNumberOfFolds);
        positiveTrain = FileContent.getExamplesLiterals(positiveTrainExample);
        negativeTrain = FileContent.getExamplesLiterals(negativeTrainExample);
        fullDLPContent = new StringBuilder();
        fullDLPContent.append(dlpContent).append("\n");
        sideWayMoves = -1;
    }

    @Override
    protected void refinement() {
        StringBuilder t = new StringBuilder();
        
        double measure = 0, aux;
        Box<Long> b = new Box<>(null), e = new Box(null);
        try {
            redirectOutputStream(outRefinement + "statistics.txt");

            System.out.println("Begin time:\t" + Time.getTime(b));
            RuleMeasurer ruleMeasure = new LaplaceMeasure();
            Set<Literal> positiveExamples, negativeExamples;
            positiveExamples = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeExamples = FileContent.getExamplesLiterals(negativeTrainExample);
            EvaluatedRule er = new EvaluatedRule(null, positiveExamples.size(), negativeExamples.size(), 0, 0, ruleMeasure);
            measure = er.getMeasure();
            EvaluatedRuleExample serializeRule = null;
            //double threshold = 0.01;
            int sideMoves = 0;
            List <EvaluatedRuleExample> listRules = getERESorted();
            for (EvaluatedRuleExample genericRuleExample : listRules) {
                try {
                    System.out.println(Time.getTime(b));
                    System.out.println("File: " + genericRuleExample.getSerializedFile().getName());

                    Refinement r = new TopDownBoundedRefinement(drewArgs, dlpContent, genericRuleExample, threshold, positiveExamples, negativeExamples, timeout, ruleMeasure);
                    r.start();
                    r.join();

                    String fileName = genericRuleExample.getSerializedFile().getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String outPath = outRefinementAll + fileName + "_";

                    Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                    List<Integer> keys = new ArrayList<>(rules.keySet());
                    if (keys.isEmpty()) {
                        continue;
                    }

                    Collections.sort(keys);
                    
                    Time.getTime(e);

                    File outputFile;
                    Integer biggestKey = 0;

                    for (Integer key : keys) {
                        outputFile = new File(outPath + key + ".txt");
                        serializeRule = new EvaluatedRuleExample(rules.get(key), genericRuleExample.getExample());
                        serializeRule.serialize(outputFile);
                        if (key > biggestKey) {
                            biggestKey = key;
                        }
                    }

                    outPath = outRefinement + fileName;
                    outputFile = new File(outPath + ".txt");

                    int refinedRuleIndex = keys.size() - 1;
                    serializeRule = new EvaluatedRuleExample(rules.get(keys.get(refinedRuleIndex)), genericRuleExample.getExample(), ruleMeasure);
                    double localMeasure = serializeRule.getMeasure();
                    if (generic) {
                        EvaluatedRuleExample otherRule;
                        double otherMeasure;
                        for (int i = refinedRuleIndex - 1; i > -1; i--) {
                            otherRule = new EvaluatedRuleExample(rules.get(keys.get(i)), genericRuleExample.getExample(), ruleMeasure);
                            otherMeasure = otherRule.getMeasure();
                            if (otherMeasure == localMeasure) {
                                localMeasure = otherMeasure;
                                serializeRule = otherRule;
                            }
                        }
                    }

                    serializeRule.serialize(outputFile);

                    System.out.println(Time.getTime(e));
                    double dif = e.getContent() - b.getContent();
                    dif /= 1000;
                    System.out.println("Total time for file(" + genericRuleExample.getSerializedFile().getName() + "): " + dif + "s");
                    System.out.println("\n");
                } catch (FileNotFoundException | InterruptedException ex) {
                    Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (serializeRule != null) {
                    aux = evaluateRule(serializeRule, ruleMeasure, measure);
                    if (aux - measure >= theoryThreshold) {
                        measure = aux;
                        sideMoves = 0;
                        if (serializeRule.getRule() != null) {
                            fullDLPContent.append(serializeRule.getRule().toString()).append("\n");
                            //Get positive covered examples
                            //Remove from listRules rules whose based examples was covered
                            //add rule to theory if there is no equivalant rule already
                        }
                    } else {
                        sideMoves++;
                        if (sideWayMoves > 0 && sideMoves > this.sideWayMoves) {
                            break;
                        }
                    }
                }

            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(DLRulesHillClimbingCLI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(DLRulesHillClimbingCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        theory = t.toString().trim();
        System.out.println("End time:\t" + Time.getTime(e));
        System.out.println("Total time:\t" + Time.getDiference(b, e));
    }

    /**
     * Evaluate the current rule with all the previous rules to get a overall
     * measure of the set of rules.
     * <br> Case can not infer the rule, returns the same measure.
     *
     * @param rule the current rule.
     * @param ruleMeasure the measure function.
     * @param measure the previous measure.
     * @return the measure of the set of rules so far.
     * @throws TimeoutException case not infer the rule, returns the same
     * measure.
     */
    private double evaluateRule(EvaluatedRuleExample rule, RuleMeasurer ruleMeasure, double measure) throws TimeoutException {
        RuleEvaluator re = new RuleEvaluator(rule.getRule(), drewArgs, fullDLPContent.toString(), positiveTrain, negativeTrain);
        EvaluatedRule er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
        if (er == null) {
            return measure;
        }
        er.setRuleMeasureFunction(ruleMeasure);
        return er.getMeasure();
    }

    /**
     * Sort the rules based on the compression measure.
     * {@link CompressionMeasure}
     *
     * @return the sorted array of rules.
     * @throws FileNotFoundException in case a rule file could not be found.
     */
    @SuppressWarnings("Convert2Lambda")
    private List<EvaluatedRuleExample> getERESorted() throws IOException {
        @SuppressWarnings("Convert2Lambda")
        File[] erRules = (new File(outER)).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("rule");
            }
        });

        List<EvaluatedRuleExample> evaluatedRuleExamples = new ArrayList<>(erRules.length);

        int i = 0;
        RuleMeasurer measure = new CompressionMeasure();
        EvaluatedRuleExample evaluatedRuleExample;
        for (File file : erRules) {
            evaluatedRuleExample = new EvaluatedRuleExample(file);
            evaluatedRuleExample.setRuleMeasureFunction(measure);
            evaluatedRuleExamples.add(evaluatedRuleExample);
            i++;
        }

        Collections.sort(evaluatedRuleExamples, new Comparator<EvaluatedRuleExample>() {

            @Override
            public int compare(EvaluatedRuleExample o1, EvaluatedRuleExample o2) {
                if (o1.getMeasure() > o2.getMeasure()) {
                    return -1;
                } else if (o1.getMeasure() == o2.getMeasure()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        return evaluatedRuleExamples;
    }

    public int getSideWayMoves() {
        return sideWayMoves;
    }

    public void setSideWayMoves(int sideWayMoves) {
        this.sideWayMoves = sideWayMoves;
    }

    public double getTheoryThreshold() {
        return theoryThreshold;
    }

    public void setTheoryThreshold(double theoryThreshold) {
        this.theoryThreshold = theoryThreshold;
    }

    public String getTheory() {
        return theory;
    }

}
