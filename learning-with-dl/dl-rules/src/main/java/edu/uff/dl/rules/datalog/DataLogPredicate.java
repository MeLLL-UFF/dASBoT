/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.datalog;

import org.semanticweb.drew.dlprogram.model.Predicate;

/**
 *
 * @author Victor
 */
public interface DataLogPredicate extends Predicate {

    public String getHead();

    public void setHead(String head);
    
}
