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
import org.semanticweb.drew.dlprogram.model.Constant;
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
     * @param head the rule's horn (head).
     * @param terms the rule's terms (body as a collection).
     */
    public SafeRule(ConcreteLiteral head, Collection<? extends ConcreteLiteral> terms) {
        this.head = head;
        this.body = filterForSafeLiterals(head, terms);
    }

    /**
     * Constructor with all needed parameters. This constructor will filter the
     * body's terms to ensure that the rule is safe.
     *
     * @param horn the rule's horn (head).
     * @param terms the rule's terms (body as an arbitrary number of arguments).
     */
    public SafeRule(ConcreteLiteral horn, ConcreteLiteral... terms) {
        this.head = horn;
        Collection<ConcreteLiteral> termsCollection = new LinkedHashSet<>();

        for (ConcreteLiteral term : terms) {
            termsCollection.add(term);
        }

        this.body = filterForSafeLiterals(horn, termsCollection);
    }

    /**
     * Filters the rule's body to take out the terms that makes the rule
     * non-safe.
     *
     * @param horn the rule's horn.
     * @param body the rule's body.
     * @return a set with only the terms that make the rule safe.
     */
    private Set<ConcreteLiteral> filterForSafeLiterals(ConcreteLiteral horn, final Collection<? extends ConcreteLiteral> body) {
        Set<Constant> safeTerms = new HashSet<>();
        //safeTerms.addAll(horn.getTerms());
        Set<ConcreteLiteral> result = new LinkedHashSet<>();
        List<? extends ConcreteLiteral> copy = new ArrayList<>(body);
        Iterator<? extends ConcreteLiteral> it = copy.iterator();
        while (it.hasNext()) {
            ConcreteLiteral con = it.next();
            if (!con.hasFailed()) {
                for (Term term : con.getTerms()) {
                    safeTerms.add(new Constant(term.getName()));
                }
                //safeTerms.addAll(con.getTerms());
                result.add(con);
                it.remove();
            }
        }

        for (ConcreteLiteral con : copy) {
            if (containsAll(safeTerms, con.getTerms()) && !horn.getPredicate().equals(con.getPredicate())) {
                result.add(con);
            }
        }

        return result;
    }

    /**
     * Checks if the rule is impossible to be safe.
     *
     * @param rule the rule
     * 
     * @return True if is impossible, false otherwise.
     */
    public static boolean isImpossibleSafe(Rule rule) {
        Set<Constant> safeTerms = new HashSet<>();
        List<? extends ConcreteLiteral> copy = new ArrayList<>(rule.getBody());
        Iterator<? extends ConcreteLiteral> it = copy.iterator();
        while (it.hasNext()) {
            ConcreteLiteral con = it.next();
            if (!con.hasFailed()) {
                for (Term term : con.getTerms()) {
                    safeTerms.add(new Constant(term.getName()));
                }
                //safeTerms.addAll(con.getTerms());
                
                it.remove();
            }
        }

        if (!containsAll(safeTerms, rule.getHead().getTerms())) {
            return true;
        }

        return false;
    }

    private static boolean containsAll(Set<Constant> safeTerms, List<Term> conTerms) {
        Set<Constant> terms = new HashSet<>();
        for (Term term : conTerms) {
            terms.add(new Constant(term.getName()));
        }

        return safeTerms.containsAll(terms);
    }

}
