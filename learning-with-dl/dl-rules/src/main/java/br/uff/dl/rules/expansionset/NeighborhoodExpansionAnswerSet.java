package br.uff.dl.rules.expansionset;

import br.uff.dl.rules.datalog.DataLogLiteral;
import br.uff.dl.rules.datalog.DataLogPredicate;
import br.uff.dl.rules.template.TypeTemplate;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created on 04/11/16.
 *
 * @author Victor Guimarães
 */
public class NeighborhoodExpansionAnswerSet extends ExampleExpansionAnswerSet {

    protected Collection<? extends Constant> termsSubset;
    protected Collection<? extends DataLogPredicate> predicatesSubset;

    public NeighborhoodExpansionAnswerSet() {
    }

    public NeighborhoodExpansionAnswerSet(Collection<? extends Constant> termsSubset,
                                          Collection<? extends DataLogPredicate> predicatesSubset) {
        this.termsSubset = termsSubset;
        this.predicatesSubset = predicatesSubset;
    }

    public NeighborhoodExpansionAnswerSet(Collection<? extends Literal> answerSet,
                                          Collection<? extends Literal> examples,
                                          TypeTemplate individualsClasses,
                                          PrintStream outStream,
                                          Collection<? extends Constant> termsSubset,
                                          Collection<? extends DataLogPredicate> predicatesSubset) {
        super(answerSet, examples, individualsClasses, outStream);
        this.termsSubset = termsSubset;
        this.predicatesSubset = predicatesSubset;
    }

    public NeighborhoodExpansionAnswerSet(Collection<? extends Literal> answerSet,
                                          Collection<? extends Literal> examples,
                                          TypeTemplate individualsClasses,
                                          PrintStream outStream) {
        super(answerSet, examples, individualsClasses, outStream);
    }

    public void setTermsSubset(Collection<? extends Constant> termsSubset) {
        this.termsSubset = termsSubset;
    }

    public void setPredicatesSubset(Collection<? extends DataLogPredicate> predicatesSubset) {
        this.predicatesSubset = predicatesSubset;
    }

    @Override
    protected Collection<? extends DataLogPredicate> getProgramPredicates() {
        Collection<? extends DataLogPredicate> answer = new HashSet<>(predicatesSubset);
        answer.retainAll(super.getProgramPredicates());
        return answer;
    }

    @Override
    protected Collection<? extends Constant> getIndividualsFromGroup(String key) {
        Collection<? extends Constant> answer = new HashSet<>(termsSubset);
        answer.retainAll(super.getIndividualsFromGroup(key));
        return answer;
    }

    @Override
    protected void addLiteralToExpansionSet(Collection<DataLogLiteral> expansionSet, DataLogLiteral lit) {
        lit.setNegative(getExample().isNegative());
        if (!answerSet.contains(lit)) {
            lit.setFailed(true);
            expansionSet.add(lit);
        }
    }

}
