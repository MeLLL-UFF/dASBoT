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
 *
 * @author Victor
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

    public CrossValidationEvaluator(String cvDirectory, String foldPrefix, int numberOfFolds, RuleMeasurer ruleMeasure) {
        this.cvDirectory = cvDirectory;
        this.foldPrefix = foldPrefix;
        this.numberOfFolds = numberOfFolds;
        this.ruleMeasure = ruleMeasure;
        this.rulesMeasures = new HashMap<>();
    }

    public CrossValidationEvaluator(String cvDirectory, String foldPrefix, int numberOfFolds, RuleMeasurer ruleMeasure, String prefixSeparator) {
        this(cvDirectory, foldPrefix, numberOfFolds, ruleMeasure);
        this.prefixSeparator = prefixSeparator;
    }

    @Override
    public void run() {
        File[] listOfFiles = (new File(cvDirectory)).listFiles(new nameFiler());
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

    private void insertMeasure(String rule, int index, double measure) {
        if (!rulesMeasures.containsKey(rule)) {
            double[] measures = new double[numberOfFolds];
            rulesMeasures.put(rule, measures);
        }

        rulesMeasures.get(rule)[index] = measure;
    }

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

        Arrays.sort(evaluations, new EvaluationComparator());

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

    public void saveToFile(String filepath) throws IOException {
        PrintFile pf = new PrintFile(filepath);
        pf.print(description);
        pf.close();
    }

}

class nameFiler implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return (name.startsWith("rule"));
    }

}

class EvaluationComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        double d1, d2;

        d1 = Double.parseDouble(o1.substring(o1.lastIndexOf("\t"), o1.length()).trim().replaceAll(",", "."));
        d2 = Double.parseDouble(o2.substring(o2.lastIndexOf("\t"), o2.length()).trim().replaceAll(",", "."));

        if (d1 > d2)
            return -1;
        else if (d1 < d2)
            return 1;
        else {
            int n1, n2;
            n1 = Integer.parseInt(o1.substring(4, o1.indexOf(":")));
            n2 = Integer.parseInt(o2.substring(4, o2.indexOf(":")));

            if (n1 < n2)
                return -1;
            else if (n1 > n2)
                return 1;
            else
                return 0;
        }
    }

}
