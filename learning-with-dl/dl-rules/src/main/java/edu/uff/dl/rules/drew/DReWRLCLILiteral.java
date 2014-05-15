/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.drew;

import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.format.DLProgramStorer;
import org.semanticweb.drew.dlprogram.format.DLProgramStorerImpl;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.drew.ldlpprogram.reasoner.RLProgramKBCompiler;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 *
 * @author Victor
 */
public class DReWRLCLILiteral extends DReWRLCLI {

    private static final Logger LOG = Logger.getLogger(DReWRLCLILiteral.class.getName());

    protected LiteralModelHandler literalModelHandler;
    protected String dlpContent;
    protected DLVInvocation invocation;

    private DReWRLCLILiteral(String[] args) {
        super(args);
        literalModelHandler = new LiteralModelHandler();
    }

    public static DReWRLCLILiteral run(String... args) {
        return run(null, args);
    }

    public static DReWRLCLILiteral run(String dlpContent, String... args) {
        DReWRLCLILiteral result = new DReWRLCLILiteral(args);
        result.setDLPContent(dlpContent);
        result.go();
        return result;
    }
    
    public static DReWRLCLILiteral get(String... args) {
        DReWRLCLILiteral result = new DReWRLCLILiteral(args);
        return result;
    }

    public static void main(String... args) {
        new DReWRLCLILiteral(args).go();
    }

    public LiteralModelHandler getLiteralModelHandler() {
        return literalModelHandler;
    }

    @Override
    @SuppressWarnings({"CallToThreadDumpStack", "null"})
    public void runDLV(DLVInputProgram inputProgram) {
        invocation = DLVWrapper.getInstance().createInvocation(
                dlvPath);

        try {
            long t0 = System.currentTimeMillis();
            invocation.setInputProgram(inputProgram);

            // invocation.setNumberOfModels(1);
            List<String> filters = new ArrayList<>();

            if (cqFile != null) {
                filters.add("ans");
            }
            if (filter != null) {
                String[] ss = filter.split(",");
                Collections.addAll(filters, ss);
            }

            if (filters != null && filters.size() > 0)
                invocation.setFilter(filters, true);

            if (maxInt != -1) {
                invocation.setMaxint(maxInt);
            }

            if (semantics.equals("wf"))
                invocation.addOption("-wf");

            literalModelHandler.setDlvHandlerStartTime(dlvHandlerStartTime);

            invocation.subscribe(literalModelHandler);

            invocation.run();

            invocation.waitUntilExecutionFinishes();

            nModels = literalModelHandler.getnModels();
            dlvHandlerEndTime = literalModelHandler.getDlvHandlerEndTime();

            List<DLVError> dlvErrors = invocation.getErrors();
            if (dlvErrors.size() > 0)
                System.err.println(dlvErrors);

            long t1 = System.currentTimeMillis();

            dlvTotalTime = t1 - t0;

            long dlvHandlerTime = dlvHandlerEndTime - dlvHandlerStartTime;
            if (verbose) {
                System.err.println("#dlv running time = "
                        + (dlvTotalTime - dlvHandlerTime) + "ms");
                System.err.println("#postprocess time = " + dlvHandlerTime
                        + "ms");
            }

        } catch (DLVInvocationException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleDLProgram(OWLOntology ontology, DLVInputProgram inputProgram) {
        try {
            DLProgramKB kb = new DLProgramKB();
            kb.setOntology(ontology);
            DLProgram elprogram = null;

            DLProgramParser parser;

            Reader reader;
            if (dlpContent != null) {
                reader = new StringReader(dlpContent);
            } else {
                reader = new FileReader(dlpFile);
            }
            parser = new DLProgramParser(reader);
            parser.setOntology(ontology);

            elprogram = parser.program();

            kb.setProgram(elprogram);

            DLProgram datalog;

            long t0 = System.currentTimeMillis();

            RLProgramKBCompiler compiler = new RLProgramKBCompiler();
            datalog = compiler.rewrite(kb);

            int j = dlpFile.lastIndexOf('/');
            String dlpTag = dlpFile;
            if (j >= 0) {
                dlpTag = dlpFile.substring(j + 1);
            }

            datalogFile = ontologyFile + "-" + dlpTag + "-rl.dlv";

            double currentMemory = ((double) ((double) (Runtime.getRuntime()
                    .totalMemory() / 1024) / 1024))
                    - ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));

            if (verbose) {
                System.err.println("#current memory = " + currentMemory + "M");
            }

            FileWriter w = new FileWriter(datalogFile);
            DLProgramStorer storer = new DLProgramStorerImpl();
            storer.store(datalog, w);
            w.close();

            long t1 = System.currentTimeMillis();

            rewritingTime = t1 - t0;
            if (verbose) {
                System.err.println("#rewrting time = " + rewritingTime + "ms");
            }

            inputProgram.addFile(datalogFile);

        } catch (IOException | ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public String getDlpContent() {
        return dlpContent;
    }

    public void setDLPContent(String dlpContent) {
        this.dlpContent = dlpContent;
    }
    
    public void killDLV() throws DLVInvocationException {
        if (invocation != null) 
            invocation.killDlv();
    }

}
