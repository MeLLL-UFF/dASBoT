/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import br.uff.dl.rules.cli.DLRulesCLI;
import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to do the parser thru the command line arguments. This class can also
 * check the parameters and instantiate a {@link DLRulesCLI}.
 *
 * @author Victor Guimarães
 * @deprecated 
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
    
    public String cvDirectory = null;
    public String cvPrefix = null;
    public int cvNumberOfFolds = 0;

    public boolean rule = false;
    public boolean ref = false;
    public boolean cv = false;
    public boolean noRec = false;
    public boolean generic = false;

    public int depth;
    public double threshold;
    public int sideWayMoves;
    public double theoryThreshold;

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
        int numberOfDLPFiles = 0;
        Queue<String> queue = new LinkedList<>();

        for (String arg : args) {
            queue.add(arg);
        }

        try {
            String peek;
            while (queue.peek().startsWith("-")) {
                peek = queue.peek().toLowerCase();
                queue.remove();
                switch (peek) {
                    case "-rule":
                        rule = true;
                        break;
                    case "-ref":
                        ref = true;
                        if (queue.peek().toLowerCase().equals("gen")) {
                            generic = true;
                            queue.remove();
                        }
                        break;
                    case "-cv":
                        cv = true;
                        break;
                    case "-norec":
                        noRec = true;
                        break;
                }
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
            positiveTrainFilepath = queue.remove();
            negativeTrainFilepath = queue.remove();

            if (queue.peek().toLowerCase().equals("-tp")) {
                queue.remove();
                templateFilepath = queue.remove();
            }

            outputDirectory = queue.remove();
            if (!outputDirectory.endsWith("/")) {
                outputDirectory += "/";
            }

            timeout = Integer.parseInt(queue.remove());

            if (cv) {
                cvDirectory = queue.remove();
                cvPrefix = queue.remove();
                cvNumberOfFolds = Integer.parseInt(queue.remove());
            }

            depth = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : 0);
            threshold = (!queue.isEmpty() ? Double.parseDouble(queue.remove()) : 0.0);
            
            sideWayMoves = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : -1);
            theoryThreshold = (!queue.isEmpty() ? Double.parseDouble(queue.remove()) : 0.0);
            
        } catch (NoSuchElementException | NumberFormatException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
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
