/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules;

/**
 * Class to a Safe Rule Exception. This exception is thrown if a {@link Rule}
 * that can not be safe is forced to be safe.
 *
 * @author Victor Guimarães
 */
public class ImpossibleSafeRuleException extends Exception {

    public static final String MESSAGE = "Impossible to this rule be safe! "
            + "This probably means that this rule has a variable in its head "
            + "which does not appears at its body.";

    public ImpossibleSafeRuleException() {
        super(MESSAGE);
    }

    public ImpossibleSafeRuleException(String message) {
        super(message);
    }

}
