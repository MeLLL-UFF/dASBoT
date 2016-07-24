/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.cli;

import br.uff.dl.rules.cli.parallel.DLRulesCLIParallel;
import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.datalog.DataLogLiteral;
import br.uff.dl.rules.drew.DReWRLCLILiteral;
import br.uff.dl.rules.evaluation.CompressionMeasure;
import br.uff.dl.rules.evaluation.F1ScoreMeasure;
import br.uff.dl.rules.evaluation.LaplaceMeasure;
import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.exception.TimeoutException;
import br.uff.dl.rules.rules.Rule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import br.uff.dl.rules.rules.refinement.Refinement;
import br.uff.dl.rules.rules.refinement.RefinementFactory;
import br.uff.dl.rules.util.Box;
import br.uff.dl.rules.util.FileContent;
import br.uff.dl.rules.util.Time;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static br.uff.dl.rules.rules.theory.DLRulesTheoryBuilder.compareAnswerSetWithExample;

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
    private RuleMeasurer theoryMeasurer = new F1ScoreMeasure();
    private Set<Rule> theoryRules = new LinkedHashSet<>();

    /*
     sideWayMoves = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : -1);
     theoryThreshold = (!queue.isEmpty() ? Double.parseDouble(queue.remove()) : 0.0);
     */
    @Override
    public Queue<String> parseArguments(String[] args) throws FileNotFoundException {
        Queue<String> queue = super.parseArguments(args);

        this.sideWayMoves = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : sideWayMoves);
        this.theoryThreshold = (!queue.isEmpty() ? Double.parseDouble(queue.remove()) : theoryThreshold);

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
        sideWayMoves = -1;
    }

    public DLRulesHillClimbingCLI() {
    }

    @Override
    protected void refinement() {

        double measure = 0, aux;
        Box<Long> b = new Box<>(null), e = new Box(null);
        try (PrintStream outStream = new PrintStream(outRefinement + "statistics.txt")) {
            positiveTrain = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeTrain = FileContent.getExamplesLiterals(negativeTrainExample);
            fullDLPContent = new StringBuilder();
            fullDLPContent.append(dlpContent).append("\n");

//            redirectOutputStream(outRefinement + "statistics.txt");
            outStream.println("Begin time:\t" + Time.getTime(b));
            RuleMeasurer ruleMeasure = new LaplaceMeasure();
            Set<Literal> positiveExamples, negativeExamples;
            Set<ConcreteLiteral> positiveCoveredExamples = new LinkedHashSet<>();
            positiveExamples = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeExamples = FileContent.getExamplesLiterals(negativeTrainExample);
            EvaluatedRule er = new EvaluatedRule(null, positiveExamples.size(), negativeExamples.size(), 0, 0, ruleMeasure);
            measure = er.getMeasure();
            EvaluatedRuleExample serializeRule = null;
            //double threshold = 0.01;
            int sideMoves = 0;
            List<EvaluatedRuleExample> listRules = getERESorted();
            for (EvaluatedRuleExample genericRuleExample : listRules) {
                if (genericRuleExample.getRule().isEquivalentToAny(theoryRules)) {
                    outStream.println("File: " + genericRuleExample.getSerializedFile().getName());
                    outStream.println("Rule not refined because a equivalent rule was already refined!");
                    continue;
                }
                if (positiveCoveredExamples.contains(genericRuleExample.getExample())) {
                    outStream.println("File: " + genericRuleExample.getSerializedFile().getName());
                    outStream.println("Rule not refined because its base example was already covered!");
                    continue;
                }
                File file = null;
                try {
                    file = genericRuleExample.getSerializedFile();

                    outStream.println(Time.getTime(b));
                    outStream.println("File: " + file.getName());

                    Refinement r = RefinementFactory.getRefinement(refinementClass);
                    r.setArgs(drewArgs);
                    r.setDlpContent(dlpContent);
                    r.setBoundRule(genericRuleExample);
                    r.setThreshold(threshold);
                    r.setPositiveExamples(positiveExamples);
                    r.setNegativeExamples(negativeExamples);
                    r.setTimeout(timeout);
                    r.setRuleMeasure(refinementRuleMeasure);
                    r.setOutStream(outStream);

                    r.start();
                    r.join();
                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String outPath = outRefinementAll + fileName + "_";

                    Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                    List<Integer> keys = new ArrayList<>(rules.keySet());
                    if (keys.isEmpty()) {
                        if (genericRuleExample.getPositives() != 0) {
                            keys.add(genericRuleExample.getRule().getBody().size());
                            rules.put(genericRuleExample.getRule().getBody().size(), genericRuleExample);
                        } else {
                            continue;
                        }
                    }

                    Collections.sort(keys);
                    Time.getTime(e);

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
                                serializeRule = otherRule;
                            }
                        }
                    }

                    serializeRule.serialize(outputFile);

                    outStream.println(Time.getTime(e));
                    double dif = e.getContent() - b.getContent();
                    dif /= 1000;
                    outStream.println("Total time for file(" + file.getName() + "): " + dif + "s");
                    outStream.println("\n");
                } catch (IOException | InterruptedException | NullPointerException ex) {
                    System.out.println("DLV Error! 1");
                    Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    System.out.println("DLV Error! 2");
                    Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (serializeRule == null) {
                    continue;
                }

                if (serializeRule.getRule().isEquivalentToAny(theoryRules)) {
                    continue;
                }

                aux = evaluateRule(serializeRule, positiveCoveredExamples);

                if (aux - measure >= theoryThreshold) {
                    measure = aux;
                    sideMoves = 0;
                    if (serializeRule.getRule() != null) {
                        fullDLPContent.append(serializeRule.getRule().toString()).append("\n");
                        theoryRules.add(serializeRule.getRule());
                    }
                } else {
                    sideMoves++;
                    if (sideWayMoves > 0 && sideMoves > this.sideWayMoves) {
                        break;
                    }
                }

            }

            outStream.println("End time:\t" + Time.getTime(e));
            outStream.println("Total time:\t" + Time.getDiference(b, e));
        } catch (IOException | ParseException ex) {
            Logger.getLogger(DLRulesHillClimbingCLI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(DLRulesHillClimbingCLI.class.getName()).log(Level.SEVERE, null, ex);
        }

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
    private double evaluateRule(EvaluatedRuleExample rule, Set<ConcreteLiteral> positiveCoveredExamples) throws TimeoutException, ParseException, FileNotFoundException {
//        RuleEvaluator re = new RuleEvaluator(rule.getRule(), drewArgs, fullDLPContent.toString(), positiveTrain, negativeTrain);
//        EvaluatedRule er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
//      return re;

        DReWRLCLILiteral drew = DReWRLCLILiteral.get(drewArgs);
        drew.setDLPContent(fullDLPContent + "\n" + rule.getRule().toString());
        drew.go();
//        if (drew.getLiteralModelHandler().getAnswerSets().isEmpty())
//            throw new ParseException();
        //lits = drew.getLiteralModelHandler().getAnswerSets().get(0);
        Set<Literal> lits = new HashSet<>();
        for (Set<Literal> set : drew.getLiteralModelHandler().getAnswerSets()) {
            lits.addAll(set);
        }

        Set<Literal> posExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(positiveTrainFilePath));
        Set<Literal> negExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(negativeTrainFilePath));

        int positives = positiveTrain.size();
        Set<ConcreteLiteral> covered = DataLogLiteral.getSetOfLiterals(compareAnswerSetWithExample(lits, posExamples));
        int positivesCovered = covered.size();

        int negatives = negativeTrain.size();
        int negativesCovered = compareAnswerSetWithExample(lits, negExamples).size();

        positiveCoveredExamples.addAll(covered);

        EvaluatedRule er = new EvaluatedRule(null, positives, negatives, positivesCovered, negativesCovered, theoryMeasurer);

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

        RuleMeasurer measure = new CompressionMeasure();
        EvaluatedRuleExample evaluatedRuleExample;
        for (File file : erRules) {
            evaluatedRuleExample = new EvaluatedRuleExample(file);
            evaluatedRuleExample.setRuleMeasureFunction(measure);
            evaluatedRuleExamples.add(evaluatedRuleExample);
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

    public Set<Rule> getTheoryRules() {
        return theoryRules;
    }

    public RuleMeasurer getTheoryMeasurer() {
        return theoryMeasurer;
    }

    public void setTheoryMeasurer(RuleMeasurer theoryMeasurer) {
        this.theoryMeasurer = theoryMeasurer;
    }

}
