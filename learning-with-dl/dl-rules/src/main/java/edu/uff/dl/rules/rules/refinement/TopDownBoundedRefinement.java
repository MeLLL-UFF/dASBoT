/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.refinement;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.rules.SafeRule;
import edu.uff.dl.rules.rules.evaluation.RuleEvaluator;
import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.exception.TimeoutException;
import edu.uff.dl.rules.util.answerpool.AnswerPool;
import edu.uff.dl.rules.util.answerpool.RuleSizeComparator;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 * Class to do the refinement according with the top-down, it means, from the
 * most generic to the most specific, strategy and using a bound rule.
 *
 * <br> This class gets a rule as bound and try to create a new rule with the
 * same head and the body restrained or equal the bound rule. It starts with the
 * head and a minimal set of literals on the body to make the rule safe. Then
 * adds literals in the body, one by one until there are no more literals which
 * fit in the body or reaches the bound rule.
 *
 * @author Victor Guimarães
 */
public class TopDownBoundedRefinement extends Refinement {

    /**
     * Constructor with all needed literals.
     *
     * @param args the DReW's arguments.
     * @param dlpContent the DLP's content.
     * @param genericRule a geniric rule to refine.
     * @param threshold a threshold improviment.
     * @param positiveSamples a set of positive examples.
     * @param negativeSamples a set of negative examples.
     * @param timeout a timeout to infer each rule.
     * @param ruleMeasure a measurer of rules.
     */
    public TopDownBoundedRefinement(String[] args, String dlpContent, EvaluatedRule genericRule, double threshold, Set<Literal> positiveSamples, Set<Literal> negativeSamples, int timeout, RuleMeasurer ruleMeasure, PrintStream outStream) {
        super(args, dlpContent, genericRule, threshold, positiveSamples, negativeSamples, timeout, ruleMeasure, outStream);
    }

    @Override
    public void refine() {
        int limit = super.boundRule.getRule().getBody().size();
        ConcreteLiteral head = boundRule.getRule().getHead();
        Set<? extends ConcreteLiteral> body = new LinkedHashSet<>(boundRule.getRule().getBody());
        Set<? extends ConcreteLiteral> candidates = getCandidates(head, body);

        Comparator com = new EvaluatedRuleComparator();
        //Trocar as duas linhas a seguir pela regra safe mínima e count = tamanho do corpo da regra
        //refineRule(new Rule(head, null), candidates, count, com);
        int count = 0;
        
        if (SafeRule.isImpossibleSafe(boundRule.getRule())) {
            return;
        }
        
        try {
            count = loadBestMinimalRule(head, candidates, com);
        } catch (TimeoutException ex) {
            //Logger.getLogger(TopDownBoundedRefinement.class.getName()).log(Level.SEVERE, null, ex);
        }
        EvaluatedRule previousRule = refinedRules.get(count);
        EvaluatedRule currentRule;
        if (previousRule == null)
            return;

        double measureIncrease = 0;

        do {
            body.removeAll(previousRule.getRule().getBody());
            //candidates = getCandidates(head, body);

            outStream.println("Refinning rule: " + previousRule.getRule());
            outStream.println("Rule: " + count + " of: " + limit);
            count++;
            currentRule = refineRule(previousRule.getRule(), getCandidates(previousRule.getRule(), body), count, com);

            if (currentRule != null) {
                measureIncrease = currentRule.getMeasure() - previousRule.getMeasure();
                previousRule = currentRule;
            } else {
                return;
            }

            outStream.println("Measure Increase: " + measureIncrease);
            outStream.println("");
            //} while (count < limit);
        } while (measureIncrease >= threshold && count < limit);
        if (measureIncrease <= 0) {
            refinedRules.remove(count);
        }
    }

    /**
     * Creates all the minimal rules and pick the best to be refined. Put the
     * best rule into a {@link Map} and return its key.
     *
     * @param head the rule's head.
     * @param candidates the candidate literals.
     * @param com a comparator to decides which rule is the best.
     * @return the rule's key.
     * @throws TimeoutException in case something goes wrong on the thread.
     */
    private int loadBestMinimalRule(ConcreteLiteral head, final Set<? extends ConcreteLiteral> candidates, Comparator<EvaluatedRule> com) throws TimeoutException {
        AnswerPool<Rule> pool = loadMinimalRule(head, candidates);
        EvaluatedRule er;
        //SortedSet<EvaluatedRule> rules = new TreeSet<>(com);
        List<EvaluatedRule> rules = new ArrayList<>();
//        int count = 0, error = 0;
        for (Rule rule : pool.getAnswerPool()) {
            RuleEvaluator re = new RuleEvaluator(rule, args, dlpContent, positiveSamples, negativeSamples);
            er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
            if (er == null) {
//                error++;
                outStream.println(rule);
            } else {
                er.setRuleMeasureFunction(ruleMeasure);
                rules.add(er);
            }

//            count++;
        }
        Collections.sort(rules, com);
        er = rules.get(0);
        int size = er.getRule().getBody().size();
//        double measure1 = er.getMeasure();
//
//        double measure2 = rules.last().getMeasure();

        refinedRules.put(size, er);

        return size;
    }

    /**
     * Loads all the possibilities of minimal rules.
     *
     * @param head the rule's head.
     * @param candidates the candidate literals.
     * @return a pool with all the minimal rules.
     */
    public static AnswerPool<Rule> loadMinimalRule(ConcreteLiteral head, final Set<? extends ConcreteLiteral> candidates) {
        SafeRule r;
        AnswerPool<Rule> pool = new AnswerPool<>(new RuleSizeComparator());

        Set<? extends ConcreteLiteral> body;
        Set<? extends ConcreteLiteral> newCandidates;
        for (ConcreteLiteral lit : candidates) {
            r = new SafeRule(head, lit);
            if (isRuleSafe(r)) {
                pool.addAnswer(r);
            } else {
                body = new LinkedHashSet<>(r.getBody());
                newCandidates = new LinkedHashSet<>(candidates);
                newCandidates.removeAll(body);
                loadMinimalRule(head, newCandidates, body, pool);
            }
        }

        return pool;
    }

    /**
     * Loads all the possibilities of minimal rules. This is the recursive part,
     * that loads the minimal rules by appending a new literal to its body until
     * it becomes safe.
     *
     * @param head the rule's head.
     * @param candidates the candidate literals.
     * @param body the rule's body.
     * @param pool the pool to insert the rule.
     */
    public static void loadMinimalRule(ConcreteLiteral head, Set<? extends ConcreteLiteral> candidates, final Set<? extends ConcreteLiteral> body, AnswerPool<Rule> pool) {
        SafeRule r;
        Set<ConcreteLiteral> newBody;
        Set<ConcreteLiteral> newCandidates;
        for (ConcreteLiteral candidate : candidates) {
            newBody = new LinkedHashSet<>(body);
            newBody.add(candidate);
            r = new SafeRule(head, newBody);
            if (isRuleSafe(r)) {
                pool.addAnswerIfNotWorse(r);
            } else if (pool.isBetter(r)) {
                newCandidates = new LinkedHashSet<>(candidates);
                newCandidates.removeAll(newBody);
                loadMinimalRule(head, newCandidates, newBody, pool);
            }
        }
    }

    /**
     * Checks if the rule is safe.
     *
     * @param rule the rule.
     * @return true if it is, false otherwise.
     */
    public static boolean isRuleSafe(Rule rule) {
        Set<Term> terms = getAllTermsFromRule(rule);
        Set<Term> safe = new HashSet<>();
        for (ConcreteLiteral bodyLit : rule.getBody()) {
            if (!bodyLit.hasFailed()) {
                safe.addAll(bodyLit.getTerms());
            }
        }

        return safe.containsAll(terms);
    }

    /**
     * Refines the rule. This part of the refinement consists in add a new
     * literal from the candidates to the rule's body and see which literal gets
     * the best result.
     *
     * @param rule the initial rule.
     * @param candidates the candidates.
     * @param count the rule's body size.
     * @param com the comparator.
     * @return a new evaluated rule.
     */
    private EvaluatedRule refineRule(final Rule rule, Set<? extends ConcreteLiteral> candidates, int count, Comparator com) {
        EvaluatedRule er;
        Rule r;
        SortedSet<EvaluatedRule> evaluatedRules = new TreeSet<>(com);
        Set<ConcreteLiteral> body;// = new LinkedHashSet<>(r.getBody());
        for (ConcreteLiteral candidate : candidates) {
            body = new LinkedHashSet<>();
            if (rule.getBody() != null) {
                body.addAll(rule.getBody());
            }
            body.add(candidate);
            r = new SafeRule(rule.getHead(), body);
            RuleEvaluator re = new RuleEvaluator(r, args, dlpContent, positiveSamples, negativeSamples);
            try {
                er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
                if (er == null)
                    continue;
                er.setRuleMeasureFunction(ruleMeasure);
                evaluatedRules.add(er);
            } catch (TimeoutException ex) {
                outStream.println("Timeout: " + r.toString());
            }
        }

        if (evaluatedRules.isEmpty())
            return null;
        EvaluatedRule best = evaluatedRules.first();
        double bestMeasure = best.getMeasure();

        EvaluatedRule worst = evaluatedRules.last();
        double worstMeasure = worst.getMeasure();

        refinedRules.put(count, best);

        return best;
    }

    /**
     * Method to get the true candidates for the rule's body.
     *
     * @param head the rule's head.
     * @param candidates the general candidates.
     * @return a set of candidates.
     */
    public static Set<? extends ConcreteLiteral> getCandidates(ConcreteLiteral head, Set<? extends ConcreteLiteral> candidates) {
        return getCandidates(new Rule(head, null), candidates);
    }

    /**
     * Method to get the true candidates for the rule's body.
     *
     * @param head the rule's head.
     * @param candidates the general candidates.
     * @return a set of candidates.
     */
    public static Set<? extends ConcreteLiteral> getCandidates(Rule r, Set<? extends ConcreteLiteral> candidates) {
        Set<ConcreteLiteral> answer = new LinkedHashSet<>();
        Set<Term> allTerms = getAllTermsFromRule(r);
        List<Term> candidateTerms;
        boolean cont;
        for (ConcreteLiteral candidate : candidates) {
            candidateTerms = candidate.getTerms();

            //Condition 1
            if (candidate.hasFailed()) {
                if (!isSafe(candidate, r))
                    continue;
            } else {
                //Condition 2    
                if (!existAnyTerms(allTerms, candidateTerms))
                    continue;
            }

            //Condition 3
            cont = false;
            if (r.getBody() != null && !r.getBody().isEmpty()) {
                for (ConcreteLiteral lit : r.getBody()) {
                    if (isEquivalent(lit, candidate, r)) {
                        cont = true;
                        break;
                    }
                }
                if (cont)
                    continue;
            }

            answer.add(candidate);
        }
        return answer;
    }

    /**
     * Method to check if will be safe add the given literal into the given
     * rule's body.
     *
     * @param lit the literal.
     * @param r the rule.
     * @return true if it is, false otherwise.
     */
    public static boolean isSafe(ConcreteLiteral lit, Rule r) {
        return (existAllTerms(r.getBody(), lit.getTerms()));
    }

    /**
     * Checks if each literal from the body is present on the set of literals.
     *
     * @param body the body.
     * @param literals the set of literals.
     * @return true if it is, false otherwise.
     */
    public static boolean existAllTerms(Set<? extends ConcreteLiteral> body, Collection<Term> literals) {
        if (literals == null || literals.isEmpty())
            return true;
        if (body == null || body.isEmpty())
            return false;
        Set<Term> terms = new HashSet<>();
        for (ConcreteLiteral lit : body) {
            terms.addAll(lit.getTerms());
        }
        return existAllTerms(terms, literals);
    }

    /**
     * Checks if each literal from the part is present on the all.
     *
     * @param all the all.
     * @param part the part.
     * @return true if it is, false otherwise.
     */
    public static boolean existAllTerms(Collection<Term> all, Term... part) {
        if (part == null || part.length == 0)
            return true;
        if (all == null || all.isEmpty())
            return false;
        for (Term term : part) {
            if (!all.contains(term)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if each literal from the part is present on the all.
     *
     * @param all the all.
     * @param part the part.
     * @return true if it is, false otherwise.
     */
    public static boolean existAllTerms(Collection<Term> all, Collection<Term> part) {
        if (part == null || part.isEmpty())
            return true;
        if (all == null || all.isEmpty())
            return false;
        for (Term term : part) {
            if (!all.contains(term)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any literal from the part is present on the all.
     *
     * @param all the all.
     * @param part the part.
     * @return true if it is, false otherwise.
     */
    public static boolean existAnyTerms(Collection<Term> all, Term... part) {
        if (all == null || all.isEmpty() || part == null || part.length == 0)
            return false;
        for (Term term : part) {
            if (all.contains(term)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any literal from the part is present on the all.
     *
     * @param all the all.
     * @param part the part.
     * @return true if it is, false otherwise.
     */
    public static boolean existAnyTerms(Collection<Term> all, Collection<Term> part) {
        if (all == null || all.isEmpty() || part == null || part.isEmpty())
            return false;
        for (Term term : part) {
            if (all.contains(term)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check if a given literal is equivalent to another into the
     * given rule.
     *
     * @param a the literal a.
     * @param b the literal b.
     * @param r the rule.
     * @return true if it is, false otherwise.
     */
    public static boolean isEquivalent(final ConcreteLiteral a, final ConcreteLiteral b, Rule r) {
        List<Term> termsA = new ArrayList<>(a.getTerms());
        List<Term> termsB = new ArrayList<>(b.getTerms());
        if (termsA.size() != termsB.size() || !existAnyTerms(termsA, termsB))
            return false;

        List<String> stringA = new ArrayList();
        List<String> stringB = new ArrayList();

        for (Term ta : termsA) {
            stringA.add(ta.getName());
        }

        for (Term tb : termsB) {
            stringB.add(tb.getName());
        }

        int[] aux = new int[termsA.size()];
        for (int i = 0; i < stringA.size(); i++) {
            if (stringA.get(i).equals(stringB.get(i))) {
                aux[i] = 1;
            } else {
                aux[i] = 0;
            }
        }

        Set<Term> all = getAllTermsFromRule(r);
        for (int i = 0; i < aux.length && aux[i] == 0; i++) {
            if (existAnyTerms(all, termsA.get(i), termsB.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Getter for all the terms from a rule. A Term is any constant or variable
     * that appear on the rule, including its head.
     *
     * @param r the rule.
     * @return a set with all the rule's terms.
     */
    public static Set<Term> getAllTermsFromRule(Rule r) {
        Set<Term> answer = new HashSet<>();
        answer.addAll(r.getHead().getTerms());

        if (r.getBody() == null || r.getBody().isEmpty())
            return answer;

        for (ConcreteLiteral literal : r.getBody()) {
            answer.addAll(literal.getTerms());
        }

        return answer;
    }

}
