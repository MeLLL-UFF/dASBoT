/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.datalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dllearner.core.ComponentAnn;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Predicate;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
@ComponentAnn(name = "DataLogLiteral", shortName = "datlogliteral", version = 0.1)
public class DataLogLiteral extends SimplePredicate implements ConcreteLiteral, Cloneable {

    protected boolean failed;
    protected boolean negative;
    protected List<Term> terms;

    public DataLogLiteral(String head, List<Term> terms) {
        super(head, terms.size());
        this.terms = terms;
    }

    public DataLogLiteral(String head, List<Term> terms, boolean negative) {
        super(head, terms.size());
        this.terms = terms;
        this.negative = negative;
    }

    public static ConcreteLiteral getInstanceFromLiteral(Literal lit) {
        String head = lit.getPredicate().toString();
        int index = head.indexOf("/");
        if (index > 0) {
            head = head.substring(0, index);
        }

        return new DataLogLiteral(head, lit.getTerms(), lit.isNegative());
    }

    public static List<ConcreteLiteral> getListOfLiterals(Collection<? extends Literal> lit) {
        List<ConcreteLiteral> resp = new ArrayList<>();

        for (Literal l : lit) {
            resp.add(DataLogLiteral.getInstanceFromLiteral(l));
        }

        return resp;
    }

    public static Set<ConcreteLiteral> getSetOfLiterals(Collection<? extends Literal> lit) {
        Set<ConcreteLiteral> resp = new HashSet<>();

        for (Literal l : lit) {
            resp.add(DataLogLiteral.getInstanceFromLiteral(l));
        }

        return resp;
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

        if (obj instanceof ConcreteLiteral) {
            final ConcreteLiteral other = (ConcreteLiteral) obj;

            //if (this.failed != other.failed) return false;
            if (this.negative != other.isNegative())
                return false;
            if (!Objects.equals(this.head, other.getHead()))
                return false;
            if (this.arity != other.getArity())
                return false;
            if (!Objects.equals(this.terms, other.getTerms()))
                return false;
        } else {
            if (getClass() != obj.getClass())
                return false;

            final DataLogLiteral other = (DataLogLiteral) obj;

            //if (this.failed != other.failed) return false;
            if (this.negative != other.negative)
                return false;
            if (!Objects.equals(this.head, other.head))
                return false;
            if (this.arity != other.arity)
                return false;
            if (!Objects.equals(this.terms, other.terms))
                return false;
        }

        return true;
    }

    public boolean sameAs(final ConcreteLiteral lit) {
        return (equals(lit) && lit.hasFailed() == this.hasFailed());
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.failed) {
            sb.append("not ");
        }

        if (this.negative) {
            sb.append("-");
        }

        sb.append(this.head);
        sb.append("(");

        for (int i = 0; i < terms.size(); i++) {
            sb.append(terms.get(i).getName());
            if (i < terms.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    public DataLogLiteral clone() {
        DataLogLiteral resp = new DataLogLiteral(head, terms, negative);

        resp.failed = this.failed;

        return resp;
    }

}
