/*
 * UFF Project Semantic Learning
 */

package edu.uff.expansion.set;

import java.util.Collection;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Clause;

/**
 *
 * @author Victor
 */
public interface DataLogRule {
    public ConcreteDataLogPredicate getHorn();
    public Collection<? extends ConcreteDataLogPredicate> getTerms();
    
    public Clause asClause();
}
