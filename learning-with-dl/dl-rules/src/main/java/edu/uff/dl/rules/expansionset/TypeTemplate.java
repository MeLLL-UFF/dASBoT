/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansionset;

import edu.uff.dl.rules.datalog.DataLogPredicate;
import java.util.Map;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;

/**
 * Interface to describe classes that provides templates of individuals's
 * typifications inside a specific problem.
 *
 * @author Victor Guimarães
 */
public interface TypeTemplate {

    public static final String OTHER_INDIVIDUALS = "+OTHERS";

    /**
     * Getter for a {@link Map} of groups of individuals where the key is the
     * type of the group.
     *
     * @return a {@link Map} of groups of individuals
     */
    public Map<String, Set<? extends Constant>> getIndividualsGroups();

    /**
     * Getter for a {@link Set} of individuals of the problem.
     *
     * @return a {@link Set} of individuals of the problem.
     */
    public Set<Constant> getIndividuals();

    /**
     * Getter for a {@link Set} of predicates of the problem.
     *
     * @return a {@link Set} of predicates of the problem.
     */
    public Set<? extends DataLogPredicate> getProgramPredicates();

    /**
     * Getter for a {@link Set} of facts from the template.
     *
     * @return a {@link Set} of facts from the template.
     */
    public Set<Clause> getTemplateFacts();

    /**
     * Getter for a {@link Set} of facts from the template for the given {@link DataLogPredicate}.
     *
     * @param pred the {@link DataLogPredicate} to filter the {@link Set} of facts.
     * @return a {@link Set} of facts from the template.
     */
    public Set<Clause> getTemplateFactsForPredicate(DataLogPredicate pred);

}
