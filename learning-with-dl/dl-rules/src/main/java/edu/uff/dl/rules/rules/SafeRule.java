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
 * Class to represent a logic rule.
 * <br> This class ensure that the rule is safe.
 * <br> This class can be load from a non-safe rule.
 *
 * @author Victor Guimarães
 */
public class SafeRule extends Rule {

    /**
     * Constructor to load a {@link SafeRule} from a {@link Rule}.
     *
     * @param rule the rule.
     */
    public SafeRule(Rule rule) {
        this(rule.getHead(), rule.getBody());
    }

    /**
     * Constructor with all needed parameters. This constructor will filter the
     * body's terms to ensure that the rule is safe.
     *
     * @param horn the rule's horn (head).
     * @param terms the rule's terms (body as a collection).
     */
    public SafeRule(ConcreteLiteral horn, Collection<? extends ConcreteLiteral> terms) {
        this.horn = horn;
        this.terms = filterForSafeLiterals(horn, terms);
    }

    /**
     * Constructor with all needed parameters. This constructor will filter the
     * body's terms to ensure that the rule is safe.
     *
     * @param horn the rule's horn (head).
     * @param terms the rule's terms (body as an arbitrary number of arguments).
     */
    public SafeRule(ConcreteLiteral horn, ConcreteLiteral... terms) {
        this.horn = horn;
        Collection<ConcreteLiteral> termsCollection = new LinkedHashSet<>();

        for (ConcreteLiteral term : terms) {
            termsCollection.add(term);
        }

        this.terms = filterForSafeLiterals(horn, termsCollection);
    }

    /**
     * Filters the rule's body to take out the terms that makes the rule
     * non-safe.
     *
     * @param horn the rule's horn.
     * @param terms the rule's body.
     * @return a set with only the terms that make the rule safe.
     */
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
            if (safeTerms.containsAll(con.getTerms()) && ! horn.getHead().equals(con.getHead())) {
                result.add(con);
            }
        }

        return result;
    }

}
