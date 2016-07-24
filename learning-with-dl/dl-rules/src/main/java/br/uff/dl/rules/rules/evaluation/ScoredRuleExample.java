/*
 * Copyright (C) 2016 Victor Guimarães
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.uff.dl.rules.rules.evaluation;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.rules.Rule;

/**
 * This class is a container for a {@link Rule} and a double measure.
 * Created on 04/07/16.
 *
 * @author Victor Guimarães
 */
public class ScoredRuleExample implements Comparable, MeasurableRuleExample {

    protected Rule rule;
    protected double measure;
    protected ConcreteLiteral basedExample;

    public ScoredRuleExample(Rule rule, ConcreteLiteral basedExample, double measure) {
        this.rule = rule;
        this.basedExample = basedExample;
        this.measure = measure;
    }

    public ScoredRuleExample(Rule rule, ConcreteLiteral basedExample) {
        this(rule, basedExample, 0.0);
    }

    public ScoredRuleExample(Rule rule, double measure) {
        this(rule, null, measure);
    }

    public ScoredRuleExample(Rule rule) {
        this(rule, null, 0.0);
    }

    @Override
    public double getMeasure() {
        return measure;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public ConcreteLiteral getExample() {
        return basedExample;
    }

    @Override
    public String toString() {
        return rule.toString() + "\t" + measure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScoredRuleExample)) {
            return false;
        }

        ScoredRuleExample that = (ScoredRuleExample) o;

        if (Double.compare(that.measure, measure) != 0) {
            return false;
        }
        return rule != null ? rule.equals(that.rule) : that.rule == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = rule != null ? rule.hashCode() : 0;
        temp = Double.doubleToLongBits(measure);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     * <p> </b>Note: this class has a natural ordering that is
     * inconsistent with equals. </p>
     */
    @Override
    public int compareTo(Object o) {
        ScoredRuleExample other = (ScoredRuleExample) o;
        return (int) Math.signum(other.measure - this.measure);
    }

}
