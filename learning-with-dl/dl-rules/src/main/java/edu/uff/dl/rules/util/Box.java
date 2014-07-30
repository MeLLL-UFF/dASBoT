/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

/**
 * Class that keeps a reference to another class. Useful to simulate a parameter
 * passed by reference. Can also be used to get a reference within.
 *
 * @author Victor Guimarães
 * @param <T> the reference's type.
 */
public class Box<T> {

    T content;

    /**
     * Constructor with the class to be referenced.
     *
     * @param content the class.
     */
    public Box(T content) {
        this.content = content;
    }

    /**
     * Getter for the content, the reference within.
     *
     * @return the content.
     */
    public T getContent() {
        return content;
    }

    /**
     * Setter for the content, the reference within.
     *
     * @param content the content.
     */
    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content.toString();
    }

}
