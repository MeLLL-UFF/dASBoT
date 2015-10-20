/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.datalog;

import java.util.Comparator;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *  A comparator to order a collection of {@link Term}.
 * 
 * @author Victor Guimarães
 */
public class TermComparator implements Comparator<Term> {

    @Override
    public int compare(Term o1, Term o2) {
        return o1.toString().compareTo(o2.toString());
    }
    
}
