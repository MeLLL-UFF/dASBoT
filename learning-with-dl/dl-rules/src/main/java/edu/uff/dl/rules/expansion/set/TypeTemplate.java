/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansion.set;

import edu.uff.dl.rules.datalog.DataLogPredicate;
import java.util.Map;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;

/**
 *
 * @author Victor
 */
public interface TypeTemplate {

    public static final String OTHER_INDIVIDUALS = "+OTHERS";

    public Map<String, Set<? extends Constant>> getIndividualsGroups();

    public Set<Constant> getIndividuals();

    public Set<? extends DataLogPredicate> getProgramPredicates();

    public Set<Clause> getTemplateFacts();
    
    public Set<Clause> getTemplateFactsForPredicate(DataLogPredicate pred);

}
