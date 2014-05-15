/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.util;

/**
 *
 * @author Victor
 */
public class Box<T> {
    
    T content;

    public Box(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content.toString();
    }
    
}
