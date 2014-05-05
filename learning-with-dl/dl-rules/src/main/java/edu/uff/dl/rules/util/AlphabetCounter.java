/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author Victor
 */
public class AlphabetCounter implements VariableGenerator {

    private int count;

    @Override
    public String getNextName() {
        count++;
        return getVariable(count);
    }

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (count > 0) this.count = count;
    }

}
