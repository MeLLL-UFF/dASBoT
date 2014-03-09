/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules;

import org.semanticweb.drew.dlprogram.model.Predicate;

/**
 *
 * @author Victor
 */
public interface HeadPredicate extends Predicate {

    public String getHead();

    public void setHead(String head);
    
}
