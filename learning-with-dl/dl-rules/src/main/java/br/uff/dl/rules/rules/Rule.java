/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.datalog.DataLogRule;
import br.uff.dl.rules.exception.VariableGenerator;
import br.uff.dl.rules.util.AlphabetCounter;
import br.uff.dl.rules.util.PredicateComparator;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to represent a logic rule.
 *
 * @author Victor Guimarães
 */
public class Rule implements DataLogRule, Component {

    protected ConcreteLiteral head;
    protected Set<? extends ConcreteLiteral> body;

    /**
     * Constructor without parameters. Needed to load this class from a file
     * (Spring).
     */
    protected Rule() {
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param head the rule's horn (head).
     * @param body the rule's terms (body).
     */
    public Rule(ConcreteLiteral head, Set<? extends ConcreteLiteral> body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public ConcreteLiteral getHead() {
        return head;
    }

    @Override
    public Set<? extends ConcreteLiteral> getBody() {
        return body;
    }

    @Override
    public Clause asClause() {
        Clause c = null;
        Reader reader;
        DLProgramParser parser;
        DLProgram elprogram = null;
        DLProgramKB kb = new DLProgramKB();
        try {
            reader = new StringReader(this.toString());
            parser = new DLProgramParser(reader);

            elprogram = parser.program();
            kb.setProgram(elprogram);
            List<ProgramStatement> l = elprogram.getStatements();
            if (l.size() == 1 && l.get(0).isClause()) {
                c = l.get(0).asClause();
            }

        } catch (ParseException ex) {
            Logger.getLogger(Rule.class.getName()).log(Level.SEVERE, null, ex);
        }

        return c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHead().toString());
        sb.append(" :- ");
        for (ConcreteLiteral con : body) {
            sb.append(con);
            sb.append(", ");
        }

        sb.setCharAt(sb.lastIndexOf(","), '.');

        return sb.toString().trim();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.head);
        hash = 97 * hash + Objects.hashCode(this.body);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!getClass().isAssignableFrom(obj.getClass()))
            return false;
        final Rule other = (Rule) obj;

        return isEquivalent(other);
//        if (!Objects.equals(this.head, other.head) || this.head.hasFailed() != other.head.hasFailed())
//            return false;
//        if (this.body.size() != other.body.size())
//            return false;
//        List<? extends ConcreteLiteral> otherTerms = new LinkedList<>(other.getBody());
//        ConcreteLiteral otherTerm;
//        int index;
//        for (ConcreteLiteral term : body) {
//            index = otherTerms.indexOf(term);
//            if (index < 0)
//                return false;
//            otherTerm = otherTerms.remove(index);
//            if (otherTerm.hasFailed() != term.hasFailed())
//                return false;
//        }
//        return true;
    }

    public boolean isEquivalentToAny(Collection<Rule> others) {
        if (others.isEmpty()) {
            return false;
        }
        
        for (Rule other : others) {
            if (isEquivalent(other)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isEquivalent(Rule other) {
        if (this.toString().equals(other.toString())) {
            return true;
        }
        
        if (!head.getPredicate().equals(other.getHead().getPredicate()) || head.getArity() != other.getHead().getArity()) {
            return false;
        }

        if (other.getBody().size() != body.size()) {
            return false;
        }

        List<ConcreteLiteral> myBody = new ArrayList<>(body.size());
        List<ConcreteLiteral> otherBody = new ArrayList<>(other.getBody().size());
        for (ConcreteLiteral concreteLiteral : body) {
            myBody.add(concreteLiteral);
        }

        for (ConcreteLiteral concreteLiteral : other.getBody()) {
            otherBody.add(concreteLiteral);
        }

        Comparator com = new PredicateComparator();

        Collections.sort(myBody, com);
        Collections.sort(otherBody, com);

        StringBuilder rule1 = new StringBuilder();
        StringBuilder rule2 = new StringBuilder();
        VariableGenerator gen1 = new AlphabetCounter();
        VariableGenerator gen2 = new AlphabetCounter();
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();

        appendLiteral(this.head, rule1, map1, gen1);
        appendLiteral(other.getHead(), rule2, map2, gen2);

        rule1.append(" :- ");
        rule2.append(" :- ");

        for (int i = 0; i < myBody.size(); i++) {
            if (!myBody.get(i).getPredicate().equals(otherBody.get(i).getPredicate()) || myBody.get(i).getArity() != otherBody.get(i).getArity()) {
                return false;
            }
            appendLiteral(myBody.get(i), rule1, map1, gen1);
            rule1.append(", ");
            appendLiteral(otherBody.get(i), rule2, map2, gen2);
            rule2.append(", ");
        }
        rule1.delete(rule1.length() - 2, rule1.length());
        rule2.delete(rule2.length() - 2, rule2.length());
        rule1.append(".");
        rule2.append(".");
        
        return rule1.toString().equals(rule2.toString());
    }

    private static void appendLiteral(ConcreteLiteral literal, StringBuilder sb, Map<String, String> map, VariableGenerator gen) {
        sb.append(literal.getPredicate());
        sb.append("(");
        for (int i = 0; i < literal.getTerms().size(); i++) {
            String term = literal.getTerms().get(i).getName();
            if (term.toLowerCase().charAt(0) == term.charAt(0)) {
                sb.append(term);
            } else {
                if (!map.containsKey(term)) {
                    map.put(term, gen.getNextName());
                }
                sb.append(map.get(term));
            }
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(")");
    }

    @Override
    public void init() throws ComponentInitException {
    }

}
