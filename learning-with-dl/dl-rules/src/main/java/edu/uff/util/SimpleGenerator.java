/*
 * UFF Project Semantic Learning
 */
package edu.uff.util;

/**
 *
 * @author Victor
 */
public class SimpleGenerator implements VariableGenerator {

    private String name = "@";

    public String getName() {
        return name;
    }

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
