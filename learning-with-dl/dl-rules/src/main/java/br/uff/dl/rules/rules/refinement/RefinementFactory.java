/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.refinement;

/**
 *
 * @author Victor Guimarães
 */
public class RefinementFactory {

    public static final String REFINEMENT_PACKAGE_NAME = "br.uff.dl.rules.rules.refinement";

    protected static Refinement tryGetClass(String refinementName) {
        Refinement r = null;
        try {
            r = (Refinement) Class.forName(REFINEMENT_PACKAGE_NAME + "." + refinementName).newInstance();
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | NullPointerException |
                ClassCastException ex) {
        }

        return r;
    }

    public static Refinement getRefinement(String refinementName) {
        Refinement r = null;

        if (refinementName != null) {
            String[] names = {REFINEMENT_PACKAGE_NAME + "." + refinementName, refinementName, REFINEMENT_PACKAGE_NAME + ".TopDownBoundedRefinement"};

            int count = 0;
            while (r == null && count < names.length) {
                r = tryGetClass(names[count++]);
            }
        }
        
        if (r == null) {
            return new TopDownBoundedRefinement();
        }

        return r;
    }

}
