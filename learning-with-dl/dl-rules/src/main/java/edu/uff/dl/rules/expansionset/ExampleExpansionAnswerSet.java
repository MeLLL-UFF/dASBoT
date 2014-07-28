/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansionset;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class responsable for create the Expamsion Answer Set based on a example by a
 * given Answer Set and the example.
 * <br> As this class bases its output on a example, it just generates the
 * literals that are considered relevants to the based example. Is not a
 * Exampansion Answer Set of the whole program but based on a single example.
 * This technique make the process faster, the rule to be generate next must be
 * for this example, instead.
 * <br> This class's performance can be dramatically improved by giving a
 * {@link IndividualTemplate} to typify the individuals from the problem.
 *
 * @author Victor Guimarães
 */
public class ExampleExpansionAnswerSet extends ExpansionAnswerSet {

    protected ConcreteLiteral example;
    protected Collection<Constant> exampleTerms;
    protected int offset = 0;

    /**
     * Constructor with only the variables allocation.
     * <br>Needed to load this class from a file (Spring).
     */
    public ExampleExpansionAnswerSet() {

    }

    /**
     * Constructor with all the needed variable to do the process.
     *
     * @param answerSet the answer set from a DReW's result.
     * @param examples a collection of examples of the problem.
     * @param individualsClasses a {@link IndividualTemplate} to typify the
     * individual. This class is needed, if you do not have a template, create a
     * instance of this class by passing a empty file.
     */
    public ExampleExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> examples, TypeTemplate individualsClasses) {
        super(answerSet, examples, individualsClasses);
    }

    /**
     * Generate the permutation list based on the given individuals with the
     * given list size.
     * <br> Just does the permutations relevants to the based example.
     * <br> The list size is basically the number of individuals in a single
     * permutation possibility. In other words, is the predicate's arity.
     *
     * @param individuals the collection of individuals.
     * @param listSize the list size.
     * @return the permutation list.
     */
    @Override
    protected List<List<Term>> permuteIndividuals(final Collection<? extends Constant> individuals, int listSize) {
        List<List<Term>> resp = new ArrayList<List<Term>>();
        if (individuals == null)
            return null;
        List<Term> l;
        for (Constant ind : individuals) {
            l = new ArrayList<>();
            l.add(ind);
            if (listSize == 1) {
                if (isRelevant(getTermsFromExample(), l))
                    resp.add(l);
            } else {
                resp.add(l);
            }
        }

        return permuteIndividuals(resp, individuals, listSize);
    }

    @Override
    protected List<List<Term>> permuteIndividuals(final List<List<Term>> append, final Collection<? extends Constant> individuals, int listSize) {

        List<Term> l;

        int size = listSize - (append != null && !append.isEmpty() ? append.get(0).size() : listSize);

        List<List<Term>> resp = new ArrayList<>(append);
        List<List<Term>> noRelevants = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<List<Term>> aux = new ArrayList<List<Term>>();

            for (List<Term> list : resp) {
                for (Constant ind : individuals) {
                    l = new ArrayList<>(list);
                    if (i == size - 1) {
                        if (isRelevant(getTermsFromExample(), l) || isRelevant(getTermsFromExample(), ind)) {
                            l.add(ind);
                            aux.add(l);
                        } else {
                            noRelevants.add(l);
                            break;
                        }
                    } else {
                        l.add(ind);
                        aux.add(l);
                    }
                }
            }

            resp = aux;
        }
        if (!noRelevants.isEmpty()) {
            resp.addAll(permuteIndividuals(noRelevants, getTermsFromExample(), listSize));
            //noRelevants.addAll(resp);
            //resp = noRelevants;

            //System.out.println(noRelevants);
        }

        //resp.addAll(noRelevants);
        return resp;
    }

    /**
     * Method to define if the specified permutation is relevante to the based
     * example.
     *
     * @param exampleTerms the example's terms.
     * @param list the specified permutation.
     * @return true if it is relevant, false otherwise.
     */
    private static boolean isRelevant(Collection<Constant> exampleTerms, List<Term> list) {
        if (exampleTerms == null || list == null)
            return false;
        boolean intersection = false;

        for (Term term : exampleTerms) {
            if (list.contains(term)) {
                intersection = true;
                break;
            }
        }

        return intersection;
    }

    /**
     * Method to define if a term is relevante to the based example.
     *
     * @param exampleTerms the example's terms.
     * @param term the term.
     * @return true if it is relevant, false otherwise.
     */
    private static boolean isRelevant(Collection<Constant> exampleTerms, Constant term) {
        if (exampleTerms == null || term == null)
            return false;

        return (exampleTerms.contains(term));
    }

    @Override
    protected List<List<Term>> getGeneralPermuteMap(int key) {

        if (!generalPermuteMap.containsKey(key)) {
//            if (generalPermuteMap.isEmpty()) {
//                generalPermuteMap.put(1, permuteIndividuals(individualsClasses.getIndividuals(), 1));
//            }

            generalPermuteMap.put(key, permuteIndividuals(individualsClasses.getIndividuals(), key));
        }

        return generalPermuteMap.get(key);
    }

    /**
     * Gets the terms from the based example.
     *
     * @return the terms from the based example.
     */
    private List<Constant> getTermsFromExample() {
        List<Constant> resp = new ArrayList<>();
        for (Term term : getExample().getTerms()) {
            if (term instanceof Constant) {
                resp.add((Constant) term);
            } else {
                resp.add(new Constant(term.getName()));
                System.err.println("Constant Error!");
            }
        }
        return resp;
    }

    /**
     * Getter for the based example.
     *
     * @return the based example.
     */
    protected ConcreteLiteral getExample() {
        if (example == null) {
            example = getExamples().get(0);
        }

        return example;
    }

    /**
     * Getter for the based example on a single-element list.
     *
     * @return the based example on a single-element list.
     */
    public List<ConcreteLiteral> getExamples() {
        return examples.subList(offset, offset + 1);
    }

    protected Collection<Constant> getExampleTerms() {
        if (exampleTerms == null) {
            exampleTerms = getTermsFromExample();
        }
        return exampleTerms;
    }

    /**
     * Setter to set a specific example.
     *
     * @param example the example.
     */
    public void setExample(ConcreteLiteral example) {
        this.example = example;
    }

    /**
     * Getter for the offset.
     * <br> The offset is to define which example on the list of example will be
     * used as the based example.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Setter for the offset.
     * <br> The offset is to define which example on the list of example will be
     * used as the based example.
     *
     * @param offset the offset.
     */
    public void setOffset(int offset) {
        if (offset < this.examples.size()) {
            this.offset = offset;
        }
    }

}
