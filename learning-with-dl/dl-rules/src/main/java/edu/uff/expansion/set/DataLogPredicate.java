/*
 * UFF Project Semantic Learning
 */

package edu.uff.expansion.set;

import org.semanticweb.drew.dlprogram.model.Predicate;

/**
 *
 * @author Victor
 */
public interface DataLogPredicate extends Predicate {

    public String getHead();

    public void setHead(String head);
    
}
