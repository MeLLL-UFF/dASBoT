/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import edu.uff.dl.rules.cli.DLRulesCLI;
import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to do the parser thru the command line arguments. This class can also
 * check the parameters and instantiate a {@link DLRulesCLI}.
 *
 * @author Victor Guimarães
 */
public class CLIArgumentsParser {

    private String[] args;

    public Set<String> dlpFilepaths;
    public String owlFilepath;
    public String positiveTrainFilepath;
    public String negativeTrainFilepath;
    public String outputDirectory;
    public int timeout;
    public String templateFilepath;
    public String cvDirectory;
    public String cvPrefix;
    public int cvNumberOfFolds;

    public boolean rule = false;
    public boolean ref = false;
    public boolean cv = false;
    public boolean noRec = false;

    public int depth;
    public double threshold;

    /**
     * The constructor with the command line arguments.
     *
     * @param args the arguments.
     */
    public CLIArgumentsParser(String[] args) {
        this.args = args;
        parser();
    }

    /**
     * Method to parse the arguments and load the values into variables.
     */
    public void parser() {
        String template = null;
        int numberOfDLPFiles = 0;
        Queue<String> queue = new LinkedList<>();

        for (String arg : args) {
            queue.add(arg);
        }

        while (queue.peek().startsWith("-")) {
            switch (queue.peek().toLowerCase()) {
                case "-rule":
                    rule = true;
                    break;
                case "-ref":
                    ref = true;
                    break;
                case "-cv":
                    cv = true;
                    break;
                case "-norec":
                    noRec = true;
                    break;
            }
            queue.remove();
        }

        try {
            numberOfDLPFiles = Integer.parseInt(queue.peek());
        } catch (NumberFormatException ex) {

        }

        if (numberOfDLPFiles != 0) {
            queue.remove();
        } else {
            numberOfDLPFiles++;
        }

        dlpFilepaths = new LinkedHashSet<>(numberOfDLPFiles);
        for (int i = 0; i < numberOfDLPFiles; i++) {
            dlpFilepaths.add(queue.remove());
        }

        owlFilepath = queue.remove();
        String positeveTrain = queue.remove();
        String negativeTrain = queue.remove();

        if (queue.peek().toLowerCase().equals("-tp")) {
            queue.remove();
            template = queue.remove();
        }

        outputDirectory = queue.remove();
        if (!outputDirectory.endsWith("/")) {
            outputDirectory += "/";
        }

        timeout = Integer.parseInt(queue.remove());

        cvDirectory = null;
        cvPrefix = null;
        cvNumberOfFolds = 0;

        if (cv) {
            cvDirectory = queue.remove();
            cvPrefix = queue.remove();
            cvNumberOfFolds = Integer.parseInt(queue.remove());
        }

        depth = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : 0);
        threshold = (!queue.isEmpty() ? Double.parseDouble(queue.remove()) : 0.0);
    }

    /**
     * Method to check the parameters.
     *
     * @return true if it is ok, false otherwise.
     * @throws ParseException in case a content do not accord with the language.
     * @throws FileNotFoundException in case a file not be found.
     */
    public boolean checkParameters() throws ParseException, FileNotFoundException {
        for (String filePaths : dlpFilepaths) {
            FileContent.getProgramStatements(FileContent.getStringFromFile(filePaths));
        }
        FileContent.getProgramStatements(FileContent.getStringFromFile(positiveTrainFilepath));
        FileContent.getProgramStatements(FileContent.getStringFromFile(negativeTrainFilepath));
        //FileContent.getProgramStatements(FileContent.getStringFromFile(templateFilepath));

        for (int i = 1; i < cvNumberOfFolds + 1; i++) {
            FileContent.getProgramStatements(FileContent.getStringFromFile(cvDirectory + cvPrefix + i + ".f"));
            FileContent.getProgramStatements(FileContent.getStringFromFile(cvDirectory + cvPrefix + i + ".n"));
        }

        return true;
    }

    /**
     * Generates a new instance of a {@link DLRulesCLI} with the passed
     * arguments.
     *
     * @return a new instance of a {@link DLRulesCLI}.
     * @throws FileNotFoundException in case a file not be found.
     */
    public DLRulesCLI newInstance() throws FileNotFoundException {
        DLRulesCLI dlrcli = new DLRulesCLI(dlpFilepaths, owlFilepath, positiveTrainFilepath, negativeTrainFilepath, outputDirectory, timeout, templateFilepath, cvDirectory, cvPrefix, cvNumberOfFolds);
        dlrcli.setRule(rule);
        dlrcli.setRefinement(ref);
        dlrcli.setCrossValidation(cv);
        dlrcli.setRecursiveRuleAllowed(!noRec);
        //dlrcli.init();
        return dlrcli;
    }

}
