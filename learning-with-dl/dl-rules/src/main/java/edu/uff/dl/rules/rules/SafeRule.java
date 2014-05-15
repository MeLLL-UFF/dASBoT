/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.rules;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class SafeRule extends Rule {
    
    public SafeRule(ConcreteLiteral horn, Collection<? extends ConcreteLiteral> terms) {
        this.horn = horn;
        this.terms = filterForSafeLiterals(horn, terms);
    }
    
    private Set<ConcreteLiteral> filterForSafeLiterals(ConcreteLiteral horn, final Collection<? extends ConcreteLiteral> terms) {
        Set<Term> safeTerms = new HashSet<>();
        //safeTerms.addAll(horn.getTerms());
        Set<ConcreteLiteral> result = new LinkedHashSet<>();
        List<? extends ConcreteLiteral> copy = new ArrayList<>(terms);
        Iterator<? extends ConcreteLiteral> it = copy.iterator();
        while (it.hasNext()) {
            ConcreteLiteral con = it.next();
            if (!con.hasFailed()) {
                safeTerms.addAll(con.getTerms());
                result.add(con);
                it.remove();
            }
        }
        
        for (ConcreteLiteral con : copy) {
            if (safeTerms.containsAll(con.getTerms())) {
                result.add(con);
            }         
        }
        
        return result;
    }
    
}
