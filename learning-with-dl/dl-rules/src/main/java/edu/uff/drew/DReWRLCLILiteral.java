/*
 * UFF Project Semantic Learning
 */
package edu.uff.drew;

import edu.uff.test.DReWRLCLI;
import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor
 */
public class DReWRLCLILiteral extends DReWRLCLI {

    private LiteralModelHandler literalModelHandler;

    private DReWRLCLILiteral(String[] args) {
        super(args);
        literalModelHandler = new LiteralModelHandler();
    }

    public static DReWRLCLILiteral run(String... args) {
        DReWRLCLILiteral result = new DReWRLCLILiteral(args);
        result.go();
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
        DLVInvocation invocation = DLVWrapper.getInstance().createInvocation(
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

        } catch (DLVInvocationException | IOException e) {
            e.printStackTrace();
        }
    }
}
