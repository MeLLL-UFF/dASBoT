/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import br.uff.dl.rules.exception.VariableGenerator;

/**
 * This class is a generator of variables. It implements
 * {@link VariableGenerator} and can be used for creating generalized rules.
 *
 * @author Victor Guimarães
 */
public class SimpleGenerator implements VariableGenerator {

    private String name = "@";

    /**
     * Getter for the name. The name is a {@link String} used to generate the
     * variables.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String getNextName() {
        int c = name.charAt(name.length() - 1);

        if (c == 90) {
            c = 64;
            name = name.substring(0, name.length() - 1) + (char) (c + 1);
            name = "A" + name;
        } else {
            name = name.substring(0, name.length() - 1) + (char) (c + 1);
        }

        return getName();
    }

}
