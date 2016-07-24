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

/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.theory;

import br.uff.dl.rules.rules.Rule;
import br.uff.dl.rules.util.FileContent;
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Victor Guimarães
 */
public class Theory {

    protected Set<Rule> rules;

    Theory(Set<Rule> rules) {
        this.rules = rules;
    }

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
        return rulesToString(this.rules);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.rules);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Theory other = (Theory) obj;
        if (!Objects.equals(this.rules, other.rules)) {
            return false;
        }
        return true;
    }

    static String rulesToString(Set<? extends Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Rule rule : rules) {
            sb.append(rule.toString()).append("\n");
        }

        return sb.toString().trim();
    }

    public void toFile(File file, String encode) throws IOException {
        FileUtils.writeStringToFile(file, this.toString(), encode);
    }

    public static Theory mergeTheories(Collection<? extends Theory> theories) {
        Set<Rule> rules = new LinkedHashSet<>();
        for (Theory theory : theories) {
            rules.addAll(theory.getRules());
        }

        return new Theory(rules);
    }

}
