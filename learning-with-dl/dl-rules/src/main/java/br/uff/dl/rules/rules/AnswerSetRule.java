/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules;

import br.uff.dl.rules.datalog.DataLogRule;
import br.uff.dl.rules.expansionset.ExpansionAnswerSet;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Class used to link a set of rules with its corresponding Expansion Answer
 * Set.
 *
 * @author Victor Guimarães
 */
public class AnswerSetRule {

    private ExpansionAnswerSet expansionAnswerSet;
    private AnswerRule answerRule;

    /**
     * The constructor with all needed parameters.
     *
     * @param expansionAnswerSet the Expansion Answer Set.
     * @param answerRule the Answer Rule.
     */
    public AnswerSetRule(ExpansionAnswerSet expansionAnswerSet, AnswerRule answerRule) {
        this.expansionAnswerSet = expansionAnswerSet;
        this.answerRule = answerRule;
    }

    /**
     * Saves the rules on text format into a file. If the file does not exist,
     * creates it. If exist, set append as true to append this rules on the
     * bottom of the file or false to overwrite the file.
     *
     * @param filePath the output file path.
     * @param append the append.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     * @throws IOException in case something during the file creation goes
     * wrong.
     */
    public void setRulesToFile(String filePath, boolean append) throws FileNotFoundException, IOException {
        OutputStream output = new FileOutputStream(filePath, append);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        BufferedWriter bw = new BufferedWriter(writer);

        if (append)
            bw.write("\n");

        bw.write(getRulesAsString());

        bw.close();
    }

    /**
     * Getter for the rules as {@link String}. The rules on the format to be
     * used as a input on a logic program.
     *
     * @return the rules as {@link String}.
     */
    public String getRulesAsString() {
        StringBuilder sb = new StringBuilder();
        for (DataLogRule dataLogRule : answerRule.getRules()) {
            sb.append(dataLogRule.toString());
            sb.append("\n");
            //sb.append("Rule body: " + dataLogRule.getTerms().size() + " literals.");
        }

        return sb.toString().trim();
    }

    /**
     * Getter for the Expansion Aswer Set.
     *
     * @return the Expansion Aswer Set.
     */
    public ExpansionAnswerSet getExpansionAnswerSet() {
        return expansionAnswerSet;
    }

    /**
     * Getter for the {@link AnswerRule}.
     *
     * @return the {@link AnswerRule}
     */
    public AnswerRule getAnswerRule() {
        return answerRule;
    }

}
