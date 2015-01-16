/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.evaluation;

import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.Serializable;
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
 * Class to keep a rule and its measure to a specific problem. This class also
 * can be used to serialize and deserialize rules.
 *
 * @author Victor Guimarães
 */
public class EvaluatedRule implements Serializable<EvaluatedRule> {

    protected Rule rule;
    protected int positives;
    protected int negatives;

    protected int positivesCovered;
    protected int negativesCovered;

    protected RuleMeasurer ruleMeasureFunction;
    protected File serializedFile;

    /**
     * Constructor without parameters, used by classes which extends this one.
     */
    protected EvaluatedRule() {

    }

    /**
     * Contructor to create a {@link EvaluatedRule} from a serialized file.
     * Careful, when a {@link EvaluatedRule} is serialized the
     * {@link RuleMeasurer} is not serialized with it, if you deserialize a file
     * the {@link RuleMeasurer} will be null. You must provide a new
     * {@link RuleMeasurer} in case you want to get new measures.
     *
     * @param file the file.
     * @throws FileNotFoundException in case the file does not exists.
     */
    public EvaluatedRule(File file) throws FileNotFoundException {
        this.serializedFile = file;
        EvaluatedRule e = deserialize(serializedFile);

        this.rule = e.rule;
        this.positives = e.positives;
        this.negatives = e.negatives;
        this.positivesCovered = e.positivesCovered;
        this.negativesCovered = e.negativesCovered;
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param rule the rule.
     * @param positives the number of positive examples.
     * @param negatives the number of negative examples.
     * @param positivesCovered the number of covered positive examples.
     * @param negativesCovered the number of covered negative examples.
     * @param ruleMeasureFunction the rule measurer.
     */
    public EvaluatedRule(Rule rule, int positives, int negatives, int positivesCovered, int negativesCovered, RuleMeasurer ruleMeasureFunction) {
        this.rule = rule;
        this.positives = positives;
        this.negatives = negatives;
        this.positivesCovered = positivesCovered;
        this.negativesCovered = negativesCovered;
        this.ruleMeasureFunction = ruleMeasureFunction;
    }

    /**
     * Getter for the rule.
     *
     * @return the rule.
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Getter for the number of positive examples.
     *
     * @return the number of positive examples.
     */
    public int getPositives() {
        return positives;
    }

    /**
     * Getter for the number of negative examples.
     *
     * @return the number of negative examples.
     */
    public int getNegatives() {
        return negatives;
    }

    /**
     * Getter for the number of covered positive examples.
     *
     * @return the number of covered positive examples.
     */
    public int getPositivesCovered() {
        return positivesCovered;
    }

    /**
     * Getter for the number of covered negative examples.
     *
     * @return the number of covered negative examples.
     */
    public int getNegativesCovered() {
        return negativesCovered;
    }

    /**
     * Getter for the rule measure function.
     *
     * @return the rule measure function.
     */
    public RuleMeasurer getRuleMeasureFunction() {
        return ruleMeasureFunction;
    }

    /**
     * Getter for the rule's measure.
     *
     * @return the rule's measure.
     */
    public double getMeasure() {
        if (ruleMeasureFunction == null)
            return 0;
        return ruleMeasureFunction.getRuleMeasure(rule, positives, negatives, positivesCovered, negativesCovered);
    }

    /**
     * Setter for the rule measure function.
     *
     * @param ruleMeasureFunction the rule measure function.
     */
    public void setRuleMeasureFunction(RuleMeasurer ruleMeasureFunction) {
        this.ruleMeasureFunction = ruleMeasureFunction;
    }

    /**
     * Getter for the serialized file. This File is where the rule has been
     * deserialized from. In case the rule has not been deserialized, this
     * method will return null.
     *
     * @return the serialized file.
     */
    public File getSerializedFile() {
        return serializedFile;
    }

    @Override
    public void serialize(File file) throws FileNotFoundException {
        OutputStream output = new FileOutputStream(file, false);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        BufferedWriter bw = new BufferedWriter(writer);
        String header = "";
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

    }

    @Override
    public EvaluatedRule deserialize(File file) throws FileNotFoundException {
        String fileContent = FileContent.getStringFromFile(file);
        EvaluatedRule er = deserialize(fileContent);
        er.serializedFile = file;
        return er;
    }

    /**
     * Method for deserialize the rule from the file's content.
     *
     * @param fileContent the file's content.
     * @return the deserialized rule.
     * @throws FileNotFoundException in case the file does not exists.
     */
    private EvaluatedRule deserialize(String fileContent) throws FileNotFoundException {
        String serializedRule = "";
        int[] ints = new int[4];
        Scanner in = new Scanner(fileContent);

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

        return new EvaluatedRule(r, ints[0], ints[1], ints[2], ints[3], null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

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
