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

package br.uff.dl.rules.rules.theory;

import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.rules.evaluation.MeasurableRule;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created on 19/07/16.
 *
 * @author Victor Guimarães
 */
public class SimpleTheoryBuilder extends TheoryBuilder {

    public SimpleTheoryBuilder(String dlpContent,
                               String positiveTrainFilePath,
                               String negativeTrainFilePath,
                               RuleMeasurer measurer,
                               String[] args,
                               List<MeasurableRule> evaluatedRuleExamples) throws FileNotFoundException,
            ParseException {
        super(dlpContent, positiveTrainFilePath, negativeTrainFilePath, measurer, args);
        this.evaluatedRuleExamples = evaluatedRuleExamples;
    }

    public SimpleTheoryBuilder(String dlpContent,
                               Set<? extends Literal> positiveTrainExamples,
                               Set<? extends Literal> negativeTrainExamples,
                               RuleMeasurer measurer,
                               String[] args,
                               List<MeasurableRule> evaluatedRuleExamples) {
        super(dlpContent, positiveTrainExamples, negativeTrainExamples, measurer, args);
        this.evaluatedRuleExamples = evaluatedRuleExamples;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void loadRules() throws IOException {

    }

    @Override
    protected void candidateRuleToTheory(MeasurableRule candidate) {

    }

    @Override
    protected void ruleAccepted(Set<Literal> theoryProvedLiterals) {

    }

}
