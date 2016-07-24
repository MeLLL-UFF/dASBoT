/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.refinement;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import org.semanticweb.drew.dlprogram.model.Term;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Victor Guimarães
 */
public class Path {

    protected Set<ConcreteLiteral> literals;
    protected ConcreteLiteral last;

    protected Set<Term> safeTerms;

    public Path() {
        this.literals = new HashSet<>();
        this.safeTerms = new HashSet<>();
    }

    public Path(Path other) {
        this.literals = new HashSet<>(other.literals);
        this.last = other.last;
        this.safeTerms = new HashSet<>(other.safeTerms);
    }

    public boolean contains(ConcreteLiteral o) {
        return literals.contains(o);
    }

    public boolean add(ConcreteLiteral o) {
        boolean add = literals.add(o);
        if (add) {
            last = o;
            if (!o.hasFailed()) {
                safeTerms.addAll(o.getTerms());
            }
        }

        return add;
    }

    public ConcreteLiteral getLast() {
        return last;
    }
    
    public boolean containsAllSafeTerms(Collection<?> c) {
        return safeTerms.containsAll(c);
    }

    public Set<ConcreteLiteral> getLiterals() {
        return literals;
    }

    @Override
    public String toString() {
        return "Path{" + "literals=" + literals + ", last=" + last + ", safeTerms=" + safeTerms + '}';
    }

    
    
}
