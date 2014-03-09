/*
 * UFF Project Semantic Learning
 */
package edu.uff.drew;

import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.Model;
import it.unical.mat.wrapper.ModelHandler;
import it.unical.mat.wrapper.ModelResult;
import it.unical.mat.wrapper.Predicate;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.drew.ldlpprogram.reasoner.LDLPProgramQueryResultDecompiler;

/**
 *
 * @author Victor
 */
public class LiteralModelHandler implements ModelHandler {

    private String type;
    private List<Set<Literal>> answerSets;
    private long dlvHandlerStartTime = 0;
    private long dlvHandlerEndTime = 0;
    private int nModels = 0;

    public LiteralModelHandler() {
        this.type = "rl";
        answerSets = new ArrayList<>();
    }

    public void setTypeEL() {
        type = "el";
    }

    public void setTypeRL() {
        type = "rl";
    }

    public String getType() {
        return type;
    }

    public List<Set<Literal>> getAnswerSets() {
        return answerSets;
    }

    public long getDlvHandlerStartTime() {
        return dlvHandlerStartTime;
    }

    public long getDlvHandlerEndTime() {
        return dlvHandlerEndTime;
    }

    public int getnModels() {
        return nModels;
    }

    public void setDlvHandlerStartTime(long dlvHandlerStartTime) {
        this.dlvHandlerStartTime = dlvHandlerStartTime;
    }

    @Override
    public void handleResult(DLVInvocation dlvi, ModelResult mr) {
        switch (type) {
            case "rl":
                handleResultRL(dlvi, mr);
                break;
            case "el":
                handleResultEL(dlvi, mr);
                break;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public void handleResultRL(DLVInvocation paramDLVInvocation, ModelResult modelResult) {
        if (dlvHandlerStartTime == 0)
            dlvHandlerStartTime = System.currentTimeMillis();

        nModels++;

        // System.out.println(nModels);
        Model model = (Model) modelResult;
        // ATTENTION !!! this is necessary and stupid, should we
        // report a bug to DLVWrapper?
        Set<Literal> literals = new HashSet<>();
        model.beforeFirst();
        while (model.hasMorePredicates()) {

            Predicate predicate = model.nextPredicate();
            while (predicate.hasMoreLiterals()) {

                it.unical.mat.dlv.program.Literal literal = predicate.nextLiteral();

                /**
                 * Instead of using a dedicated parser, we can actually access
                 * the literal directly by: <code>
                 * predicate.name();
                 * literal.attributes();
                 * </code>
                 */
                DLProgramParser parser = new DLProgramParser(
                        new StringReader(literal.toString()));

                LDLPProgramQueryResultDecompiler decompiler = new LDLPProgramQueryResultDecompiler();

                try {
                    org.semanticweb.drew.dlprogram.model.Literal decompileLiteral = decompiler
                            .decompileLiteral(parser.literal());
                    literals.add(decompileLiteral);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }
        
        answerSets.add(literals);
        dlvHandlerEndTime = System.currentTimeMillis();
    }

    public void handleResultEL(DLVInvocation paramDLVInvocation, ModelResult modelResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
