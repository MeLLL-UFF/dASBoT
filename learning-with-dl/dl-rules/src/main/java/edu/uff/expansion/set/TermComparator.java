/*
 * UFF Project Semantic Learning
 */

package edu.uff.expansion.set;

import org.semanticweb.drew.dlprogram.model.Term;
import java.util.Comparator;

/**
 *
 * @author Victor
 */
public class TermComparator implements Comparator<Term> {

    @Override
    public int compare(Term o1, Term o2) {
        return o1.toString().compareTo(o2.toString());
    }
    
}
