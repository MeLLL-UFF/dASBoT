/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.exception;

/**
 * Class to handle the possible exceptions that can occur by create the
 * Expansion Answer Sets.
 *
 * @author Victor Guimarães
 */
class ExpansionAnswerSetException extends Exception {

    /**
     * Constructor with the exception message.
     *
     * @param msg the exception's message.
     */
    public ExpansionAnswerSetException(String msg) {
        super(msg);
    }

}
