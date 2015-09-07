/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules;

import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.drew.DReWReasoner;

import static edu.uff.dl.rules.util.Time.getTime;

import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.FileContent;
import it.unical.mat.wrapper.DLVInvocationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class used to run the logic program and generate the rules.
 * <br> This class generates a rule and comparates it against the positive and
 * negative examples.
 *
 * @author Victor Guimarães
 */
public class DLExamplesRules extends Thread {
    //Input parametres
    protected String dlpContent;

    protected String dlpPositivesExamples;
    protected String dlpNegativesExamples;

    //Output parametres
    protected DReWReasoner reasoner;
    protected double duration;
    protected AnswerSetRule answerSetRule;
    protected EvaluatedRule evaluatedRule;

    //Optional parametres
    protected int offset;
    protected boolean compareRule;

    //Others
    protected DReWRLCLILiteral drew;

    //Private
    private int positives;
    private int negatives;

    private int positivesCovered;
    private int negativesCovered;

    /**
     * Constructor with all needed parameters.
     *
     * @param dlpContent the DLP's content.
     * @param reasoner a DReW's reasoner to obtain its output.
     * @param dlpPositivesExamples the posivite examples.
     * @param dlpNegativesExamples the negative examples.
     * @throws FileNotFoundException in case a file does not exists.
     */
    public DLExamplesRules(String dlpContent, DReWReasoner reasoner, String dlpPositivesExamples, String dlpNegativesExamples) throws FileNotFoundException {
        this.dlpContent = dlpContent;
        this.reasoner = reasoner;

        this.dlpPositivesExamples = dlpPositivesExamples;
        this.dlpNegativesExamples = dlpNegativesExamples;

        this.compareRule = true;
        this.offset = 0;
    }

    @Override
    public void run() {
        String begin, end;
        Box<Long> b = new Box<>(null), e = new Box(null);
        begin = getTime(b);
        getTime();
        
        try {
            runDLRulesReasoner(offset);
        } catch (ComponentInitException | IOException | ParseException ex) {
            //Logger.getLogger(DLExamplesRules.class.getName()).log(Level.SEVERE, null, ex);
        }

        end = getTime(e);
        System.out.println("");
        System.out.println("Begin: " + begin);
        System.out.println("End:   " + end);
        double dif = e.getContent() - b.getContent();
        dif /= 1000;
        System.out.println("Total time: " + dif + "s");
        this.duration = dif;
    }

    /**
     * Method which does all the process. Runs the DReW obtains the output,
     * generates the rule and copare it.
     *
     * @param offset the offset to specifi the example that will be used to base
     * the rule on.
     * @throws ComponentInitException in case something goes wrong for DReW.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     * @throws IOException in case something during the file creation goes
     * wrong.
     * @throws org.semanticweb.drew.dlprogram.parser.ParseException
     */
    public void runDLRulesReasoner(int offset) throws ComponentInitException, FileNotFoundException, IOException, ParseException {
        reasoner.setOffset(offset);
        reasoner.run();

        if (reasoner.getAnswerSetRules() == null || reasoner.getAnswerSetRules().size() < 1)
            return;

        answerSetRule = reasoner.getAnswerSetRules().get(reasoner.getAnswerSetRules().size() - 1);

        System.out.println(answerSetRule.getRulesAsString());
        System.out.println("");
        System.out.println("Rule's body: " + answerSetRule.getAnswerRule().getRules().iterator().next().getBody().size() + " literals.");
        System.out.println("");
        System.out.println("Comparar a Regra: " + getTime());
        System.out.println("");

        if (compareRule) {
            String in = dlpContent + "\n" + answerSetRule.getRulesAsString();
            compareRuleWithExamples(in);
            evaluatedRule = new EvaluatedRule(answerSetRule.getAnswerRule().getRule(), positives, negatives, positivesCovered, negativesCovered, null);
        }

    }

    /**
     * Compare the rule against a both the positive and negative examples.
     *
     * @param in the DLP's content.
     * @throws ParseException in case some file does not accord with the laguage
     * rules.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     */
    protected void compareRuleWithExamples(String in) throws ParseException, FileNotFoundException {
        drew = DReWRLCLILiteral.get(reasoner.getArg());
        drew.setDLPContent(in);
        drew.go();
        Set<Literal> lits = drew.getLiteralModelHandler().getAnswerSets().get(0);
        String head = "Verificando exemplos positivos";
        String bottom = "Exemplos Positivos Cobertos (train1.f) / Total";

        Set<Literal> listExamples = FileContent.getExamplesLiterals(dlpPositivesExamples);
        positivesCovered = compareRuleWithExample(lits, listExamples, head, bottom);
        positives = listExamples.size();

        head = "Verificando exemplos negativos";

        bottom = "Exemplos Negativos Cobertos (train1.n) / Total";
        listExamples = FileContent.getExamplesLiterals(dlpNegativesExamples);
        negativesCovered = compareRuleWithExample(lits, listExamples, head, bottom);
        negatives = listExamples.size();
    }

    /**
     * Compare the rule against a set of examples.
     * <br> This method have a human-like print output.
     *
     * @param literals the DReW's output (Answer Set).
     * @param listExamples the set of examples.
     * @param head a head label.
     * @param bottom a bottom label.
     * @return the number of examples proved by the rule.
     * @throws ParseException in case some file does not accord with the laguage
     * rules.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     */
    protected int compareRuleWithExample(Set<Literal> literals, Set<Literal> listExamples, String head, String bottom) throws ParseException, FileNotFoundException {
        System.out.println(head + ": " + getTime());
        int positive = 0;
        for (Literal s : listExamples) {
            if (literals.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }

        System.out.println(bottom + ": " + positive + " / " + listExamples.size());
        System.out.println("");

        return positive;
    }

    @Override
    public void interrupt() {
        try {
            drew.killDLV();
            reasoner.killDLV();
        } catch (DLVInvocationException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }

        super.interrupt();
    }

    /**
     * Getter for the duration. The process's duration.
     *
     * @return the duration.
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Getter for the {@link AnswerSetRule}.
     *
     * @return the {@link AnswerSetRule}.
     */
    public AnswerSetRule getAnwserSetRule() {
        return answerSetRule;
    }

    /**
     * Getter for the offset.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Setter for the offset.
     *
     * @param offset the offset.
     */
    public void setOffset(int offset) {
        this.offset = offset;
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
     * Getter for the examples.
     *
     * @return the examples.
     */
    public List<ConcreteLiteral> getExamples() {
        return reasoner.getExamplesForRule();
    }

}
