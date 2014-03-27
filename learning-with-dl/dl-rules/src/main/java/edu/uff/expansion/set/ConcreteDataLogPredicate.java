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
public interface ConcreteDataLogPredicate extends DataLogPredicate {
    
    public boolean hasFailed();
    public void setFailed(boolean failed);
    public boolean isNegative();
    public List<Term> getTerms();
    
    public boolean sameAs(final ConcreteDataLogPredicate lit);
    
}
