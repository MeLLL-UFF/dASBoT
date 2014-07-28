/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.rules.refinement;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRule;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.Rule;
import edu.uff.dl.rules.rules.SafeRule;
import edu.uff.dl.rules.rules.avaliation.RuleEvaluator;
import edu.uff.dl.rules.rules.avaliation.RuleMeasurer;
import edu.uff.dl.rules.util.TimeoutException;
import edu.uff.dl.rules.util.answerpool.AnswerPool;
import edu.uff.dl.rules.util.answerpool.RuleSizeComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor
 */
public class TopDownBoundedRefinement extends Refinement {

    public TopDownBoundedRefinement(String[] args, String dlpContent, EvaluatedRule genericRule, double threshold, Set<Literal> positiveSamples, Set<Literal> negativeSamples, int timeout, RuleMeasurer ruleMeasure) {
        super(args, dlpContent, genericRule, threshold, positiveSamples, negativeSamples, timeout, ruleMeasure);
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
        try {
            count = loadBestMinimalRule(head, candidates, com);
        } catch (TimeoutException ex) {
            Logger.getLogger(TopDownBoundedRefinement.class.getName()).log(Level.SEVERE, null, ex);
        }
        EvaluatedRule previousRule = refinedRules.get(count);
        EvaluatedRule currentRule;
        if (previousRule == null)
            return;

        double measureIncrease = 0;

        do {
            body.removeAll(previousRule.getRule().getBody());
            //candidates = getCandidates(head, body);

            System.out.println("Refinning rule: " + previousRule.getRule());
            System.out.println("Rule: " + count + " of: " + limit);
            count++;
            currentRule = refineRule(previousRule.getRule(), getCandidates(previousRule.getRule(), body), count, com);

            if (currentRule != null) {
                measureIncrease = currentRule.getMeasure() - previousRule.getMeasure();
                previousRule = currentRule;
            } else {
                return;
            }
            
            System.out.println("Measure Increase: " + measureIncrease);
            System.out.println("");
        //} while (count < limit);
        } while (Math.abs(measureIncrease) > threshold && count < limit);
    }

    private int loadBestMinimalRule(ConcreteLiteral head, final Set<? extends ConcreteLiteral> candidates, Comparator<EvaluatedRule> com) throws TimeoutException {
        AnswerPool<Rule> pool = loadMinimalRule(head, candidates);
        EvaluatedRule er;
        SortedSet<EvaluatedRule> rules = new TreeSet<>(com);
        int count = 0, error = 0;
        for (Rule rule : pool.getAnswerPool()) {
            RuleEvaluator re = new RuleEvaluator(rule, args, dlpContent, positiveSamples, negativeSamples);
            er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
            if (er == null) {
                error++;
                System.out.println(rule);
            } else {
                er.setRuleMeasureFunction(ruleMeasure);
                rules.add(er);
            }

            count++;
        }
        er = rules.first();
        int size = er.getRule().getBody().size();
        double measure1 = er.getMeasure();

        double measure2 = rules.last().getMeasure();

        refinedRules.put(size, er);

        return size;
    }

    private AnswerPool<Rule> loadMinimalRule(ConcreteLiteral head, final Set<? extends ConcreteLiteral> candidates) {
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

    private void loadMinimalRule(ConcreteLiteral head, Set<? extends ConcreteLiteral> candidates, final Set<? extends ConcreteLiteral> body, AnswerPool<Rule> pool) {
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

    private boolean isRuleSafe(Rule rule) {
        Set<Term> terms = getAllTermsFromRule(rule);
        Set<Term> safe = new HashSet<>();
        for (ConcreteLiteral bodyLit : rule.getBody()) {
            if (!bodyLit.hasFailed()) {
                safe.addAll(bodyLit.getTerms());
            }
        }

        return safe.containsAll(terms);
    }

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
                System.out.println("Timeout: " + r.toString());
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

    private Set<? extends ConcreteLiteral> getCandidates(ConcreteLiteral head, Set<? extends ConcreteLiteral> candidates) {
        return getCandidates(new Rule(head, null), candidates);
    }

    private Set<? extends ConcreteLiteral> getCandidates(Rule r, Set<? extends ConcreteLiteral> candidates) {
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

    private boolean isSafe(ConcreteLiteral lit, Rule r) {
        return (existAllTerms(r.getBody(), lit.getTerms()));
    }

    private boolean existAllTerms(Set<? extends ConcreteLiteral> body, Collection<Term> literal) {
        if (literal == null || literal.isEmpty())
            return true;
        if (body == null || body.isEmpty())
            return false;
        Set<Term> terms = new HashSet<>();
        for (ConcreteLiteral lit : body) {
            terms.addAll(lit.getTerms());
        }
        return existAllTerms(terms, literal);
    }

    private boolean existAllTerms(Collection<Term> all, Term... part) {
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

    private boolean existAllTerms(Collection<Term> all, Collection<Term> part) {
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

    private boolean existAnyTerms(Collection<Term> all, Term... part) {
        if (all == null || all.isEmpty() || part == null || part.length == 0)
            return false;
        for (Term term : part) {
            if (all.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean existAnyTerms(Collection<Term> all, Collection<Term> part) {
        if (all == null || all.isEmpty() || part == null || part.isEmpty())
            return false;
        for (Term term : part) {
            if (all.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEquivalent(final ConcreteLiteral a, final ConcreteLiteral b, Rule r) {
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

    private Set<Term> getAllTermsFromRule(Rule r) {
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
