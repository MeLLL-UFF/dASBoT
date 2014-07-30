/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import edu.uff.dl.rules.exception.VariableGenerator;

/**
 * This class is a generator of variables. It implements
 * {@link VariableGenerator} and can be used for creating generalized rules.
 *
 * @author Victor Guimarães
 */
public class NumericVariableGenerator implements VariableGenerator {

    private char letter;
    private int count;// = Integer.MIN_VALUE;

    /**
     * Constructor for the class. Initiates the variable letter and the counter.
     * The default letter is "X".
     */
    public NumericVariableGenerator() {
        this.letter = 'X';
        this.count = Integer.MIN_VALUE;
    }

    /**
     * Constructor for the class. Initiates the variable letter with the given
     * letter and the counter.
     *
     * @param letter the letter.
     */
    public NumericVariableGenerator(char letter) {
        this.letter = letter;
    }

    /**
     * Constructor for the class. Initiates the variable letter with the given
     * letter and the given counter.
     *
     * @param letter the letter.
     * @param count the counter.
     */
    public NumericVariableGenerator(char letter, int count) {
        this.letter = String.valueOf(letter).toUpperCase().charAt(0);
        this.count = count + Integer.MIN_VALUE;
    }

    @Override
    public String getNextName() {
        return String.valueOf(letter) + getCount();
    }

    /**
     * Getter fot the counter. This method moves the counter to use all the
     * possibilites from the integer variable.
     *
     * @return the counter.
     */
    private int getCount() {
        return count++ + -Integer.MIN_VALUE;
    }

}
