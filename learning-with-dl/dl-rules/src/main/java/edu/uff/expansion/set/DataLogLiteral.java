/*
 * UFF Project Semantic Learning
 */
package edu.uff.expansion.set;

import java.util.List;
import java.util.Objects;
import org.dllearner.core.ComponentAnn;
import org.semanticweb.drew.dlprogram.model.Predicate;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
@ComponentAnn(name = "DataLogLiteral", shortName = "datlogliteral", version = 0.1)
public class DataLogLiteral extends SimplePredicate implements ConcreteDataLogLiteral {

    protected boolean failed;
    protected boolean negative;
    protected List<Term> terms;

    public DataLogLiteral(String head, List<Term> terms) {
        super(head, terms.size());
        this.terms = terms;
    }
    
    public boolean hasFailed() {
        return failed;
    }

    public void setFailed(boolean falled) {
        this.failed = falled;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public List<Term> getTerms() {
        return terms;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.failed ? 1 : 0);
        hash = 23 * hash + (this.negative ? 1 : 0);
        hash = 23 * hash + Objects.hashCode(this.head);
        hash = 23 * hash + this.arity;
        hash = 23 * hash + Objects.hashCode(this.terms);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DataLogLiteral other = (DataLogLiteral) obj;
        if (this.failed != other.failed)
            return false;
        if (this.negative != other.negative)
            return false;
        if (!Objects.equals(this.head, other.head))
            return false;
        if (this.arity != other.arity)
            return false;
        if (!Objects.equals(this.terms, other.terms))
            return false;
        return true;
    }

    public boolean equalsButFailed(final DataLogLiteral lit) {
        if (this.negative != lit.negative)
            return false;
        if (!Objects.equals(this.head, lit.head))
            return false;
        if (this.arity != lit.arity)
            return false;

        return true;
    }

    @Override
    public int compareTo(Predicate o) {
        int ret = 0;
        if (o == null)
            return -1;
        if (getClass() == o.getClass()) {
            final DataLogLiteral other = (DataLogLiteral) o;
            if (this.failed != other.failed)
                ret++;
            if (this.negative != other.negative)
                ret++;
            if (!Objects.equals(this.head, other.head))
                ret++;
            if (this.arity != other.arity)
                ret++;
        }
        if (o instanceof DataLogPredicate) {
            final DataLogPredicate other = (DataLogPredicate) o;
            ret = 5;
            if (!Objects.equals(this.head, other.getHead()))
                ret++;
            if (this.arity != other.getArity())
                ret++;
        } else {
            return 10;
        }

        return ret;
    }

}
