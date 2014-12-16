/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.datalog.DataLogRule;
import edu.uff.dl.rules.exception.VariableGenerator;
import edu.uff.dl.rules.template.TermType;
import edu.uff.dl.rules.template.TypeTemplate;
import edu.uff.dl.rules.util.SimpleGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class that generates a rule based on a example and a given Expansion Answer
 * Set. This class has a list of examples and pick a example at random to based
 * the rule on. If you wish to create a rule based on a specific example, you
 * should pass a single-element list.
 * <br><br>
 * This class was designed to generate a set of rules from several examples
 * picked at random. But, for now, it generates only a rule.
 *
 * @author Victor Guimarães
 */
@ComponentAnn(name = "AnswerRule", shortName = "AnswerRule", version = 0.1)
public class AnswerRule implements Component {

    private List<ConcreteLiteral> examples;

    private List<ConcreteLiteral> uncoveredExamples;
    private List<ConcreteLiteral> coveredExamples;

    private List<ConcreteLiteral> answerSet;

    private Random randomGenerator = new Random();

    private Set<DataLogRule> rules;

    private int transitivityDepth = 1;

    private boolean recursive = true;

    private TypeTemplate template;

    /**
     * Constructor without parameters. Needed to load the class from a file
     * (Spring). Just allocates the variables.
     */
    public AnswerRule() {
        this.uncoveredExamples = new ArrayList<>();
        this.coveredExamples = new ArrayList<>();
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param examples the list of examples.
     * @param answerSet the expansion answer set.
     */
    public AnswerRule(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet) {
        this();
        this.examples = examples;
        this.answerSet = answerSet;
        this.uncoveredExamples.addAll(examples);
    }

    /**
     * Constructor with all needed parameters and a transitivity depth parameter
     * (optional). <br> The transitive depth defines how deep should be consider
     * a transitivity relation between the predicates to it be considered as
     * relevant.
     * <br> The transitivity is setted by default as 1. Increase this value
     * might increase the complexity exponentially.
     *
     * @param examples the list of examples.
     * @param answerSet the expansion answer set.
     * @param transitivityDepth the transitivity depth.
     */
    public AnswerRule(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet, int transitivityDepth) {
        this(examples, answerSet);
        this.transitivityDepth = transitivityDepth;
    }

    /**
     * Constructor with all needed parameters and a transitivity depth parameter
     * (optional). <br> The transitive depth defines how deep should be consider
     * a transitivity relation between the predicates to it be considered as
     * relevant.
     * <br> The transitivity is setted by default as 1. Increase this value
     * might increase the complexity exponentially.
     * <br> The template is necessary for create rules with constants, if you
     * will not use this functionality, there is need for template.
     *
     * @param examples the list of examples.
     * @param answerSet the expansion answer set.
     * @param transitivityDepth the transitivity depth.
     * @param template the type template.
     */
    public AnswerRule(List<ConcreteLiteral> examples, List<ConcreteLiteral> answerSet, int transitivityDepth, TypeTemplate template) {
        this(examples, answerSet);
        this.transitivityDepth = transitivityDepth;
        this.template = template;
    }

    /**
     * Method that does all the rule generation.
     *
     * @throws ComponentInitException in case something goes wrong.
     */
    @Override
    public void init() throws ComponentInitException {
        rules = new HashSet<>();
        rules.add(getRule());
    }

    /**
     * Gets a rule based on a random example.
     *
     * @return the rule.
     */
    public Rule getRule() {
        ConcreteLiteral example = pickExampleAtRandom();

        Set<? extends ConcreteLiteral> relevants = getRelevants(example);

        System.out.println("Rule based on example: " + example);
        System.out.println("");

        VariableGenerator v = new SimpleGenerator();

        Map<Term, String> map = new HashMap<>();
        Map<Term, Set<Term>> typeMap = new HashMap<>();
        List<Term> terms;
        Collection<ConcreteLiteral> body = new LinkedHashSet<>();
        DataLogLiteral l;

        DataLogLiteral s;
        s = new DataLogLiteral(example.getHead(), example.getTerms(), example.isNegative());
        checkForCorrectTypes(example, typeMap);
        s.setFailed(true);

        relevants.remove(s);

        Map<String, List<List<TermType>>> constantsMap = template.getConstantMap();
        Map<String, Set<? extends Constant>> individualGroups = template.getIndividualsGroups();
        List<TermType> constantList;
        int index;
        for (ConcreteLiteral con : relevants) {
            if (!checkForCorrectTypes(con, typeMap)) {
                continue;
            }

            constantList = getTermsWithConstants(constantsMap.get(con.getHead()), con, individualGroups);
            terms = new ArrayList<>(con.getArity());
            if (constantList == null) {
                for (Term term : con.getTerms()) {
                    if (!map.containsKey(term)) {
                        map.put(term, v.getNextName());
                    }

                    terms.add(new Constant(map.get(term)));
                }
            } else {
                index = 0;
                for (Term term : con.getTerms()) {
                    if (constantList.get(index).isConstant()) {
                        terms.add(new Constant(term.getName()));
                    } else {
                        if (!map.containsKey(term)) {
                            map.put(term, v.getNextName());
                        }

                        terms.add(new Constant(map.get(term)));
                    }
                    index++;
                }
            }

            l = new DataLogLiteral(con.getHead(), terms, con.isNegative());
            l.setFailed(con.hasFailed());

            body.add(l);
        }

        terms = new ArrayList<>();
        for (Term term : example.getTerms()) {
            terms.add(new Constant(map.get(term)));
        }
        s = new DataLogLiteral(example.getHead(), terms, example.isNegative());

        s.setFailed(false);

        Rule r;// = new SafeRule(example, relevants);
        
        //System.out.println(r.toString());
        
        r = new SafeRule(s, body);

        return r;
    }

    /**
     * Checks if the literal can appear on the rule based on its type as
     * specified on template.
     *
     * @param literal the literal.
     * @param termTypes a {@link Map} with the {@link Term} that already
     * appeared on the rule and its types.
     * @return true if its possible, false otherwise.
     */
    private boolean checkForCorrectTypes(ConcreteLiteral literal, Map<Term, Set<Term>> termTypes) {
        Set<Clause> fact = template.getTemplateFactsForPredicate(literal);
        if (fact.isEmpty())
            return true;
        List<Term> templateType;
        List<Term> terms = literal.getTerms();
        Map<Term, Set<Term>> types = new HashMap<>(terms.size());

        for (int i = 0; i < terms.size(); i++) {
            types.put(terms.get(i), new HashSet<Term>());
        }

        for (Clause c : fact) {
            templateType = c.getHead().getTerms();
            if (templateType.size() == terms.size()) {
                for (int i = 0; i < templateType.size(); i++) {
                    types.get(terms.get(i)).add(templateType.get(i));
                }
            }
        }

        boolean result = true;
        for (Term term : terms) {
            if (!termTypes.containsKey(term)) {
                termTypes.put(term, types.get(term));
            } else {
                termTypes.get(term).retainAll(types.get(term));

                if (result && termTypes.get(term).isEmpty()) {
                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * Method to get the correct constant type template for the predicate.
     *
     * @param types the possible type templates.
     * @param lit the literal which should fit the template.
     * @param individualGroups the groups of individuals.
     * @return The {@link List} with the template if it fits, null otherwise.
     */
    private List<TermType> getTermsWithConstants(List<List<TermType>> types, ConcreteLiteral lit, Map<String, Set<? extends Constant>> individualGroups) {
        if (types == null)
            return null;
        List<TermType> answer = null;
        for (List<TermType> list : types) {
            if (checkTypes(list, lit, individualGroups)) {
                return list;
            }
        }

        return answer;
    }

    /**
     * Checks if the given literal fits the given template.
     *
     * @param type the template.
     * @param lit the literal.
     * @param individualGroups the group of individuals.
     * @return true if it fits, false otherwise.
     */
    private boolean checkTypes(List<TermType> type, ConcreteLiteral lit, Map<String, Set<? extends Constant>> individualGroups) {
        Set<? extends Constant> individuals;
        int index = 0;
        String name = lit.getTerms().get(index).getName();
        for (TermType termType : type) {
            individuals = individualGroups.get(termType.getType());
            if (individuals == null || !individuals.contains(new Constant(lit.getTerms().get(index).getName()))) {
                return false;
            }
            index++;
        }

        return true;
    }

    /**
     * Gets a list of relevant literals for the given example.
     *
     * @param example the example.
     * @return a list of relevant literals.
     */
    protected Set<? extends ConcreteLiteral> getRelevants(ConcreteLiteral example) {
        if (this.answerSet == null || this.examples == null)
            return null;

        Set<ConcreteLiteral> bodyLiterals = new LinkedHashSet<>();

        Set<Term> terms = new HashSet<>(example.getTerms());

        for (ConcreteLiteral pred : answerSet) {
            if (pred.isNegative() == example.isNegative() && pred.getHead().equals(example.getHead())
                    && pred.getTerms().size() == terms.size()
                    && terms.containsAll(pred.getTerms())) {

                bodyLiterals.add(pred);
            }
        }

        if (bodyLiterals.isEmpty())
            return null;

        //getTransitivity(relevants);
        int count = transitivityDepth;
        boolean allTransitivity = transitivityDepth == 0;
        SafeRule sf;

        Set<ConcreteLiteral> relevants = new LinkedHashSet<>(bodyLiterals);
        
        int increase, size;
        while (count > 0 || allTransitivity) {
            increase = getTransitivity(relevants);
            if (increase == 0)
                break;

            size = bodyLiterals.size();
            bodyLiterals.addAll(relevants);
            
            if (size == bodyLiterals.size()) 
                break;
            
            sf = new SafeRule(example, relevants);
            relevants.retainAll(sf.getBody());

            count--;
        }

        if (!recursive) {
            return removeRecursion(example, bodyLiterals);
        }

        return bodyLiterals;
    }

    /**
     * Method to filter the relevant literals and removes the ones which have
     * the same predicate as the head.
     *
     * @param head the head.
     * @param relevants the set of relevant literals.
     * @return the filtered set.
     */
    private Set<? extends ConcreteLiteral> removeRecursion(ConcreteLiteral head, Set<ConcreteLiteral> relevants) {
        Iterator<? extends ConcreteLiteral> it = relevants.iterator();

        while (it.hasNext()) {
            if (it.next().getHead().equals(head.getHead())) {
                it.remove();
            }
        }

        return relevants;
    }

    /**
     * Loads the relevant literals by transitivity onto the given relevant
     * collection. The transitivity's deep is based on the
     * {@link #transitivityDepth}.
     *
     * @param relevants the relevant collection
     * @return the number of relevants appended to the given collection.
     */
    private int getTransitivity(Collection<ConcreteLiteral> relevants) {
        List<ConcreteLiteral> append = new ArrayList<>();
        List<Term> t;
        int count = 0;

        for (ConcreteLiteral pred : relevants) {
            for (ConcreteLiteral unc : answerSet) {

                if (pred.isNegative() == unc.isNegative() && !pred.sameAs(unc) && !relevants.contains(unc)) {
                    t = pred.getTerms();

                    for (Term uncTerm : unc.getTerms()) {
                        if (t.contains(uncTerm)) {
                            append.add(unc);
                            break;
                        }
                    }

                }
            }
            count++;
        }
        relevants.addAll(append);

        return append.size();
    }

    /**
     * Pick a example at random.
     *
     * @return the example.
     */
    private ConcreteLiteral pickExampleAtRandom() {
        return uncoveredExamples.get(randomGenerator.nextInt(uncoveredExamples.size()));
    }

    /**
     * Getter for a measure based on the relative number of examples covered by
     * the rule.
     *
     * @return the measure.
     */
    public double getMeasure() {
        return coveredExamples.size() / examples.size();
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
     * Setter for the Expansion Answer Set.
     *
     * @param answerSet the Exapansion Answer Set.
     */
    public void setAnswerSet(List<ConcreteLiteral> answerSet) {
        this.answerSet = answerSet;
    }

    /**
     * Getter for the rules.
     *
     * @return the rules.
     */
    public Set<DataLogRule> getRules() {
        return rules;
    }

    /**
     * Getter for the recursive.
     * <br> This variable defines if the rule can be recursive or not.
     *
     * @return the recursive.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Setter for the recursive.
     * <br> This variable defines if the rule can be recursive or not.
     *
     * @param recursive the recursive.
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

}
