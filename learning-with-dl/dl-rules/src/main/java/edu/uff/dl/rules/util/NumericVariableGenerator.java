/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.util;

/**
 *
 * @author Victor
 */
public class NumericVariableGenerator implements VariableGenerator {
    private char letter;
    private int count;// = Integer.MIN_VALUE;

    public NumericVariableGenerator() {
        this.letter = 'X';
        this.count = Integer.MIN_VALUE;
    }

    public NumericVariableGenerator(char letter) {
        this.letter = letter;
    }

    public NumericVariableGenerator(char letter, int count) {
        this.letter = String.valueOf(letter).toUpperCase().charAt(0);
        this.count = count + Integer.MIN_VALUE;
    }

    @Override
    public String getNextName() {
        return String.valueOf(letter) + getCount();
    }
    
    private int getCount() {
        return count++ + -Integer.MIN_VALUE;
    }
    
    
}
