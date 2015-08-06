/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.evaluation;

import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.util.FileContent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class for keep a rule, its based example and its measure to a specific
 * problem. This class also can be used to serialize and deserialize rules.
 *
 * @author Victor Guimarães
 */
public class EvaluatedRuleExample extends EvaluatedRule {

    protected ConcreteLiteral example;

    /**
     * Constructor with all needed parameters.
     *
     * @param rule the rule.
     * @param positives the number of positive examples.
     * @param negatives the number of negative examples.
     * @param positivesCovered the number of covered positive examples.
     * @param negativesCovered the number of covered negative examples.
     * @param ruleMeasureFunction the rule measurer.
     * @param example the based example.
     */
    public EvaluatedRuleExample(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered, RuleMeasurer ruleMeasureFunction, ConcreteLiteral example) {
        super(rule, positives, negatives, positivesCovered, negativesCovered, ruleMeasureFunction);
        this.example = example;
    }

    /**
     * Contructor to create a {@link EvaluatedRuleExample} from a serialized
     * file. Careful, when a {@link EvaluatedRuleExample} is serialized the
     * {@link RuleMeasurer} is not serialized with it, if you deserialize a file
     * the {@link RuleMeasurer} will be null. You must provide a new
     * {@link RuleMeasurer} in case you want to get new measures.
     *
     * @param file the file.
     * @throws FileNotFoundException in case the file does not exists.
     */
    public EvaluatedRuleExample(File file) throws IOException {
        this.serializedFile = file;
        EvaluatedRuleExample e = deserialize(serializedFile);

        this.example = e.example;
        setEvaluatedRule(e);
    }

    /**
     * Constructor with all needed parameters. Used to create an
     * {@link EvaluatedRuleExample} from an {@link EvaluatedRule}.
     *
     * @param er the {@link EvaluatedRule}.
     * @param example the based example.
     */
    public EvaluatedRuleExample(final EvaluatedRule er, ConcreteLiteral example) {
        this.example = example;
        setEvaluatedRule(er);
    }

    /**
     * Constructor with all needed parameters. Used to create an
     * {@link EvaluatedRuleExample} from an {@link EvaluatedRule}.
     *
     * @param er the {@link EvaluatedRule}.
     * @param example the based example.
     * @param ruleMeasureFunction the rule measurer.
     */
    public EvaluatedRuleExample(final EvaluatedRule er, ConcreteLiteral example, RuleMeasurer ruleMeasureFunction) {
        this.example = example;
        setEvaluatedRule(er);
    }

    /**
     * Method to set the variables based on a {@link EvaluatedRule}.
     *
     * @param er the {@link EvaluatedRule}.
     */
    private void setEvaluatedRule(final EvaluatedRule er) {
        this.rule = er.rule;
        this.positives = er.positives;
        this.negatives = er.negatives;
        this.positivesCovered = er.positivesCovered;
        this.negativesCovered = er.negativesCovered;
        this.serializedFile = er.serializedFile;
    }

    /**
     * Getter for the based examples.
     *
     * @return the based examples.
     */
    public ConcreteLiteral getExample() {
        return example;
    }

    @Override
    public void serialize(File file) throws FileNotFoundException {
        OutputStream output = new FileOutputStream(file, false);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        BufferedWriter bw = new BufferedWriter(writer);
        String header = "";
        header += example.toString() + "\n";
        header += positives;
        header += " ";
        header += negatives;
        header += " ";
        header += positivesCovered;
        header += " ";
        header += negativesCovered;
        header += "\n";
        try {
            bw.write(header);
            bw.write(rule.toString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(EvaluatedRule.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.serializedFile = file;
    }

    @Override
    public EvaluatedRuleExample deserialize(File file) throws IOException {
        String serializedRule = "";
        int[] ints = new int[4];
        Scanner in = new Scanner(file);

        String literalLine = in.nextLine();
        ConcreteLiteral serializedExample;
        if (!literalLine.endsWith(".")) {
            literalLine += ".";
        }
        try {
            serializedExample = FileContent.getLiteralFromString(literalLine);
        } catch (ParseException ex) {
            throw new IOException("Malformed rule file!");
        }

        for (int i = 0; i < ints.length; i++) {
            ints[i] = in.nextInt();
        }

        while (in.hasNext()) {
            serializedRule += in.next();
        }
        Rule r = null;

        try {
            r = FileContent.getRuleFromString(serializedRule);
        } catch (ParseException ex) {
            //Logger.getLogger(EvaluatedRule.class.getName()).log(Level.SEVERE, null, ex);
        }

        EvaluatedRuleExample ere = new EvaluatedRuleExample(r, ints[0], ints[1], ints[2], ints[3], null, serializedExample);
        ere.serializedFile = file;
        return ere;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Example: ");
        sb.append(example.toString());
        sb.append("\n");
        sb.append("Positives: ");
        sb.append(positives);
        sb.append(" Negatives: ");
        sb.append(negatives);
        sb.append(" Positives Covered: ");
        sb.append(positivesCovered);
        sb.append(" Negatives Covered: ");
        sb.append(negativesCovered);
        sb.append("\n");
        sb.append(rule.toString());

        return sb.toString().trim();
    }

}
