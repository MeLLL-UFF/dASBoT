/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.datalog;

import org.semanticweb.drew.dlprogram.model.Predicate;

/**
 * An interface to describe a non-instantiated DataLog predicate.
 *
 * @author Victor Guimarães
 */
public interface DataLogPredicate extends Predicate {

    /**
     * Getter for the predicate's head.
     * @return the predicate's head.
     */
    public String getPredicate();

    /**
     * Setter for the predicate's head.
     * @param head the predicate's head.
     */
    public void setHead(String head);

}
