/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import br.uff.dl.rules.exception.VariableGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is a generator of variables. It implements {@link VariableGenerator}
 * and can be used for creating generalized rules.
 *
 * @author Victor Guimarães
 */
public class AlphabetCounter implements VariableGenerator {

    private int count;

    @Override
    public String getNextName() {
        count++;
        return getVariable(count);
    }

    /**
     * Method to generate the variable based on a number.
     *
     * @param number the number.
     * @return the variable.
     */
    private String getVariable(int number) {
        List<Integer> conversion = new LinkedList<>();
        int rest;
        int div = number;
        do {
            rest = (char) (div % 26);
            div = div / 26;
            conversion.add(rest);
        } while (div > 0);

        char letter[] = new char[conversion.size()];
        int count = letter.length - 1;
        for (Integer l : conversion) {
            if (conversion.size() > 1 && count == letter.length - 1) {
                letter[count] = (char) (l + 65);
            } else {
                letter[count] = (char) (l + 64);
            }
            count--;
        }

        return String.valueOf(letter);
    }

    /**
     * Getter for the counter.
     *
     * @return the counter.
     */
    public int getCount() {
        return count;
    }

    /**
     * Setter for the counter.
     *
     * @param count the counter.
     */
    public void setCount(int count) {
        if (count > 0)
            this.count = count;
    }

}
