/*
 * Copyright (C) 2016 Victor Guimarães
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.theory;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.evaluation.CompressionMeasure;
import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.rules.evaluation.DescendingMeasurableComparator;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import br.uff.dl.rules.rules.evaluation.MeasurableRule;
import br.uff.dl.rules.rules.evaluation.MeasurableRuleExample;
import br.uff.dl.rules.util.FileContent;
import br.uff.dl.rules.util.Time;
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author Victor Guimarães
 */
public class DLRulesTheoryBuilder extends TheoryBuilder {

    private String outputDirectory;

    private String refinementStatistics;

    private long time;

    public DLRulesTheoryBuilder(String dlpContent,
                                String positiveTrainFilePath,
                                String negativeTrainFilePath,
                                RuleMeasurer measurer,
                                String[] args,
                                String outputDirectory) throws IOException, ParseException {
        super(dlpContent, positiveTrainFilePath, negativeTrainFilePath, measurer, args);
        this.outputDirectory = outputDirectory;
        this.refinementStatistics = FileContent.getStringFromFile(new File(outputDirectory, "refinement/statistics" +
                ".txt"));
    }

    public DLRulesTheoryBuilder(String dlpContent,
                                String positiveTrainFilePath,
                                String negativeTrainFilePath,
                                RuleMeasurer measurer,
                                String[] args,
                                String outputDirectory,
                                double threshold,
                                int maxSideWayMovements) throws IOException, ParseException {
        this(dlpContent, positiveTrainFilePath, negativeTrainFilePath, measurer, args, outputDirectory);
        this.threshold = threshold;
        this.maxSideWayMovements = maxSideWayMovements;
    }

    @Override
    protected void init() {
        this.time = 0;
    }

    @Override
    protected void loadRules() throws IOException {
        File[] files = (new File(outputDirectory, "ER/")).listFiles((dir, name) -> name.endsWith(".txt"));
        List<EvaluatedRuleExample> extractedRules = new ArrayList<>(files.length);
        RuleMeasurer compression = new CompressionMeasure();
        EvaluatedRuleExample evaluatedRuleExample;

        for (File file : files) {
//            if (!file.getName().endsWith(".txt")) {
//                continue;
//            }
            evaluatedRuleExample = new EvaluatedRuleExample(file);
            evaluatedRuleExample.setRuleMeasureFunction(compression);
            extractedRules.add(evaluatedRuleExample);
        }

        DescendingMeasurableComparator com = new DescendingMeasurableComparator();
        Collections.sort(extractedRules, com);

        this.evaluatedRuleExamples = new ArrayList<>();
        File file;
        EvaluatedRuleExample refinedRule;
        for (EvaluatedRuleExample ere : extractedRules) {
            description.append("Measure: ").append(ere.getMeasure()).append("\tExample: ").append(ere.getExample());
//                    .append("\n");

            file = new File(outputDirectory + "/refinement/" + ere.getSerializedFile().getName());
            if (!file.exists()) {
                description.append("\t --- NOT REFINED ---\n");
                continue;
            }
            description.append("\n");

            refinedRule = new EvaluatedRuleExample(file);
            this.evaluatedRuleExamples.add(refinedRule);
        }
    }

    @Override
    public Theory buildTheory() throws FileNotFoundException, ParseException {
        this.theory = super.buildTheory();

        String rulesTotalTime = getGeneratedRulesTotalTime();
        description.append("\nRules generation:\t").append(rulesTotalTime).append("\n");
        description.append("Rule refinement:\t").append(Time.getFormatedTime(time)).append("\n");
        description.append("Total time:\t\t").append(Time.getFormatedTime(Time.getLongTime(rulesTotalTime) + time))
                .append("\n");

        return theory;
    }

    @Override
    protected void candidateRuleToTheory(MeasurableRule candidate) {
        if (candidate instanceof EvaluatedRuleExample) {
            time += getFormattedTimeForRule((EvaluatedRuleExample) candidate);
        }
    }

    @Override
    protected void ruleAccepted(Set<Literal> theoryProvedLiterals) {
        removeFromRulesList(literalsToConcreteLiterals(theoryProvedLiterals));
    }

    protected void removeFromRulesList(Set<ConcreteLiteral> literals) {
        Iterator<MeasurableRule> it = evaluatedRuleExamples.iterator();
        MeasurableRule measurableRule;
        while (it.hasNext()) {
            measurableRule = it.next();
            if (measurableRule instanceof MeasurableRuleExample) {
                if (literals.contains(((MeasurableRuleExample) measurableRule).getExample())) {
                    it.remove();
                }
            }
        }
    }

    private long getFormattedTimeForRule(EvaluatedRuleExample rule) {
        String key = "Total time for file(" + rule.getSerializedFile().getName() + "): ";
        int index = refinementStatistics.lastIndexOf(key) + key.length();
        long value = (long) (Double.parseDouble(refinementStatistics.substring(index, refinementStatistics.indexOf
                ("s", index))) * 1000);

        return value;
    }

    private String getGeneratedRulesTotalTime() throws FileNotFoundException {
        String statistics;
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

}
