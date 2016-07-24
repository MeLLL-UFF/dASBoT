/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.drew;

import it.unical.mat.wrapper.*;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.drew.ldlpprogram.reasoner.LDLPProgramQueryResultDecompiler;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to get the DReW's answer set as a collection of
 * {@link Literal} so it can be used by other classes.
 *
 * @author Victor Guimarães
 */
public class LiteralModelHandler implements ModelHandler {

    private String type;
    private List<Set<Literal>> answerSets;
    private long dlvHandlerStartTime = 0;
    private long dlvHandlerEndTime = 0;
    private int nModels = 0;

    /**
     * Constructor of the class. Initiates the answer's collection and sets the
     * language as RL (default language).
     */
    public LiteralModelHandler() {
        this.type = "rl";
        answerSets = new ArrayList<>();
    }

    /**
     * Set the language as EL.
     */
    public void setTypeEL() {
        type = "el";
    }

    /**
     * Set the language as RL (default).
     */
    public void setTypeRL() {
        type = "rl";
    }

    /**
     * Getter for the language type.
     *
     * @return the language type.
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for the answer sets.
     *
     * @return a list with the answer sets.
     */
    public List<Set<Literal>> getAnswerSets() {
        return answerSets;
    }

    /**
     * Getter for the DLV Start Time.
     * <br>(Control Variable)
     *
     * @return the DLV Start Time.
     */
    public long getDLVHandlerStartTime() {
        return dlvHandlerStartTime;
    }

    /**
     * Getter for the DLV End Time.
     * <br>(Control Variable)
     *
     * @return the DLV End Time.
     */
    public long getDlvHandlerEndTime() {
        return dlvHandlerEndTime;
    }

    /**
     * Getter for the nModels.
     * <br>(Control Variable)
     *
     * @return the nModels.
     */
    public int getNModels() {
        return nModels;
    }

    /**
     * Setter for DLV Start Time.
     * <br>(Control Variable)
     *
     * @param dlvHandlerStartTime the DLV Start Time.
     */
    public void setDLVHandlerStartTime(long dlvHandlerStartTime) {
        this.dlvHandlerStartTime = dlvHandlerStartTime;
    }

    /**
     * Method to handle the DReW's results.
     * <br>Needed by implement the interface method.
     *
     * @param dlvi the DLV invocation.
     * @param mr the result model.
     */
    @Override
    public synchronized void handleResult(DLVInvocation dlvi, ModelResult mr) {
        switch (type) {
            case "rl":
                handleResultRL(dlvi, mr);
                break;
            case "el":
                handleResultEL(dlvi, mr);
                break;
        }
    }

    /**
     * Method to handle the result by the RL language.
     *
     * @param paramDLVInvocation the DLV invocation.
     * @param modelResult the result model.
     */
    public synchronized void handleResultRL(DLVInvocation paramDLVInvocation, ModelResult modelResult) {
        if (dlvHandlerStartTime == 0) {
            dlvHandlerStartTime = System.currentTimeMillis();
        }

        nModels++;

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
//                } catch (ParseException e) {
                } catch (ParseException | IllegalArgumentException e) {
//                    e.printStackTrace();
                }
            }
        }

        answerSets.add(literals);
        dlvHandlerEndTime = System.currentTimeMillis();
    }

    /**
     * Method to handle the result by the EL language.
     * <br>(Not Implemented Yet)
     *
     * @param paramDLVInvocation the DLV invocation.
     * @param modelResult the result model.
     */
    public void handleResultEL(DLVInvocation paramDLVInvocation, ModelResult modelResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * The class's description. A {@link String} which contains all the answer
     * sets generates by DReW's. Same as DReW's output.
     *
     * @return the answer sets results.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Set<Literal> set : answerSets) {
            sb.append("{ ");

            for (Literal l : set) {
                sb.append(l);
                sb.append(", ");
            }
            sb.setCharAt(sb.lastIndexOf(","), '}');

            sb.append("\n");
            sb.append("\n");
        }

        return sb.toString().trim();
    }

}
