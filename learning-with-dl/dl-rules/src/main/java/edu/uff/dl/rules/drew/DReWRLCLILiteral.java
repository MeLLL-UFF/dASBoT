/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.drew;

import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import java.io.BufferedWriter;
import java.io.File;
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
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.format.DLProgramStorer;
import org.semanticweb.drew.dlprogram.format.DLProgramStorerImpl;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.drew.ldlpprogram.reasoner.RLProgramKBCompiler;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Class to call DReW by command line interface. <BR>This class can be used just
 * as the DReW would be.<br>This class has an extra output that is used to catch
 * the DReW's results as Java classes and re-use it.
 *
 * @author Victor Guimarães
 */
public class DReWRLCLILiteral extends DReWRLCLI {

    private static final Logger LOG = Logger.getLogger(DReWRLCLILiteral.class.getName());

    protected LiteralModelHandler literalModelHandler;
    protected String dlpContent;
    protected DLVInvocation invocation;

    /**
     * Constructor with the arguments to initialize the DReW.
     *
     * @param args the command line arguments as array.
     */
    private DReWRLCLILiteral(String[] args) {
        super(args);
        literalModelHandler = new LiteralModelHandler();
    }

    /**
     * Static function to run the DReW.
     *
     * @param args the command line arguments as an arbitrary number of
     * arguments.
     * @return this class loaded with the results of the running.
     */
    public static DReWRLCLILiteral run(String... args) {
        return run(null, args);
    }

    /**
     * Static function to run the DReW. <br>
     * Instead of recieve a DLP file path, receives a string with the DLP's
     * content.<br>
     * Useful when you need run the DReW more than once and already have the
     * content in a variable, or need to manipulate the content by reading from
     * more than one file, adding rules, etc...
     *
     * @param dlpContent the wished dlp content to run.
     * @param args the command line arguments as an arbitrary number of
     * arguments.
     * @return this class loaded with the results of the running.
     */
    public static DReWRLCLILiteral run(String dlpContent, String... args) {
        DReWRLCLILiteral result = new DReWRLCLILiteral(args);
        result.setDLPContent(dlpContent);
        result.go();
        return result;
    }

    /**
     * A static function to get a instance of this class load with the specified
     * arguments.
     *
     * @param args the arguments.
     * @return an instance of this class.
     */
    public synchronized static DReWRLCLILiteral get(String... args) {
        DReWRLCLILiteral result = new DReWRLCLILiteral(args);
        return result;
    }

    /**
     * A main method to execute the program directly from this class. Call the
     * constructor with the specified arguments.
     *
     * @param args the command line arguments to pass thru the constructor.
     */
    public static void main(String... args) {
        new DReWRLCLILiteral(args).go();
    }

    /**
     * Getter for {@link LiteralModelHandler}, a class that contains the DReW's
     * results.
     *
     * @return a {@link LiteralModelHandler} with the DReW's results.
     */
    public LiteralModelHandler getLiteralModelHandler() {
        return literalModelHandler;
    }

    @Override
    public synchronized void runDLV(DLVInputProgram inputProgram) {
        invocation = DLVWrapper.getInstance().createInvocation(dlvPath);

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

            if (filters.size() > 0) {
                invocation.setFilter(filters, true);
            }

            if (maxInt != -1) {
                invocation.setMaxint(maxInt);
            }

            if (semantics.equals("wf")) {
                invocation.addOption("-wf");
            }

            literalModelHandler.setDLVHandlerStartTime(dlvHandlerStartTime);

            invocation.subscribe(literalModelHandler);

            invocation.run();

            invocation.waitUntilExecutionFinishes();

            nModels = literalModelHandler.getNModels();
            dlvHandlerEndTime = literalModelHandler.getDlvHandlerEndTime();

            List<DLVError> dlvErrors = invocation.getErrors();
            if (dlvErrors.size() > 0) {
                System.err.println(dlvErrors);
            }

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
//            System.out.println("Error Here!");
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void handleDLProgram(OWLOntology ontology, DLVInputProgram inputProgram) {
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

            FileWriter writer = new FileWriter(datalogFile);
            BufferedWriter w = new BufferedWriter(writer);
            DLProgramStorer storer = new DLProgramStorerImpl();
            storer.store(datalog, w);
            w.flush();
            w.close();

            long t1 = System.currentTimeMillis();

            rewritingTime = t1 - t0;
            if (verbose) {
                System.err.println("#rewrting time = " + rewritingTime + "ms");
            }

            inputProgram.addFile(datalogFile);

        } catch (IOException | ParseException ex) {
//            System.out.println("Error Here 4");
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Getter for the DLP's content.
     *
     * @return the DLP's content.
     */
    public String getDLPContent() {
        return dlpContent;
    }

    /**
     * Setter for the DLP's content.
     *
     * @param dlpContent the DLP's content.
     */
    public void setDLPContent(String dlpContent) {
        this.dlpContent = dlpContent;
    }

    /**
     * Method to kill the DLV's execution. <br>
     * Usiful when the program takes to long to be executed and exceeds the
     * problem's timeout.
     *
     * @throws DLVInvocationException a possible exception during the DLV
     * invocation.
     */
    public void killDLV() throws DLVInvocationException {
        if (invocation != null) {
            invocation.killDlv();
        }
    }

}
