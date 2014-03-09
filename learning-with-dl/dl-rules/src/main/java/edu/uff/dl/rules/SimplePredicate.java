/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.semanticweb.drew.default_logic.OWLPredicate;
import org.semanticweb.drew.dlprogram.model.Predicate;

/**
 *
 * @author Victor
 */

public class SimplePredicate implements HeadPredicate {

    private String head;
    private int arity;

    public SimplePredicate() {
    }

    public SimplePredicate(String head, int arity) {
        this.head = head;
        this.arity = arity;
    }

    @Override
    public String getHead() {
        return head;
    }

    @Override
    public void setHead(String head) {
        this.head = head;
    }

    @Override
    public int getArity() {
        return arity;
    }

    @Override
    public void setArity(int arity) {
        this.arity = arity;
    }

    @Override
    public OWLPredicate asOWLPredicate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Predicate o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
