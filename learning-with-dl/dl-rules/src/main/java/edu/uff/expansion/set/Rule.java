/*
 * UFF Project Semantic Learning
 */
package edu.uff.expansion.set;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
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
 *
 * @author Victor
 */
public class Rule implements DataLogRule, Component {

    protected ConcreteDataLogPredicate horn;
    protected Collection<? extends ConcreteDataLogPredicate> terms;

    public Rule() {
    }

    public Rule(ConcreteDataLogPredicate horn, Collection<? extends ConcreteDataLogPredicate> terms) {
        this.horn = horn;
        this.terms = terms;
    }

    public void setHorn(ConcreteDataLogPredicate horn) {
        this.horn = horn;
    }

    public void setTerms(Set<ConcreteDataLogPredicate> terms) {
        this.terms = terms;
    }

    @Override
    public ConcreteDataLogPredicate getHorn() {
        return horn;
    }

    @Override
    public Collection<? extends ConcreteDataLogPredicate> getTerms() {
        return terms;
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
        sb.append(getHorn().toString());
        sb.append(" :- ");
        for (ConcreteDataLogPredicate con : terms) {
            sb.append(con);
            sb.append(", ");
        }

        sb.setCharAt(sb.lastIndexOf(","), '.');

        return sb.toString().trim();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.horn);
        hash = 97 * hash + Objects.hashCode(this.terms);
        return hash;
    }

    @Override
    public boolean equals(Object obj
    ) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Rule other = (Rule) obj;
        if (!Objects.equals(this.horn, other.horn))
            return false;
        if (!Objects.equals(this.terms, other.terms))
            return false;
        return true;
    }

    @Override
    public void init() throws ComponentInitException {
    }

}
