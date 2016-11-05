package br.uff.dl.rules.rules;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.datalog.DataLogPredicate;
import br.uff.dl.rules.datalog.DataLogRule;
import br.uff.dl.rules.expansionset.NeighborhoodExpansionAnswerSet;
import br.uff.dl.rules.rules.theory.TheoryBuilder;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static br.uff.dl.rules.rules.refinement.Refinement.getAllConstantsFromRulesBody;
import static br.uff.dl.rules.rules.refinement.Refinement.getAllPredicatesFromRule;
import static br.uff.dl.rules.util.Time.getTime;

/**
 * {@inheritDoc}
 * <p>
 * Despite the {@link RuleGenerator}, this class looks only at a subset of literal around the based example. The
 * radius of the neighborhood is defined by the {@link #depth} attribute.
 *
 * @author Victor Guimarães
 */
public class NeighborhoodRuleGenerator extends RuleGenerator {

    /**
     * The method that does all the process for a single example.
     * <br>Call {@link #init()} before.
     * <br>This method gets the previously obtained results from DReW by the
     * {@link #init()}, and generates the rule based on the specified {@link #depth}.
     * <br>With the rule, it creates a sub sample of the Expansion Answer Set,
     * using only the terms appearing in the rule based.
     * <br> After that, the rule is variabilized.
     * <p>
     * The example is selected according with the
     * offset. The offset can be between [0, N) where N is the total number of
     * examples within the given content.
     */
    @Override
    public void run() {
        AnswerSetRule aes;
        //loadFromAnswerSet(individuals, predicates);
        NeighborhoodExpansionAnswerSet e;
        try {
            Set<Literal> literalAnswerSet = drew.getLiteralModelHandler().getAnswerSets().iterator().next();
            List<ConcreteLiteral> concreteAnswerSet = new ArrayList<>(TheoryBuilder.literalsToConcreteLiterals
                    (literalAnswerSet));

            outStream.println("Iniciar Geração da Regra: " + getTime());
            outStream.println("");
            outStream.println("Gerando regra com profundidade de variáveis: " + depth);
            e = new NeighborhoodExpansionAnswerSet(literalAnswerSet, examples, individualTemplate, outStream);
            e.setOffset(offset);

            AnswerRule ar = new AnswerRule(e.getExamples(), concreteAnswerSet, depth, individualTemplate, outStream);
            ar.setRecursive(recursiveRuleAllowed);
            Rule rule = ar.generateRule();
            if (SafeRule.isImpossibleSafe(rule)) {
                return;
            }

            Set<Constant> terms = getAllConstantsFromRulesBody(rule);
            Set<? extends DataLogPredicate> predicates = getAllPredicatesFromRule(rule);

            outStream.println("Iniciar Geração do Conjunto Expandido: " + getTime());

            e.setTermsSubset(terms);
            e.setPredicatesSubset(predicates);
            e.init();

            List<ConcreteLiteral> neighborhoodExpansionAnswerSet = new ArrayList<>(e.getExpansionSet());
            neighborhoodExpansionAnswerSet.addAll(rule.body);
//            ar = new AnswerRule(e.getExamples(), neighborhoodExpansionAnswerSet, depth, individualTemplate, outStream);
            ar.setAnswerSet(neighborhoodExpansionAnswerSet);
            ar.setRecursive(recursiveRuleAllowed);
            ar.init();

            DataLogRule r = ar.getRules().iterator().next();
//            ar = new AnswerRule(e.getExamples(), e.getFullExpansionAnswerSet(), depth, individualTemplate, outStream);
            ar.setAnswerSet(e.getFullExpansionAnswerSet());
            ar.setRecursive(recursiveRuleAllowed);
            ar.getRules().add(r);

            aes = new AnswerSetRule(e, ar);
            answerSetRules.add(aes);
            examplesForRule = e.getExamples();

            outStream.println("");
            outStream.println(e.getClass());
            outStream.println("");
        } catch (Exception ex) {
            Logger.getLogger(RuleGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
