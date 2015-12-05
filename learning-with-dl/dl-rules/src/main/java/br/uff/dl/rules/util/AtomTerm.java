/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;

/**
 * Class used to get an Atom used by DL-Learner from a {@link String}.
 *
 * @author Victor Guimarães
 */
public class AtomTerm {

    private Atom atom;
    private static PrologParser pp = new PrologParser();

    /**
     * Constructor with all needed parameters.
     *
     * @param atomString the Atom on the {@link String} form.
     * @throws ParseException in case the {@link String} does not accord with
     * the language.
     */
    public AtomTerm(String atomString) throws ParseException {
        atom = pp.parseAtom(atomString);
    }

    /**
     * Getter for the {@link Atom}.
     *
     * @return the {@link Atom}.
     */
    public Atom getAtom() {
        return atom;
    }

    @Override
    public String toString() {
        return atom.toString();
    }

}
