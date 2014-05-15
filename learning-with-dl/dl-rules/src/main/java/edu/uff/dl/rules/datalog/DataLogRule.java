/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.datalog;

import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Clause;

/**
 *
 * @author Victor
 */
public interface DataLogRule {
    public ConcreteLiteral getHead();
    public Set<? extends ConcreteLiteral> getTerms();
    
    public Clause asClause();
}
