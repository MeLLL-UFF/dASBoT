/*
 * UFF Project Semantic Learning
 */
package edu.uff.expansion.set;

import java.util.Objects;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.default_logic.OWLPredicate;
import org.semanticweb.drew.dlprogram.model.Predicate;

/**
 *
 * @author Victor
 */
@ComponentAnn(name = "SimplePredicate", shortName = "smppred", version = 0.1)
public class SimplePredicate implements DataLogPredicate, Component {

    protected String head;
    protected int arity;

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
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.head);
        hash = 17 * hash + this.arity;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SimplePredicate other = (SimplePredicate) obj;
        if (!Objects.equals(this.head, other.head))
            return false;
        if (this.arity != other.arity)
            return false;
        
        return true;
    }

    @Override
    public OWLPredicate asOWLPredicate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Predicate o) {
        if (equals(o))
            return 0;
        return -1;
    }

    @Override
    public void init() throws ComponentInitException {
    }

}