/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.rules;

import edu.uff.dl.rules.datalog.DataLogRule;
import edu.uff.dl.rules.expansion.set.ExpansionAnswerSet;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author Victor
 */
public class AnswerSetRule {
    private ExpansionAnswerSet expansionAnswerSet;
    private AnswerRule answerRule;

    public AnswerSetRule(ExpansionAnswerSet expansionAnswerSet, AnswerRule answerRule) {
        this.expansionAnswerSet = expansionAnswerSet;
        this.answerRule = answerRule;
    }

    public void setRulesToFile(String filePath, boolean append) throws FileNotFoundException, IOException {
        OutputStream output = new FileOutputStream(filePath, append);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        BufferedWriter bw = new BufferedWriter(writer);
        
        if (append) bw.write("\n");
        
        bw.write(getRulesAsString());
        
        bw.close();
    }

    public String getRulesAsString() {
        StringBuilder sb = new StringBuilder();
        for (DataLogRule dataLogRule : answerRule.getRules()) {
            sb.append(dataLogRule.toString());
            sb.append("\n");
        }
        
        return sb.toString().trim();
    }
    
    public ExpansionAnswerSet getExpansionAnswerSet() {
        return expansionAnswerSet;
    }

    public AnswerRule getAnswerRule() {
        return answerRule;
    }
    
}
