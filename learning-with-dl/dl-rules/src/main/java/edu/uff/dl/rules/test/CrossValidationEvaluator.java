/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.stanford.nlp.io.PrintFile;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRuleExample;
import edu.uff.dl.rules.rules.avaliation.RuleMeasurer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to evaluate the rule against the folds of the cross validation.
 *
 * @author Victor Guimarães
 */
public class CrossValidationEvaluator implements Runnable {

    private String cvDirectory;
    private String foldPrefix;
    private String prefixSeparator = "_";
    private int numberOfFolds;

    private RuleMeasurer ruleMeasure;

    private Map<String, double[]> rulesMeasures;

    private String description;

    private DecimalFormat df = new DecimalFormat("#.#####");

    private NameFilterCompare nameFilterCompare;

    /**
     * Constructor with all needed parameters.
     *
     * @param cvDirectory the directory with the folds.
     * @param foldPrefix the fold's prefix.
     * @param numberOfFolds the number of folds (the folds should have the
     * number after its prefix and must be in [1,{@link #numberOfFolds}].
     * @param ruleMeasure the measurer of rules.
     */
    public CrossValidationEvaluator(String cvDirectory, String foldPrefix, int numberOfFolds, RuleMeasurer ruleMeasure) {
        this.cvDirectory = cvDirectory;
        this.foldPrefix = foldPrefix;
        this.numberOfFolds = numberOfFolds;
        this.ruleMeasure = ruleMeasure;
        this.rulesMeasures = new HashMap<>();
        this.nameFilterCompare = new NameFilterCompare("rule");
    }

    /**
     * Constructor with all needed parameters. With addition of the
     * {@link #prefixSeparator}, a {@link String} to separate the rule's name
     * from the fold's name on the output files. This separator is "_"
     * (underscore) by default.
     *
     * @param cvDirectory the directory with the folds.
     * @param foldPrefix the fold's prefix.
     * @param numberOfFolds the number of folds (the folds should have the
     * number after its prefix and must be in [1,{@link #numberOfFolds}].
     * @param ruleMeasure the measurer of rules.
     * @param prefixSeparator the {@link #prefixSeparator}, a {@link String} to
     * separate the rule's name from the fold's name on the output files.
     */
    public CrossValidationEvaluator(String cvDirectory, String foldPrefix, int numberOfFolds, RuleMeasurer ruleMeasure, String prefixSeparator) {
        this(cvDirectory, foldPrefix, numberOfFolds, ruleMeasure);
        this.prefixSeparator = prefixSeparator;
    }

    @Override
    public void run() {
        File[] listOfFiles = (new File(cvDirectory)).listFiles(nameFilterCompare);
        EvaluatedRuleExample evaluatedRuleExample;
        String ruleName;
        int index;

        for (File file : listOfFiles) {
            try {
                evaluatedRuleExample = new EvaluatedRuleExample(file);
                evaluatedRuleExample.setRuleMeasureFunction(ruleMeasure);
                ruleName = file.getName();
                ruleName = ruleName.substring(0, ruleName.lastIndexOf("."));
                index = Integer.parseInt(ruleName.substring(ruleName.indexOf(foldPrefix) + foldPrefix.length())) - 1;
                ruleName = ruleName.substring(0, ruleName.indexOf(prefixSeparator));
                insertMeasure(ruleName, index, evaluatedRuleExample.getMeasure());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrossValidationEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        loadResult();
    }

    /**
     * Method to insert the rule into the {@link Map} of measures.
     *
     * @param rule the rule.
     * @param index the index of the avaliated fold.
     * @param measure the measure.
     */
    private void insertMeasure(String rule, int index, double measure) {
        if (!rulesMeasures.containsKey(rule)) {
            double[] measures = new double[numberOfFolds];
            rulesMeasures.put(rule, measures);
        }

        rulesMeasures.get(rule)[index] = measure;
    }

    /**
     * Format and load the results into a {@link String} variable.
     */
    private void loadResult() {
        String[] evaluations = new String[rulesMeasures.size()];

        int i = 0;
        double[] measures;
        String evaluation;

        String bestRule = "", worstRule = "";
        double maxValue = Double.MIN_VALUE, minValue = Double.MAX_VALUE;
        int bestIndex = 1, worstIndex = 1, baux, waux;

        for (String rule : rulesMeasures.keySet()) {
            measures = rulesMeasures.get(rule);
            evaluation = rule;
            evaluation += ":\t";
            try {
                if (Integer.parseInt(rule.substring("rule".length())) < 100) {
                    evaluation += "\t";
                }
            } catch (NumberFormatException ex) {

            }
            evaluation += formatMeasures(measures);
            evaluations[i] = evaluation;
            i++;

            baux = bestIndex(measures);
            waux = worstIndex(measures);
            if (measures[baux] > maxValue) {
                maxValue = measures[baux];
                bestRule = rule;
                bestIndex = baux;
            }

            if (measures[waux] < minValue) {
                minValue = measures[waux];
                worstRule = rule;
                worstIndex = waux;
            }
        }

        Arrays.sort(evaluations, nameFilterCompare);

        description = "";
        i = 0;
        for (String eval : evaluations) {
            description += eval + "\n";
            i++;
        }

        description += "Number of rules:\t" + i;

        description += "\n";

        description += "Best Rule:\t";
        description += bestRule;
        try {
            if (Integer.parseInt(bestRule.substring("rule".length())) < 100) {
                description += "\t";
            }
        } catch (NumberFormatException ex) {

        }
        description += "\tfor fold:\t";
        description += (bestIndex + 1);

        description += "\n";

        description += "Worst Rule:\t";
        description += worstRule;
        try {
            if (Integer.parseInt(worstRule.substring("rule".length())) < 100) {
                description += "\t";
            }
        } catch (NumberFormatException ex) {

        }
        description += "\tfor fold:\t";
        description += (worstIndex + 1);
    }

    /**
     * Format the array of results.
     *
     * @param measures the array.
     * @return the formated {@link String}.
     */
    private String formatMeasures(double[] measures) {
        String format = "";
        double total = 0;
        for (int i = 0; i < measures.length; i++) {
            format += foldPrefix + (i + 1) + ":\t" + df.format(measures[i]) + "\t";
            total += measures[i];
        }
        total = (total / measures.length);
        format += "Final:\t" + df.format(total);

        return format;
    }

    /**
     * Finds the index of the best measure from a array of measures. The array
     * of measure is an array with the measure of a rule from each fold, each
     * index of the array is a measure of a fold.
     *
     * @param measures the array of measures.
     * @return the index.
     */
    private int bestIndex(double[] measures) {
        double max = Double.MIN_VALUE;
        int index = -1;
        for (int i = 0; i < measures.length; i++) {
            if (measures[i] > max) {
                max = measures[i];
                index = i;
            }
        }

        return index;
    }

    /**
     * Finds the index of the worst measure from a array of measures. The array
     * of measure is an array with the measure of a rule from each fold, each
     * index of the array is a measure of a fold.
     *
     * @param measures the array of measures.
     * @return the index.
     */
    private int worstIndex(double[] measures) {
        double min = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < measures.length; i++) {
            if (measures[i] < min) {
                min = measures[i];
                index = i;
            }
        }

        return index;
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * Saves the result into a text file.
     *
     * @param filepath the filepath.
     * @throws IOException in case something goes wrong during writing the file.
     */
    public void saveToFile(String filepath) throws IOException {
        PrintFile pf = new PrintFile(filepath);
        pf.print(description);
        pf.close();
    }

}

/**
 * Class to filter the files for rule's files and sort they.
 *
 * @author Victor Guimarães
 */
class NameFilterCompare implements FilenameFilter, Comparator<String> {

    private String rulePrefix;

    /**
     * Constructor with all needed parameters.
     *
     * @param rulePrefix the prefix of the rule's files.
     */
    public NameFilterCompare(String rulePrefix) {
        this.rulePrefix = rulePrefix;
    }

    @Override
    public boolean accept(File dir, String name) {
        return (name.startsWith(rulePrefix));
    }

    @Override
    public int compare(String o1, String o2) {
        int size = rulePrefix.length();
        double d1, d2;

        d1 = Double.parseDouble(o1.substring(o1.lastIndexOf("\t"), o1.length()).trim().replaceAll(",", "."));
        d2 = Double.parseDouble(o2.substring(o2.lastIndexOf("\t"), o2.length()).trim().replaceAll(",", "."));

        if (d1 > d2)
            return -1;
        else if (d1 < d2)
            return 1;
        else {
            int n1, n2;
            n1 = Integer.parseInt(o1.substring(size, o1.indexOf(":")));
            n2 = Integer.parseInt(o2.substring(size, o2.indexOf(":")));

            if (n1 < n2)
                return -1;
            else if (n1 > n2)
                return 1;
            else
                return 0;
        }
    }
}
