/*
 * UFF Project Semantic Learning
 */

package edu.uff.expansion.set;

import java.util.List;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public interface ConcreteDataLogLiteral extends DataLogPredicate {
    
    public boolean hasFailed();
    public boolean isNegative();
    public List<Term> getTerms();
    
}
