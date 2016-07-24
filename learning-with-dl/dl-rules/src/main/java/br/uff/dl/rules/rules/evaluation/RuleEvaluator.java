/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.evaluation;

import br.uff.dl.rules.drew.DReWRLCLILiteral;
import br.uff.dl.rules.exception.TimeoutException;
import br.uff.dl.rules.rules.Rule;
import br.uff.dl.rules.util.Box;
import it.unical.mat.wrapper.DLVInvocationException;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static br.uff.dl.rules.util.Time.getTime;

/**
 * Class used to evaluate a rule. This class runs the DReW's to get the covered
 * examples. The most important about this class is to be able to set up a
 * timeout for the DReW's execution.
 * <br><br>
 * To use this class to get a {@link EvaluatedRule}, first you should create a
 * instance of this class, then you can call {@link #evaluateRuleWithTimeout(edu.uff.dl.rules.rules.avaliation.RuleEvaluator, int)
 * } passing this instance. Once you have done the first evaluation, you can
 * call {@link #reEvaluateRule(edu.uff.dl.rules.rules.avaliation.RuleEvaluator, java.util.Set, java.util.Set)
 * } for get different measures when only sets of examples change.
 *
 * @author Victor Guimarães
 */
public class RuleEvaluator extends Thread implements RuleContainer {

    private Rule rule;

    private String[] args;
    private String dlpContent;
    private Set<Literal> positivesExamples;
    private Set<Literal> negativesExamples;

    private Set<Literal> positivesCoveredExamples;
    private Set<Literal> negativesCoveredExamples;
    
    private DReWRLCLILiteral drew;
    private int positives;
    private int positivesCovered;
    private int negatives;
    private int negativesCovered;

    private double duration;

    private EvaluatedRule evaluatedRule;
    private Set<Literal> lits;

    /**
     * Constructor with all needed parameters.
     *
     * @param rule the rule to be evaluated.
     * @param args the DReW's argumetns.
     * @param dlpContent the DLP's content.
     * @param positivesExamples the positive examples.
     * @param negativesExamples the negative examples.
     */
    public RuleEvaluator(Rule rule, String[] args, String dlpContent, Set<Literal> positivesExamples, Set<Literal> negativesExamples) {
        this.rule = rule;
        this.args = args;
        this.dlpContent = dlpContent;
        this.positivesExamples = positivesExamples;
        this.negativesExamples = negativesExamples;
    }

    @Override
    public void run() {
        String begin, end;
        Box<Long> b = new Box<>(null), e = new Box(null);
        begin = getTime(b);
        getTime();
        try {
            compareRule();
        } catch (IOException | ParseException ex) {
        }

        end = getTime(e);
        double dif = e.getContent() - b.getContent();
        dif /= 1000;
        this.duration = dif;
    }

    /**
     * Method that prepare the DReW's input and calls it.
     *
     * @throws FileNotFoundException in case a file not be found.
     * @throws IOException in case something goes wrong during read a file.
     * @throws ParseException in case a input does not agree with the language
     * rules.
     */
    protected void compareRule() throws FileNotFoundException, IOException, ParseException {
        String in = dlpContent + "\n" + rule.toString();
        compareRuleWithExamples(in);
        evaluatedRule = new EvaluatedRule(rule, positives, negatives, positivesCovered, negativesCovered, null);
    }

    /**
     * Method that runs the DReW's and compare its results.
     *
     * @param in the DReW's input program.
     * @throws FileNotFoundException in case a file not be found.
     * @throws ParseException in case a input does not agree with the language
     * rules.
     */
    protected void compareRuleWithExamples(String in) throws ParseException, FileNotFoundException {
        drew = DReWRLCLILiteral.get(args);
        drew.setDLPContent(in);
        drew.go();
        if (drew.getLiteralModelHandler().getAnswerSets().isEmpty())
            throw new ParseException();
        //lits = drew.getLiteralModelHandler().getAnswerSets().get(0);
        lits = new HashSet<>();
        for (Set<Literal> set : drew.getLiteralModelHandler().getAnswerSets()) {
            lits.addAll(set);
        }
        positives = positivesExamples.size();
        positivesCovered = compareRuleWithExample(lits, positivesExamples, positivesCoveredExamples);

        negatives = negativesExamples.size();
        negativesCovered = compareRuleWithExample(lits, negativesExamples, negativesCoveredExamples);
    }

    /**
     * Counts how many literals from the listExamples appears on the literals.
     * When the literals is the DReW's answer set, it means, counts how many
     * examples are covered.
     *
     * @param literals a set of literals (usually, DReW's output).
     * @param listExamples a set of examples.
     * @return the number of literals from listExamples appears on literals.
     */
    protected static int compareRuleWithExample(Set<Literal> literals, Set<Literal> listExamples) {
        int covered = 0;
        for (Literal s : listExamples) {
            if (literals.contains(s)) {
                covered++;
            }
        }
        return covered;
    }
    
    protected static int compareRuleWithExample(Set<Literal> literals, Set<Literal> listExamples, Set<Literal> coveredExamples) {
        int covered = 0;
        for (Literal s : listExamples) {
            if (literals.contains(s)) {
                coveredExamples.add(s);
                covered++;
            }
        }
        return covered;
    }

    /**
     * Getter for the {@link EvaluatedRule}.
     *
     * @return the {@link EvaluatedRule}.
     */
    public EvaluatedRule getEvaluatedRule() {
        return evaluatedRule;
    }

    /**
     * Getter for the duration. The duration is how long it takes for the rule
     * be inferred.
     *
     * @return the duration.
     */
    public double getDuration() {
        return duration;
    }

    @Override
    public void interrupt() {
        try {
            drew.killDLV();
        } catch (DLVInvocationException ex) {
        }

        super.interrupt();
    }

    /**
     * Method that should be called to get the {@link EvaluatedRule}. This
     * method will run the evaluation on another thread and wait untill its
     * finish or reach the timeout. If the timeout been reached, it will return
     * null, otherwise will return the {@link EvaluatedRule}.
     *
     * @param ruleEvaluater the {@link RuleEvaluator}.
     * @param timeout the timout, maximum time the rule has to be inferred.
     * @return If the timeout has been reached, it will return null, otherwise
     * will return the {@link EvaluatedRule}.
     * @throws TimeoutException if something goes wrong with the thread.
     */
    public static EvaluatedRule evaluateRuleWithTimeout(RuleEvaluator ruleEvaluater, int timeout) throws TimeoutException {
        try {
            ruleEvaluater.start();
            ruleEvaluater.join(timeout * 1000);

            if (ruleEvaluater.getState() == Thread.State.TERMINATED) {
                return ruleEvaluater.getEvaluatedRule();
            } else {
                ruleEvaluater.interrupt();
                throw new TimeoutException();
            }
        } catch (InterruptedException ex) {
        }
        return null;
    }

    /**
     * Once the rule has been evaluated, you can call this method to get new
     * measures by changing the sets of examples but keeping the DLP program.
     *
     * @param ruleEvaluater the {@link RuleEvaluator}.
     * @param positiveLiterals the new set of positive examples.
     * @param negativeLiterals the new set of negative examples.
     * @return the new {@link EvaluatedRule} for the new sets of examples.
     */
    public static EvaluatedRule reEvaluateRule(RuleEvaluator ruleEvaluater, Set<Literal> positiveLiterals, Set<Literal> negativeLiterals) {
        int positives = positiveLiterals.size();
        int positivesCovered = compareRuleWithExample(ruleEvaluater.lits, positiveLiterals);

        int negatives = negativeLiterals.size();
        int negativesCovered = compareRuleWithExample(ruleEvaluater.lits, negativeLiterals);

        return new EvaluatedRule(ruleEvaluater.rule, positives, negatives, positivesCovered, negativesCovered, null);
    }

    public Set<Literal> getPositivesCoveredExamples() {
        return positivesCoveredExamples;
    }

    public Set<Literal> getNegativesCoveredExamples() {
        return negativesCoveredExamples;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

}
