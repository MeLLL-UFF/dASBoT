/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.datalog;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.default_logic.OWLPredicate;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;
import org.semanticweb.drew.dlprogram.model.Predicate;

import java.util.Objects;

/**
 * A DataLog simple pradicate.
 * <br>A predicate that is not instanciaded yet.
 * <br>Have just a head and an arity.
 *
 * @author Victor Guimarães
 */
@ComponentAnn(name = "SimplePredicate", shortName = "smppred", version = 0.1)
public class SimplePredicate implements DataLogPredicate, Component {

    protected String head;
    protected int arity;

    /**
     * A empty construtor, needed to load the class from a file by Spring.
     */
    public SimplePredicate() {
    }

    /**
     * The construtor with all needed parameters
     * @param head the predicate's head
     * @param arity the predicate's arity
     */
    public SimplePredicate(String head, int arity) {
        this.head = head;
        this.arity = arity;
    }

    /**
     * The construtor to create a predicate by a {@link NormalPredicate}
     * @param normalPredicate the {@link NormalPredicate} needed.
     */
    public SimplePredicate(NormalPredicate normalPredicate) {
        this.head = normalPredicate.getName();
        this.arity = normalPredicate.getArity();
    }

    @Override
    public String getPredicate() {
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

    @Override
    public String toString() {
        return head + "/" + arity;
    }

}
