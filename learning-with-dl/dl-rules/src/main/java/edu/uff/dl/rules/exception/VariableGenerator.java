/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.exception;

/**
 * Interface to define a generator of variables.
 * <br>Generators of variables are used to creates generalized rules.
 * 
 * @author Victor Guimarães
 */
public interface VariableGenerator {

    /**
     * Method to get the next distinct variable from generator.
     * 
     * @return a new distinct variable.
     */
    public String getNextName();
}
