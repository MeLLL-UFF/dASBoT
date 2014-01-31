/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uff.dl.rules.example;

import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;

/**
 *
 * @author Victor
 */
public class AtomTerm {
    private Atom atom;
    private static PrologParser pp = new PrologParser();
    
    public AtomTerm(String atomString) throws ParseException {
        atom = pp.parseAtom(atomString);
    }

    public Atom getAtom() {
        return atom;
    }

    @Override
    public String toString() {
        return atom.toString();
    }

}
