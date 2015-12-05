/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.template;

/**
 * Class used to keep constants during the rule's generalization. This class
 * links a predicate with its terms and specifies which of its terms should be
 * kept as constants.
 *
 * @author Victor Guimarães
 */
public class TermType {

    protected String type;
    protected boolean constant;

    /**
     * Constructor with the type's name. If the type's name starts with "#" it
     * will be considered as constant, if not, will not be considered.
     *
     * @param type the type's name.
     */
    public TermType(String type) {
        if (type.startsWith("#")) {
            this.type = type.substring(1);
            this.constant = true;
        } else {
            this.type = type;
            this.constant = false;
        }
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param type the type's name.
     * @param constant if it is or not a constant.
     */
    public TermType(String type, boolean constant) {
        this.type = type;
        this.constant = constant;
    }

    /**
     * Getter for the type's name.
     *
     * @return the type's name.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for the type's name.
     *
     * @param type the type's name.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for the constant condition.
     *
     * @return the constant condition.
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * Setter for the constant condition.
     *
     * @param constant the constant condition.
     */
    public void setConstant(boolean constant) {
        this.constant = constant;
    }

}
