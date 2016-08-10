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

package br.uff.dl.rules.util;

import br.uff.dl.rules.drew.DReWRLCLILiteral;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.drew.dlprogram.model.Literal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static br.uff.dl.rules.util.FileContent.getExamplesLiterals;

/**
 * Created by Victor Guimarães on 24/06/16.
 */
public class DReWPerformanceTest {

    public static final String DEFAULT_ENCODE = "UTF8";

    public static void main(String[] args) throws Exception {
        long t0, t1, tt0, tt1;
        tt0 = System.currentTimeMillis();
        double loadKB, transformKB, loadRules, checkEvaluation;
        int numberOfTest = Integer.parseInt(args[2]);

        System.out.println("DLV_PATH:\t" + System.getenv("DLV_PATH"));
        System.out.println();

        t0 = System.currentTimeMillis();
//        String dlpContent = getDLPContentFromFile(args[0]);
        String dlpContent = FileUtils.readFileToString(new File(args[0]), DEFAULT_ENCODE);
        t1 = System.currentTimeMillis();
        loadKB = (t1 - t0) / 1000.0;
        System.out.printf("Time to load KB:\t%.3f%s\n", loadKB, "s");

        t0 = System.currentTimeMillis();
//        String[] rules = getRulesFromFile(args[1]).split("\n");
        String[] rules = FileUtils.readFileToString(new File(args[1]), DEFAULT_ENCODE).split("\n");
        t1 = System.currentTimeMillis();
        loadRules = (t1 - t0) / 1000.0;
        System.out.printf("Time to load Rules:\t%.3f%s\n", loadRules, "s");

        runDReW(dlpContent, rules[0]);

        t0 = System.currentTimeMillis();
        Set<Literal> kb = getExamplesLiterals(dlpContent);
        t1 = System.currentTimeMillis();
        transformKB = (t1 - t0) / 1000.0;
        System.out.printf("Time to transform KB representation:\t%.3f%s\n", transformKB, "s");
        System.out.println("\n");

        String msg;
        Set<Literal> evaluated;
        Double evaluationTimes[][] = new Double[rules.length][numberOfTest];
        int numberInferredFacts[] = new int[rules.length];
        for (int i = 0; i < rules.length; i++) {
            msg = "Rule [" + (i + 1) + "\\" + rules.length + "]:\t" + rules[i];
            System.out.println(StringUtils.repeat("-", msg.length() + 5));
            System.out.println(msg);
            System.out.println();

            for (int j = 0; j < numberOfTest; j++) {
                t0 = System.currentTimeMillis();
                evaluated = runDReW(dlpContent, rules[i]);
                t1 = System.currentTimeMillis();
                evaluationTimes[i][j] = (t1 - t0) / 1000.0;

                t0 = System.currentTimeMillis();

                if (!evaluated.containsAll(kb)) {
                    throw new Exception("KB not contained in evaluated set!");
                }

                Set<Literal> remaining = new HashSet<>(evaluated);
                remaining.removeAll(kb);
                t1 = System.currentTimeMillis();
                checkEvaluation = (t1 - t0) / 1000.0;

                System.out.println("Evaluation[" + (j + 1) + "\\" + numberOfTest + "]:");
                System.out.printf("Time to eval:\t%.3f%s\n", evaluationTimes[i][j], "s");
                System.out.printf("Time to check:\t%.3f%s\n", checkEvaluation, "s");

                System.out.println("KB Size:\t\t" + kb.size());
                System.out.println("Eval Size:\t\t" + evaluated.size());

                if (j > 0 && numberInferredFacts[i] != remaining.size()) {
                    throw new Exception("Nondeterministic number of inferred facts!");
                }

                numberInferredFacts[i] = remaining.size();
                System.out.println("Remaining Size:\t" + numberInferredFacts[i]);
                System.out.println();
            }
        }

        double min, avg, max, std;
        System.out.println();
        System.out.println("Number of evaluation(s):\t" + numberOfTest);
        System.out.println("\n");
        for (int i = 0; i < evaluationTimes.length; i++) {
            min = Statistics.min(evaluationTimes[i]);
            avg = Statistics.average(evaluationTimes[i]);
            max = Statistics.max(evaluationTimes[i]);
            std = Statistics.standard_deviation(evaluationTimes[i]);

            System.out.println("Rule [" + (i + 1) + "\\" + rules.length + "]:\t" + rules[i]);
            System.out.println("Inferred fact(s):\t" + numberInferredFacts[i]);
            System.out.printf("Evaluation Time:\t%.3fs" + StringUtils.repeat(", %.3fs", evaluationTimes[i].length - 1) + "\n", evaluationTimes[i]);
            msg = String.format("Evaluation Time (min, avg, max, std):\t%.3fs, %.3fs, %.3fs, %.3fs\n", min, avg, max, std);
            System.out.println(msg);
            System.out.println(StringUtils.repeat("-", msg.length() + 1));
            System.out.println();
        }
        tt1 = System.currentTimeMillis();
        System.out.printf("Total Time:\t%.3f%s\n", (tt1 - tt0) / 1000.0, "s");
    }

    public static Set<Literal> runDReW(String dlpContent, String rules) {
        String[] args = DReWDefaultArgs.getDefaultArgs();
        args[2] = System.getenv("ONTOLOGY");
        DReWRLCLILiteral drew = DReWRLCLILiteral.get(args);


        drew.setDLPContent(dlpContent + "\n" + rules);
        drew.go();

        Set<Literal> answer = new HashSet<>();
        for (Set<Literal> lit : drew.getLiteralModelHandler().getAnswerSets()) {
            answer.addAll(lit);
        }

        return answer;
    }

    public static String getDLPContentFromFile(String filepath) throws IOException {
        List<String> lines = FileUtils.readLines(new File(filepath), DEFAULT_ENCODE);
        StringBuilder sb = new StringBuilder();

        String[] fields;
        for (String line : lines) {
            line = line.replaceAll("[\\\\<>.\"]", "");
            fields = line.split("\t");
            sb.append(fields[1]).append("(\"").append(fields[0]).append("\",\"").append(fields[2]).append("\").\n");
        }

        return sb.toString().trim();
    }

    public static String getRulesFromFile(String filepath) throws IOException {
        List<String> lines = FileUtils.readLines(new File(filepath), DEFAULT_ENCODE);
        StringBuilder sb = new StringBuilder();

        String[] fields;
        for (String line : lines) {
            fields = line.split("\t");
            sb.append(convertRuleToDataLog(fields[0])).append("\n");
        }

        return sb.toString().trim();
    }

    public static String convertRuleToDataLog(String rule) {
        String[] array = rule.replace(" ", "").split("\\?|<|>|=>");

        List<String> literals = new ArrayList<>();
        String literal;
        int i = 0;
        while (i < array.length) {
            if (array[i].isEmpty()) {
                i++;
                continue;
            }
            literal = array[i + 1] + "(" + array[i].toUpperCase() + "," + array[i + 3].toUpperCase() + ")";
            literals.add(literal);
            i += 4;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(literals.get(literals.size() - 1)).append(" :- ");
        for (i = 0; i < literals.size() - 2; i++) {
            sb.append(literals.get(i)).append(", ");
        }
        sb.append(literals.get(i)).append(".");

        return sb.toString();
    }

}

class Statistics {

    public static double average(Double array[]) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum / array.length;
    }

    private static double squared_difference_sum(Double array[]) {
        double m = average(array);

        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += Math.pow(array[i] - m, 2);
        }

        return sum;
    }

    public static double standard_deviation(Double array[]) {
        return Math.sqrt(squared_difference_sum(array) / array.length);
    }

    public static double min(Double array[]) {
        double min = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static double max(Double array[]) {
        double max = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

}
