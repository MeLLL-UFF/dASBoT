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

package br.uff.dl.rules.rules.theory;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.datalog.DataLogLiteral;
import br.uff.dl.rules.drew.DReWRLCLILiteral;
import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.rules.Rule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRule;
import br.uff.dl.rules.rules.evaluation.MeasurableRule;
import br.uff.dl.rules.util.FileContent;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created on 16/07/16.
 *
 * @author Victor Guimarães
 */
public abstract class TheoryBuilder implements Runnable {

    protected String dlpContent;

    protected Set<? extends Literal> positiveTrainExamples;
    protected Set<? extends Literal> negativeTrainExamples;

    protected Set<? extends Literal> positiveTestExamples;
    protected Set<? extends Literal> negativeTestExamples;

    protected double threshold = 0.0;
    protected RuleMeasurer measurer;
    protected String[] args;

    protected int maxSideWayMovements = -1;

    protected List<MeasurableRule> evaluatedRuleExamples;

    protected StringBuilder description;

    protected Theory theory;

    protected boolean ruleIndividuallyEvaluated = false;
    protected Set<Literal> theoryProvedLiterals;

    public TheoryBuilder(String dlpContent,
                         String positiveTrainFilePath,
                         String negativeTrainFilePath,
                         RuleMeasurer measurer,
                         String[] args) throws FileNotFoundException, ParseException {
        this.dlpContent = dlpContent;
        this.positiveTrainExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile
                (positiveTrainFilePath));
        this.negativeTrainExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile
                (negativeTrainFilePath));
        this.measurer = measurer;
        this.args = args;
        this.description = new StringBuilder();
    }

    public TheoryBuilder(String dlpContent,
                         Set<? extends Literal> positiveTrainExamples,
                         Set<? extends Literal> negativeTrainExamples, RuleMeasurer measurer, String[] args) {
        this.dlpContent = dlpContent;
        this.positiveTrainExamples = positiveTrainExamples;
        this.negativeTrainExamples = negativeTrainExamples;
        this.measurer = measurer;
        this.args = args;
        this.description = new StringBuilder();
    }

    protected Set<Literal> runReasoning(String dlpContent,
                                        Set<Literal> append) throws ParseException, FileNotFoundException {
        DReWRLCLILiteral drew = DReWRLCLILiteral.get(args);
        drew.setDLPContent(dlpContent);
        drew.go();

        Set<Literal> literals = append == null ? new HashSet<>() : append;

        for (Set<Literal> set : drew.getLiteralModelHandler().getAnswerSets()) {
            literals.addAll(set);
        }

        return literals;
    }

    protected EvaluatedRule evaluateAnswerSet(Set<Literal> literals,
                                              Set<? extends Literal> positiveExamples,
                                              Set<? extends Literal> negativeExamples) {

        int positives = positiveExamples.size();
        Set<Literal> covered = compareAnswerSetWithExample(literals, positiveExamples);
        int positivesCovered = covered.size();

        int negatives = negativeExamples.size();
        int negativesCovered = compareAnswerSetWithExample(literals, negativeExamples).size();

        return new EvaluatedRule(null, positives, negatives, positivesCovered, negativesCovered, measurer);
    }

    @Override
    public void run() {
        try {
            init();
            loadRules();
            buildTheory();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    protected abstract void init();

    protected abstract void loadRules() throws IOException;

    public Theory buildTheory() throws FileNotFoundException, ParseException {
        Set<Rule> theorySet = new LinkedHashSet<>();

        int count = 0;
        double measure, newMeasure;

        if (positiveTrainExamples == null) {
            positiveTrainExamples= new HashSet<>();
        }
        if (negativeTrainExamples == null) {
            negativeTrainExamples = new HashSet<>();
        }

        measure = measurer.getRuleMeasure(null, positiveTrainExamples.size(), negativeTrainExamples.size(), 0, 0);

        double initialMeasure = measure;
        MeasurableRule candidateRule;
        EvaluatedRule evaluatedMeasure = new EvaluatedRule(null, 0, 0, positiveTrainExamples.size(), negativeTrainExamples.size(), measurer);
        int sideMovements = 0;
        List<MeasurableRule> resultRules = new ArrayList<>();
        Set<Literal> currentLiterals = null;
        theoryProvedLiterals = new HashSet<>();

        while (!evaluatedRuleExamples.isEmpty() && count < evaluatedRuleExamples.size()) {
            candidateRule = evaluatedRuleExamples.get(count);
            count++;
            if (containsEquivalentRule(resultRules, candidateRule.getRule())) {
                continue;
            }

            candidateRuleToTheory(candidateRule);

            if (ruleIndividuallyEvaluated) {
                currentLiterals = runReasoning(dlpContent + "\n" + candidateRule.getRule(), null);
                currentLiterals.addAll(theoryProvedLiterals);
            } else {
                currentLiterals = runReasoning(dlpContent + "\n" + Theory.rulesToString(theorySet) + "\n" +
                                                       candidateRule.getRule(), null);
            }

            evaluatedMeasure = evaluateAnswerSet(currentLiterals, positiveTrainExamples, negativeTrainExamples);
            newMeasure = evaluatedMeasure.getMeasure();
//            newMeasure = runReasoning(dlpContent + "\n" + Theory.rulesToString(theorySet) + "\n" +
//                                              candidateRule.getRule(), theoryProvedLiterals,
//                                      positiveTrainExamples, negativeTrainExamples).getMeasure();

            if (newMeasure - measure < threshold) {
                sideMovements++;
                if (maxSideWayMovements > -1 && sideMovements > maxSideWayMovements) {
                    break;
                }
            } else {
                theoryProvedLiterals = currentLiterals;
                measure = newMeasure;
                sideMovements = 0;
                resultRules.add(candidateRule);
                theorySet.add(candidateRule.getRule());
                ruleAccepted(currentLiterals);
            }
        }

        this.theory = new Theory(theorySet);

        if (theoryProvedLiterals != currentLiterals) {
            evaluatedMeasure = evaluateAnswerSet(theoryProvedLiterals, positiveTrainExamples, negativeTrainExamples);
        }

        description.append("\n\n");
        description.append(theory.toString()).append("\n");
        description.append("\nTrain Measure:\t").append(evaluatedMeasure.getMeasure()).append("\n");
        description.append("Positives:\t").append(evaluatedMeasure.getPositives()).append("\n");
        description.append("Negatives:\t").append(evaluatedMeasure.getNegatives()).append("\n");
        description.append("Covered positives:\t").append(evaluatedMeasure.getPositivesCovered()).append("\n");
        description.append("Covered negatives:\t").append(evaluatedMeasure.getNegativesCovered()).append("\n");

        if (evaluateTestExamples()) {
            evaluatedMeasure = evaluateAnswerSet(theoryProvedLiterals, positiveTestExamples, negativeTestExamples);
            description.append("\nTest Measure:\t").append(evaluatedMeasure.getMeasure()).append("\n");
            description.append("Positives:\t").append(evaluatedMeasure.getPositives()).append("\n");
            description.append("Negatives:\t").append(evaluatedMeasure.getNegatives()).append("\n");
            description.append("Covered positives:\t").append(evaluatedMeasure.getPositivesCovered()).append("\n");
            description.append("Covered negatives:\t").append(evaluatedMeasure.getNegativesCovered()).append("\n");
        }

        description.append("\n");
        description.append("Measure Class:\t").append(measurer.getClass().getName()).append("\n");
        description.append("Initial Measure:\t").append(initialMeasure).append("\n");
        description.append("Threshold:\t").append(threshold).append("\n");

        return theory;
    }

    protected boolean evaluateTestExamples() {
        return positiveTestExamples != null && negativeTestExamples != null
                && !positiveTestExamples.isEmpty() && !negativeTestExamples.isEmpty();
    }

    protected abstract void candidateRuleToTheory(MeasurableRule candidate);

    protected abstract void ruleAccepted(Set<Literal> theoryProvedLiterals);

    public Set<? extends Literal> getPositiveTestExamples() {
        return positiveTestExamples;
    }

    public void setPositiveTestExamples(Set<? extends Literal> positiveTestExamples) {
        this.positiveTestExamples = positiveTestExamples;
    }

    public Set<? extends Literal> getNegativeTestExamples() {
        return negativeTestExamples;
    }

    public void setNegativeTestExamples(Set<? extends Literal> negativeTestExamples) {
        this.negativeTestExamples = negativeTestExamples;
    }

    public boolean isRuleIndividuallyEvaluated() {
        return ruleIndividuallyEvaluated;
    }

    public void setRuleIndividuallyEvaluated(boolean ruleIndividuallyEvaluated) {
        this.ruleIndividuallyEvaluated = ruleIndividuallyEvaluated;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getMaxSideWayMovements() {
        return maxSideWayMovements;
    }

    public void setMaxSideWayMovements(int maxSideWayMovements) {
        this.maxSideWayMovements = maxSideWayMovements;
    }

    public Set<Literal> getTheoryProvedLiterals() {
        return theoryProvedLiterals;
    }

    public Theory getTheory() {
        return theory;
    }

    @Override
    public String toString() {
        return description.toString().trim();
    }

    public static Set<Literal> compareAnswerSetWithExample(Set<Literal> answerSet, Set<? extends Literal> listExamples) {
        Set<Literal> covered = new HashSet<>();
        for (Literal s : listExamples) {
            if (answerSet.contains(s)) {
                covered.add(s);
            }
        }
        return covered;
    }

    public static boolean containsEquivalentRule(List<MeasurableRule> rules, Rule rule) {
        for (MeasurableRule r : rules) {
            if (rule.isEquivalent(r.getRule())) {
                return true;
            }
        }

        return false;
    }

    public static ConcreteLiteral literalToConcreteLiteral(Literal literal) {
        String head = literal.getPredicate().toString();
        head = head.substring(0, Math.min(head.indexOf("/"), head.length()));
        return new DataLogLiteral(head, literal.getTerms(), literal.isNegative());
    }

    public static Set<ConcreteLiteral> literalsToConcreteLiterals(Collection<? extends Literal> literals) {
        Set<ConcreteLiteral> concreteLiterals = new HashSet<>();

        if (literals != null) {
            for (Literal lit : literals) {
                concreteLiterals.add(literalToConcreteLiteral(lit));
            }
        }

        return concreteLiterals;
    }

}
