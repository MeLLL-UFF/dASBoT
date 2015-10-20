/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.evaluation.CompressionMeasure;
import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 *
 * @author Victor Guimarães
 */
public class ResultSet {

    private String dlpContent;
    private String positiveTrainFilePath;
    private String negativeTrainFilePath;
    
    private String positiveTestFilePath;
    private String negativeTestFilePath;

    private String outputDirectory;

    private List<EvaluatedRuleExample> evaluatedRuleExamples;

    private double threshold = 0.0;
    private RuleMeasurer measurer;
    private String[] args;

    private String refinementStatistics;

    private int positives;
    private int positivesCovered;

    private int negatives;
    private int negativesCovered;
    private int maxSideWayMovements = -1;
    
    private StringBuilder description;

    public ResultSet(String dlpContent, String positiveTrainFilePath, String negativeTrainFilePath, String outputDirectory, RuleMeasurer measurer, String[] args) throws IOException, ParseException {
        this.dlpContent = dlpContent;
        this.positiveTrainFilePath = positiveTrainFilePath;
        this.negativeTrainFilePath = negativeTrainFilePath;
        this.outputDirectory = outputDirectory;
        this.measurer = measurer;
        this.args = args;
        this.refinementStatistics = FileContent.getStringFromFile(new File(outputDirectory, "refinement/statistics.txt"));
        description = new StringBuilder();
        loadRules();
        loadResults();
    }

    public ResultSet(String dlpContent, String positiveTrainFilePath, String negativeTrainFilePath, String positiveTestFilePath, String negativeTestFilePath, String outputDirectory, RuleMeasurer measurer, String[] args) throws IOException, ParseException {
        this(dlpContent, positiveTrainFilePath, negativeTrainFilePath, outputDirectory, measurer, args);
        this.positiveTestFilePath = positiveTestFilePath;
        this.negativeTestFilePath = negativeTestFilePath;
    }
    
    private void loadRules() throws IOException {
        evaluatedRuleExamples = new LinkedList<>();
        File[] files = (new File(outputDirectory, "ER/")).listFiles();
        RuleMeasurer compression = new CompressionMeasure();
        EvaluatedRuleExample evaluatedRuleExample;
        for (File file : files) {
            if (!file.getName().endsWith(".txt"))
                continue;
            evaluatedRuleExample = new EvaluatedRuleExample(file);
            evaluatedRuleExample.setRuleMeasureFunction(compression);
            evaluatedRuleExamples.add(evaluatedRuleExample);
        }

        EvaluatedRuleComparator com = new EvaluatedRuleComparator();
        Collections.sort(evaluatedRuleExamples, com);

        for (EvaluatedRuleExample ere : evaluatedRuleExamples) {
            description.append("Measure: ").append(ere.getMeasure()).append("\tExample: ").append(ere.getExample()).append("\n");
        }
    }

    private void loadResults() throws FileNotFoundException, ParseException {
        StringBuilder sb = new StringBuilder();
        long time = 0;

        int count = 0;
        double measure = 0.0, newMeasure;

        try {
            Set<Literal> posExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(positiveTrainFilePath));
            Set<Literal> negExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(negativeTrainFilePath));
            EvaluatedRule er = new EvaluatedRule(null, posExamples.size(), negExamples.size(), 0, 0, measurer);
            measure = er.getMeasure();
        } catch (Exception e) {

        }

        double initialMeasure = measure;
        EvaluatedRuleExample ere;
        File file;
        Set<ConcreteLiteral> literals = new HashSet<>();
        int sideMovements = 0;
        List<EvaluatedRuleExample> resultRules = new ArrayList<>();

        while (!evaluatedRuleExamples.isEmpty() && count < evaluatedRuleExamples.size()) {
            try {
                file = new File(outputDirectory + "/refinement/" + evaluatedRuleExamples.get(count).getSerializedFile().getName());
                ere = new EvaluatedRuleExample(file);
                count++;
                if (containsEquivalentRule(resultRules, ere.getRule())) {
                    continue;
                }
            } catch (Exception e) {
                count++;
                continue;
            }

            time += getFormatedTimeForRule(ere);
            newMeasure = compareRule(dlpContent + "\n" + sb.toString() + "\n" + ere.getRule(), literals);

            if (newMeasure - measure <= threshold) {
                sideMovements++;
                if (maxSideWayMovements > 0 && sideMovements > maxSideWayMovements) {
                    break;
                }
            } else {
                measure = newMeasure;
                sideMovements = 0;
                resultRules.add(ere);
                sb.append(ere.getRule()).append("\n");
                removeFromRulesList(literals);
            }
        }

//        while (Math.abs(measure - newMeasure) > threshold && !evaluatedRuleExamples.isEmpty()) {
//            measure = newMeasure;
//            try {
//                file = new File(outputDirectory + "/refinement/" + evaluatedRuleExamples.get(count).getSerializedFile().getName());
//            } catch (Exception e) {
//                count++;
//                continue;
//            }
//            
//            ere = new EvaluatedRuleExample(file);
//            if (!sb.toString().contains(ere.getRule().toString().trim())) {
//                sb.append(ere.getRule()).append("\n");
//            }
//
//            newMeasure = compareRule(dlpContent + "\n" + sb.toString(), literals);
//            removeFromRulesList(literals);
//            count++;
//            time += getFormatedTimeForRule(ere);
//        }
        description.append("\n\n");
        description.append(sb.toString().trim()).append("\n");
        description.append("\nTrain Measure:\t").append(compareRule(dlpContent + "\n" + sb.toString(), literals)).append("\n");
        description.append("Positives: ").append(positives).append("\n");
        description.append("Negatives: ").append(negatives).append("\n");
        description.append("Covered positives: ").append(positivesCovered).append("\n");
        description.append("Covered negatives: ").append(negativesCovered).append("\n");

        if (positiveTestFilePath != null && negativeTestFilePath != null
                && new File(positiveTestFilePath).exists() && new File(negativeTestFilePath).exists()) {
            positiveTrainFilePath = positiveTrainFilePath.replace("train", "test");
            negativeTrainFilePath = negativeTrainFilePath.replace("train", "test");
            description.append("\nTest Measure:\t").append(compareRule(dlpContent + "\n" + sb.toString(), literals)).append("\n");
            description.append("Positives: ").append(positives).append("\n");
            description.append("Negatives: ").append(negatives).append("\n");
            description.append("Covered positives: ").append(positivesCovered).append("\n");
            description.append("Covered negatives: ").append(negativesCovered).append("\n");
        }

        description.append("\n");
        description.append("Measure Class: ").append(measurer.getClass().getName()).append("\n");
        description.append("Initial Measure: ").append(initialMeasure).append("\n");
        description.append("Threshold: ").append(threshold).append("\n");
        description.append("Rules generation:\t").append(getGeneratedRulesTotalTime()).append("\n");
        description.append("Rule refinement:\t").append(Time.getFormatedTime(time)).append("\n");
        description.append("Total time:\t\t").append(Time.getFormatedTime(Time.getLongTime(getGeneratedRulesTotalTime()) + time)).append("\n");
    }

    private static boolean containsEquivalentRule(List<EvaluatedRuleExample> rules, Rule rule) {
        for (EvaluatedRuleExample r : rules) {
            if (rule.isEquivalent(r.getRule())) {
                return true;
            }
        }

        return false;
    }

    private double compareRule(String in, Set<ConcreteLiteral> literals) throws ParseException, FileNotFoundException {
        DReWRLCLILiteral drew = DReWRLCLILiteral.get(args);
        drew.setDLPContent(in);
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

        positives = posExamples.size();
        Set<Literal> covered = compareRuleWithExample(lits, posExamples);
        positivesCovered = covered.size();

        negatives = negExamples.size();
        negativesCovered = compareRuleWithExample(lits, negExamples).size();

        for (Literal literal : covered) {
            literals.add(createConcreteLiteral(literal));
        }

        EvaluatedRule er = new EvaluatedRule(null, positives, negatives, positivesCovered, negativesCovered, measurer);
        return er.getMeasure();
    }

    private void removeFromRulesList(Set<ConcreteLiteral> literals) {
        Iterator<EvaluatedRuleExample> it = evaluatedRuleExamples.iterator();
        while (it.hasNext()) {
            if (literals.contains(it.next().getExample()))
                it.remove();
        }
    }

    private static ConcreteLiteral createConcreteLiteral(Literal literal) {
        String head = literal.getPredicate().toString();
        head = head.substring(0, Math.min(head.indexOf("/"), head.length()));
        return new DataLogLiteral(head, literal.getTerms(), literal.isNegative());
    }

    protected static Set<Literal> compareRuleWithExample(Set<Literal> literals, Set<Literal> listExamples) {
        Set<Literal> covered = new HashSet<>();
        for (Literal s : listExamples) {
            if (literals.contains(s)) {
                covered.add(s);
            }
        }
        return covered;
    }

    private long getFormatedTimeForRule(EvaluatedRuleExample rule) {
        String key = "Total time for file(" + rule.getSerializedFile().getName() + "): ";
        int index = refinementStatistics.lastIndexOf(key) + key.length();
        long value = (long) (Double.parseDouble(refinementStatistics.substring(index, refinementStatistics.indexOf("s", index))) * 1000);
        
        return value;
    }

    private String getGeneratedRulesTotalTime() throws FileNotFoundException {
        String statistics = null;
        try {
            statistics = getTimeFromSource(outputDirectory + "/globalStatistics.txt", "Rules Total Time:");
        } catch (IOException e) {
            try {
                statistics = getTimeFromSource(outputDirectory + "/statistics.txt", "Total time:");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return statistics;
    }

    private static String getTimeFromSource(String sourceFile, String prefix) throws IOException {
        long time = 0;
        List<String> lines = FileUtils.readLines(new File(sourceFile));
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                time += Time.getLongTime(line.substring(prefix.length() + 1).trim());
            }
        }

        return Time.getFormatedTime(time);
    }

    @Override
    public String toString() {
        return description.toString().trim();
    }

}
