/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.avaliation;

import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.util.Box;
import static edu.uff.dl.rules.util.Time.getTime;
import edu.uff.dl.rules.util.TimeoutException;
import it.unical.mat.wrapper.DLVInvocationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

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
public class RuleEvaluator extends Thread {

    private Rule rule;

    private String[] args;
    private String dlpContent;
    private Set<Literal> positivesExamples;
    private Set<Literal> negativesExamples;

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
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }

        end = getTime(e);
        //System.out.println("");
        //System.out.println("Begin: " + begin);
        //System.out.println("End:   " + end);
        double dif = e.getContent() - b.getContent();
        dif /= 1000;
        //System.out.println("Total time: " + dif + "s");
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
        positivesCovered = compareRuleWithExample(lits, positivesExamples);

        negatives = negativesExamples.size();
        negativesCovered = compareRuleWithExample(lits, negativesExamples);
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
        int positive = 0;
        for (Literal s : listExamples) {
            if (literals.contains(s)) {
                //System.out.println(s);
                positive++;
            }
        }
        return positive;
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
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
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
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
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

}