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

package br.uff.dl.rules.rules;

/**
 * This class is a container for a {@link Rule} and a double score.
 * Created on 04/07/16.
 *
 * @author Victor Guimarães
 */
public class ScoredRule implements Comparable {

    protected Rule rule;
    protected double score;

    public ScoredRule(Rule rule) {
        this.rule = rule;
        this.score = 0.0;
    }

    public ScoredRule(Rule rule, double score) {
        this.rule = rule;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return rule.toString() + "\t" + score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScoredRule)) {
            return false;
        }

        ScoredRule that = (ScoredRule) o;

        if (Double.compare(that.score, score) != 0) {
            return false;
        }
        return rule != null ? rule.equals(that.rule) : that.rule == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = rule != null ? rule.hashCode() : 0;
        temp = Double.doubleToLongBits(score);
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
        ScoredRule other = (ScoredRule) o;
        return (int) Math.signum(other.score - this.score);
    }

}
