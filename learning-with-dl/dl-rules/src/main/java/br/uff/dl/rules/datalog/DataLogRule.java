/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.datalog;

import org.semanticweb.drew.dlprogram.model.Clause;

import java.util.Set;

/**
 * An interface to describe a rule from DataLog.
 *
 * @author Victor Guimarães
 */
public interface DataLogRule {

    /**
     * Getter for the rule's head.
     * @return the rule's head.
     */
    public ConcreteLiteral getHead();

    /**
     * Getter for the rule's body.
     * @return the rule's body.
     */
    public Set<? extends ConcreteLiteral> getBody();

    /**
     * Get the rule as a {@link Clause}.
     * @return the rule as a {@link Clause}.
     */
    public Clause asClause();
}
