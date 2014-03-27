/*
 * UFF Project Semantic Learning
 */

package edu.uff.expansion.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class SafeRule extends Rule {
    
    public SafeRule(ConcreteDataLogPredicate horn, Collection<? extends ConcreteDataLogPredicate> terms) {
        this.horn = horn;
        this.terms = filterForSafeLiterals(horn, terms);
    }
    
    private Collection<ConcreteDataLogPredicate> filterForSafeLiterals(ConcreteDataLogPredicate horn, final Collection<? extends ConcreteDataLogPredicate> terms) {
        Set<Term> safeTerms = new HashSet<>();
        safeTerms.addAll(horn.getTerms());
        List<ConcreteDataLogPredicate> result = new ArrayList<>();
        List<? extends ConcreteDataLogPredicate> copy = new ArrayList<>(terms);
        Iterator it = copy.iterator();
        while (it.hasNext()) {
            ConcreteDataLogPredicate con = (ConcreteDataLogPredicate) it.next();
            if (!con.hasFailed()) {
                safeTerms.addAll(con.getTerms());
                result.add(con);
                it.remove();
            }
        }
        
        for (ConcreteDataLogPredicate con : copy) {
            if (safeTerms.containsAll(con.getTerms())) {
                result.add(con);
            }         
        }
        
        return result;
    }
    
}
