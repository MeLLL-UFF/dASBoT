/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.datalog;

import java.util.Collection;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Clause;

/**
 *
 * @author Victor
 */
public interface DataLogRule {
    public ConcreteLiteral getHorn();
    public Collection<? extends ConcreteLiteral> getTerms();
    
    public Clause asClause();
}
