/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.datalog;

import org.semanticweb.drew.dlprogram.model.Term;

import java.io.Serializable;
import java.util.List;

/**
 * An interface to describe a Concrete (instantiated literal) from the DataLog
 * language.
 *
 * @author Victor Guimarães
 */
public interface ConcreteLiteral extends DataLogPredicate, Serializable {

    /**
     * Getter to see if the literal has failed to be proved.
     * @return true if has failed, false otherwise.
     */
    public boolean hasFailed();

    /**
     * Setter to define if the literal has failed to be proved.
     * @param failed true if has failed, false otherwise.
     */
    public void setFailed(boolean failed);

    /**
     * Getter to see if the literal is negative.
     * @return true if it is, false otherwise.
     */
    public boolean isNegative();

    /**
     * Getter for the literal terms.
     * The term inside the literal's parentheses.
     * @return the literal's terms
     */
    public List<Term> getTerms();

    /**
     * Compare this literal against an other.
     * @param lit the other literal.
     * @return true if they are equal, false otherwise.
     */
    public boolean sameAs(final ConcreteLiteral lit);

}
