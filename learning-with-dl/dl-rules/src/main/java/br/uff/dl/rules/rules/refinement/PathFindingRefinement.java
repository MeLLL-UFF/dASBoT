/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.rules.refinement;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.exception.TimeoutException;
import br.uff.dl.rules.rules.Rule;
import br.uff.dl.rules.rules.SafeRule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleAndSizeComparator;
import br.uff.dl.rules.rules.evaluation.RuleEvaluator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Term;

/**
 *
 * @author Victor Guimarães
 */
public class PathFindingRefinement extends Refinement {

//    protected String[] args;
//    protected String dlpContent;
//    protected EvaluatedRule boundRule;
//    protected double threshold;
//    protected Set<Literal> positiveExamples;
//    protected Set<Literal> negativeExamples;
//    protected int timeout;
//    protected RuleMeasurer ruleMeasure;
//    protected PrintStream outStream;
    /**
     * Defines which {@link Term} on the literal will be the edge's end. If less
     * than zero, the last {@link Term} will be picked. If not, will be the
     * {@link Term} of index {@link #edgeEndingTerm}.
     */
    protected int edgeEndingTerm = 1;

    @Override
    public void refine() {
        Rule rule = new SafeRule(boundRule.getRule());
        List<Term> terms = rule.getHead().getTerms();

        Term entry = terms.get(0);
        Term exit = terms.get(edgeEndingTerm < 0 ? terms.size() - 1 : edgeEndingTerm);

        Map<Term, Set<ConcreteLiteral>> graph = generateGraphStructure(rule);

        List<Rule> rules = generateRules(rule.getHead(), graph, entry, exit);

        List<EvaluatedRule> evaluatedRules = new ArrayList<>(rules.size());
        RuleEvaluator re;
        EvaluatedRule evaluatedRule;

        for (Rule r : rules) {
            re = new RuleEvaluator(r, args, dlpContent, positiveExamples, negativeExamples);
            try {
                evaluatedRule = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
                outStream.println(r);
                if (evaluatedRule != null) {
                    evaluatedRule.setRuleMeasureFunction(ruleMeasure);
                    evaluatedRules.add(evaluatedRule);
                    outStream.println("Measure:\t" + evaluatedRule.getMeasure());
                } else {
                    outStream.println("Evaluation Timeout!");
                }
                outStream.println();
            } catch (TimeoutException ex) {
                Logger.getLogger(PathFindingRefinement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (boundRule.getPositives() != 0) {
            boundRule.setRuleMeasureFunction(ruleMeasure);
            evaluatedRules.add(boundRule);
        }

        Collections.sort(evaluatedRules, new EvaluatedRuleAndSizeComparator());

        for (int i = 0; i < evaluatedRules.size(); i++) {
            refinedRules.put(i, evaluatedRules.get(i));
        }
    }

    protected List<Rule> generateRules(ConcreteLiteral head, Map<Term, Set<ConcreteLiteral>> graph, Term entry, Term exit) {
        List<Rule> rules = new ArrayList<>();
        List<Path> paths = initializePaths(graph, entry);
        Path path;

        Path newWay;
        ConcreteLiteral lastLiteral;
        Rule rule;
        for (int i = 0; i < paths.size(); i++) {
            path = paths.get(i);
            lastLiteral = path.getLast();

            for (Term term : lastLiteral.getTerms()) {
                for (ConcreteLiteral literal : graph.get(term)) {
                    if (path.contains(literal) || (literal.hasFailed() && !path.containsAllSafeTerms(literal.getTerms()))) {
                        continue;
                    }

                    newWay = new Path(path);
                    newWay.add(literal);

                    if (literal.getTerms().contains(exit)) {
                        rule = new SafeRule(head, newWay.getLiterals());
                        if (!rule.isEquivalentToAny(rules)) {
                            rules.add(rule);
                        }
                    } else {
                        paths.add(newWay);
                    }
                }
            }

        }

        return rules;
    }

    protected List<Path> initializePaths(Map<Term, Set<ConcreteLiteral>> graph, Term entry) {
        List<Path> paths = new ArrayList<>();

        for (ConcreteLiteral literal : graph.get(entry)) {
            Path path = new Path();
            path.add(literal);
            paths.add(path);
        }

        return paths;
    }

    protected Map<Term, Set<ConcreteLiteral>> generateGraphStructure(Rule rule) {
        Map<Term, Set<ConcreteLiteral>> graph = new HashMap<>();

        for (ConcreteLiteral literal : rule.getBody()) {
            for (Term term : literal.getTerms()) {
                if (!graph.containsKey(term)) {
                    graph.put(term, new HashSet<>());
                }

                graph.get(term).add(literal);
            }
        }

        return graph;
    }

    public int getEdgeEndingTerm() {
        return edgeEndingTerm;
    }

    public void setEdgeEndingTerm(int edgeEndingTerm) {
        this.edgeEndingTerm = edgeEndingTerm;
    }

}
