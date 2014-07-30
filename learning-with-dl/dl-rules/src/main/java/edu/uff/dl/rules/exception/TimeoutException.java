/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.exception;

/**
 * Class to describe a Exception caused by timeout.
 *
 * @author Victor Guimarães.
 */
public class TimeoutException extends Exception {

    /**
     * Default constructor without parameters.
     */
    public TimeoutException() {
    }

    /**
     * Constructor with a message.
     *
     * @param message the message.
     */
    public TimeoutException(String message) {
        super(message);
    }

}
