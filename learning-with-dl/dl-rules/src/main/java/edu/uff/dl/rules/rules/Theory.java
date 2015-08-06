/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules;

import edu.uff.dl.rules.util.FileContent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 *
 * @author Victor Guimarães
 */
public class Theory {

    protected Set<Rule> rules;

    public Theory() {
        rules = new LinkedHashSet<>();
    }

    public Theory(File file) throws IOException {
        this();
        List<String> lines = FileUtils.readLines(file);
        
        int i;
        for (i = 0; i < lines.size(); i++) {
            if (lines.get(i).isEmpty()) {
                break;
            }
        }

        for (; i < lines.size(); i++) {
            if (lines.get(i).isEmpty()) {
                if (rules.isEmpty()) {
                    continue;
                } else {
                    break;
                }
            }

            try {
                rules.add(FileContent.getRuleFromString(lines.get(i)));
            } catch (ParseException ex) {

            }
        }
    }

    public Theory(String filepath) throws IOException {
        this(new File(filepath));
    }
    
    public Set<Rule> getRules() {
        return rules;
    }
    
    @Override
    public String toString() {
        if (rules.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Rule rule : rules) {
            sb.append(rule.toString()).append("\n");
        }

        return sb.toString().trim();
    }

}
