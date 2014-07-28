/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansionset;

import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class responsable for create the Expamsion Answer Set by a given Answer Set.
 * <br> This class's performance can be dramatically improved by giving a
 * {@link IndividualTemplate} to typify the individuals from the problem.
 *
 * @author Victor Guimarães
 */
@ComponentAnn(name = "ExpansionAnswerSet", shortName = "expset", version = 0.1)
public class ExpansionAnswerSet implements Component {

    protected List<ConcreteLiteral> answerSet;
    protected List<ConcreteLiteral> examples;

    protected List<DataLogLiteral> expansionSet;

    protected Map<List<Term>, List<List<Term>>> permuteMap;
    protected Map<Integer, List<List<Term>>> generalPermuteMap;
    protected TypeTemplate individualsClasses;

    /**
     * Constructor with only the variables allocation.
     * <br>Needed to load this class from a file (Spring).
     */
    public ExpansionAnswerSet() {
        this.permuteMap = new HashMap<>();
        this.generalPermuteMap = new HashMap<>();
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
    public ExpansionAnswerSet(Collection<? extends Literal> answerSet, Collection<? extends Literal> examples, TypeTemplate individualsClasses) {
        this();
        this.answerSet = DataLogLiteral.getListOfLiterals(answerSet);
        this.examples = DataLogLiteral.getListOfLiterals(examples);

        this.individualsClasses = individualsClasses;
    }

    /**
     * Method that does all the process of this class.
     *
     * @throws ComponentInitException in case occurs a exception during the
     * process.
     */
    @Override
    public void init() throws ComponentInitException {
        answerSet.removeAll(getExamples());

        expansionSet = new ArrayList<>();

        Collection<Clause> facts;
        for (DataLogPredicate pred : individualsClasses.getProgramPredicates()) {
            facts = individualsClasses.getTemplateFactsForPredicate(pred);
            if (facts != null && !facts.isEmpty()) {
                loadLiteralsFromFacts(expansionSet, facts);
            } else {
                System.out.println(pred);
                loadLiteralsFromFacts(expansionSet, facts, pred.getHead(), getGeneralPermuteMap(pred.getArity()));
            }
        }

        //loadLiteralsFromFacts(expansionSet, individualsClasses.getTemplateFacts());
    }

    /**
     * Loads the literal from the problem's facts.
     *
     * @param expansionSet the ExpansionAswerSet with the class's result.
     * @param facts the facts to be loaded.
     */
    protected void loadLiteralsFromFacts(Collection<DataLogLiteral> expansionSet, Collection<Clause> facts) {
        if (expansionSet == null || facts == null || facts.isEmpty())
            return;

        for (Clause c : facts) {
            List<List<Term>> list = getPermuteMap(c.getHead().getTerms());
            loadLiteralsFromFacts(expansionSet, facts, ((NormalPredicate) c.getHead().getPredicate()).getName(), list);
        }
    }

    /**
     * Loads the literal from the problem's facts based on a predicate and
     * appending into a list of lists.
     *
     * @param expansionSet the ExpansionAswerSet with the class's result.
     * @param facts the facts to be loaded.
     * @param pred the based predicate
     * @param list the list to append in.
     */
    protected void loadLiteralsFromFacts(Collection<DataLogLiteral> expansionSet, Collection<Clause> facts, String pred, List<List<Term>> list) {
        DataLogLiteral lit;
        for (List<Term> terms : list) {

            lit = new DataLogLiteral(pred, terms);
            if (!answerSet.contains(lit)) {
                lit.setFailed(true);
                expansionSet.add(lit);
            }

            lit = lit.clone();
            //lit.setFailed(false);
            lit.setNegative(true);
            if (!answerSet.contains(lit)) {
                lit.setFailed(true);
                expansionSet.add(lit);
            }
        }
    }

    /**
     * Gets a permutation from the @{@link Map} of permutations based on the
     * given key.
     * <br>If the there is not a permute for the key, is generated a permutation
     * and saved for future use.
     * <br>This function uses template.
     *
     * @param key the key of the permutation.
     * @return the permutation.
     */
    protected List<List<Term>> getPermuteMap(List<Term> key) {

        if (!permuteMap.containsKey(key)) {
            List<List<Term>> resp = permuteIndividuals(individualsClasses.getIndividualsGroups().get(key.get(0).getName()), 1);
            try {
                resp.addAll(permuteIndividuals(individualsClasses.getIndividualsGroups().get(TypeTemplate.OTHER_INDIVIDUALS), 1));
            } catch (NullPointerException ex) {

            }
            List<List<Term>> aux;
            for (int i = 1; i < key.size(); i++) {
                aux = permuteIndividuals(resp, individualsClasses.getIndividualsGroups().get(key.get(i).getName()), i + 1);
                try {
                    aux.addAll(permuteIndividuals(resp, individualsClasses.getIndividualsGroups().get(TypeTemplate.OTHER_INDIVIDUALS), i + 1));
                } catch (NullPointerException ex) {

                }
                resp = aux;
            }

            permuteMap.put(key, resp);
        }

        return permuteMap.get(key);
    }

    /**
     * Gets a permutation from the @{@link Map} of permutations based on the
     * given key.
     * <br>If the there is not a permute for the key, is generated a permutation
     * and saved for future use.
     * <br>This function uses all possibilities.
     *
     * @param key the key of the permutation (basically the predicate's arity).
     * @return the permutation.
     */
    protected List<List<Term>> getGeneralPermuteMap(int key) {
        if (!generalPermuteMap.containsKey(key)) {
            if (generalPermuteMap.isEmpty()) {
                generalPermuteMap.put(1, permuteIndividuals(individualsClasses.getIndividuals(), 1));
            }
            if (key > 1)
                generalPermuteMap.put(key, permuteIndividuals(getGeneralPermuteMap(key - 1), individualsClasses.getIndividuals(), key));
        }

        return generalPermuteMap.get(key);
    }

    /**
     * Generate the permutation list based on the given individuals with the
     * given list size.
     * <br>The list size is basically the number of individuals in a single
     * permutation possibility. In other words, is the predicate's arity.
     *
     * @param individuals the collection of individuals.
     * @param listSize the list size.
     * @return the permutation list.
     */
    protected List<List<Term>> permuteIndividuals(final Collection<? extends Constant> individuals, int listSize) {
        List<List<Term>> resp = new ArrayList<List<Term>>();

        List<Term> l;
        for (Constant ind : individuals) {
            l = new ArrayList<>();
            l.add(ind);
            resp.add(l);
        }

        return permuteIndividuals(resp, individuals, listSize);
    }

    /**
     * Generate the permutation list based on the given individuals with the
     * given list size.
     * <br>The list size is basically the number of individuals in a single
     * permutation possibility. In other words, is the predicate's arity.
     * <br>Is used recursively by
     * {@link #permuteIndividuals(java.util.Collection, int)} to generates the
     * list by appending the parcial results into another list.
     *
     * @param append the appendable list.
     * @param individuals the collection of individuals.
     * @param listSize the list size.
     * @return the permutation list.
     */
    protected List<List<Term>> permuteIndividuals(final List<List<Term>> append, final Collection<? extends Constant> individuals, int listSize) {
        List<Term> l;
        int size = listSize - (append != null ? append.get(0).size() : listSize);
        //List<List<Term>> resp = copyList(append);
        List<List<Term>> resp = new ArrayList<>(append);

        for (int i = 0; i < size; i++) {
            List<List<Term>> aux = new ArrayList<List<Term>>();

            for (List<Term> list : resp) {
                for (Constant ind : individuals) {
                    l = new ArrayList<>(list);
                    l.add(ind);
                    aux.add(l);
                }
            }

            resp = aux;
        }

        return resp;
    }

    /**
     * Getter for the ExapansioAnswerSet.
     *
     * @return the the ExapansioAnswerSet.
     */
    public List<ConcreteLiteral> getExpansionSet() {
        List<ConcreteLiteral> resp = new ArrayList<>();
        resp.addAll(answerSet);
        resp.addAll(expansionSet);
        return resp;
    }

    /**
     * Setter for the AnswerSet (DReW's output).
     *
     * @param answerSet the AnswerSet.
     */
    public void setAnswerSet(List<ConcreteLiteral> answerSet) {
        this.answerSet = answerSet;
    }

    /**
     * A text output of the {@link ExpansionAnswerSet}.
     * <br>Follows the same DReW's format.
     *
     * @return the output like text format.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (ConcreteLiteral l : answerSet) {
            sb.append(l);
            sb.append(", ");
        }

        for (ConcreteLiteral l : expansionSet) {
            //sb.append("not ");
            sb.append(l);
            sb.append(", ");
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");

        return sb.toString().trim();
    }

    /**
     * Getter for the examples.
     *
     * @return the examples.
     */
    public List<ConcreteLiteral> getExamples() {
        return examples;
    }

    /**
     * Setter for the examples.
     *
     * @param examples the examples.
     */
    public void setExamples(List<ConcreteLiteral> examples) {
        this.examples = examples;
    }

    /**
     * Setter for the {@link TypeTemplate}.
     *
     * @param individualsClasses the {@link TypeTemplate}.
     */
    public void setIndividualsClasses(TypeTemplate individualsClasses) {
        this.individualsClasses = individualsClasses;
    }

}
