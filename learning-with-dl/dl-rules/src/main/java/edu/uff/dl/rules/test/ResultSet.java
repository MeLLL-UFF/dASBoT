/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.rules.evaluation.CompressionMeasure;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.rules.evaluation.RuleMeasurer;
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
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 *
 * @author Victor Guimarães
 */
public class ResultSet {

    private String dlpContent;
    private String positiveExamples;
    private String negativeExamples;

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

    public ResultSet(String dlpContent, String positiveExamples, String negativeExamples, String outputDirectory, RuleMeasurer measurer, String[] args) throws IOException, ParseException {
        this.dlpContent = dlpContent;
        this.positiveExamples = positiveExamples;
        this.negativeExamples = negativeExamples;
        this.outputDirectory = outputDirectory;
        this.measurer = measurer;
        this.args = args;
        this.refinementStatistics = FileContent.getStringFromFile(outputDirectory + "refinement/statistics.txt");
        loadRules();
        loadResults();
    }

    private void loadRules() throws IOException {
        evaluatedRuleExamples = new LinkedList<>();
        File[] files = (new File(outputDirectory + "ER/")).listFiles();
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
            System.out.println("Measure: " + ere.getMeasure() + "\tExample: " + ere.getExample());
        }
    }

    private void loadResults() throws FileNotFoundException, ParseException {
        StringBuilder sb = new StringBuilder();
        long time = 0;

        int count = 0;
        double measure = 0.0, newMeasure;

        try {
            Set<Literal> posExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(positiveExamples));
            Set<Literal> negExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(negativeExamples));
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
        System.out.println("\n");
        System.out.println(sb.toString());
        System.out.println("\nTrain Measure:\t" + compareRule(dlpContent + "\n" + sb.toString(), literals));
        System.out.println("Positives: " + positives);
        System.out.println("Negatives: " + negatives);
        System.out.println("Covered positives: " + positivesCovered);
        System.out.println("Covered negatives: " + negativesCovered);

        if (new File(positiveExamples.replace("train", "test")).exists() && new File(negativeExamples.replace("train", "test")).exists()) {
            positiveExamples = positiveExamples.replace("train", "test");
            negativeExamples = negativeExamples.replace("train", "test");
            System.out.println("\nTest Measure:\t" + compareRule(dlpContent + "\n" + sb.toString(), literals));
            System.out.println("Positives: " + positives);
            System.out.println("Negatives: " + negatives);
            System.out.println("Covered positives: " + positivesCovered);
            System.out.println("Covered negatives: " + negativesCovered);
        }

        System.out.println("");
        System.out.println("Avaliação Inicial: " + initialMeasure);
        System.out.println("Limiar: " + threshold);
        System.out.println("Geração das regras:\t" + getGeneratedRulesTotalTime());
        System.out.println("Refinamento das regras:\t" + Time.getFormatedTime(time));
        System.out.println("Tempo total:\t\t" + Time.getFormatedTime(Time.getLongTime(getGeneratedRulesTotalTime()) + time));
    }

    private boolean containsEquivalentRule(List<EvaluatedRuleExample> rules, Rule rule) {
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
        if (drew.getLiteralModelHandler().getAnswerSets().isEmpty())
            throw new ParseException();
        //lits = drew.getLiteralModelHandler().getAnswerSets().get(0);
        Set<Literal> lits = new HashSet<>();
        for (Set<Literal> set : drew.getLiteralModelHandler().getAnswerSets()) {
            lits.addAll(set);
        }

        Set<Literal> posExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(positiveExamples));
        Set<Literal> negExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(negativeExamples));

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

    private ConcreteLiteral createConcreteLiteral(Literal literal) {
        String head = literal.getPredicate().toString();
        head = head.substring(0, Math.min(head.indexOf("/"), head.length()));
        return new DataLogLiteral(head, literal.getTerms(), literal.isNegative());
    }

    protected static Set<Literal> compareRuleWithExample(Set<Literal> literals, Set<Literal> listExamples) {
        int positive = 0;
        Set<Literal> covered = new HashSet<>();
        for (Literal s : listExamples) {
            if (literals.contains(s)) {
                //System.out.println(s);
                covered.add(s);
            }
        }
        return covered;
    }

    private long getFormatedTimeForRule(EvaluatedRuleExample rule) {
        String key = "Total time for file(" + rule.getSerializedFile().getName() + "): ";
        int index = refinementStatistics.indexOf(key) + key.length();
        long value = (long) (Double.parseDouble(refinementStatistics.substring(index, refinementStatistics.indexOf("s", index))) * 1000);
        //System.out.println("Values: " + value);
        return value;
    }

    private String getGeneratedRulesTotalTime() throws FileNotFoundException {
        String statistics = null;
        String prefix = "Total time:";
        try {
            statistics = FileContent.getStringFromFile(outputDirectory + "/globalStatistics.txt");
            statistics = statistics.substring(statistics.indexOf(":") + 1, statistics.indexOf("\n")).trim();
            if (statistics.startsWith("-")) {
                try {
                    statistics = Time.getFormatedTime(Long.parseLong(statistics.substring(1, statistics.length() - 1)));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            statistics = FileContent.getStringFromFile(outputDirectory + "/statistics.txt");
            int index = statistics.indexOf("Total time:") + 1 + prefix.length();
            statistics = statistics.substring(index, statistics.indexOf("\n", index)).trim();
        }

        return statistics;
    }

}
