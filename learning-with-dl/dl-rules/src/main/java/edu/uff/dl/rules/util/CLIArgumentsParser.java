/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import edu.uff.dl.rules.cli.DLRulesCLI;
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
 *
 * @author Victor
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

    public CLIArgumentsParser(String[] args) {
        this.args = args;
        parser();
    }

    public void parser() {
        int numberOfDLPFiles = 0;
        Queue<String> queue = new LinkedList<>();

        for (String arg : args) {
            queue.add(arg);
        }

        rule = false;
        ref = false;
        cv = false;

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

        cvDirectory = queue.remove();
        cvPrefix = queue.remove();
        cvNumberOfFolds = Integer.parseInt(queue.remove());
    }

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

    public DLRulesCLI newInstance() throws FileNotFoundException {
        DLRulesCLI dlrcli = new DLRulesCLI(dlpFilepaths, owlFilepath, positiveTrainFilepath, negativeTrainFilepath, outputDirectory, timeout, templateFilepath, cvDirectory, cvPrefix, cvNumberOfFolds);
        dlrcli.setRule(rule);
        dlrcli.setRefinement(ref);
        dlrcli.setCrossValidation(cv);
        dlrcli.init();
        return dlrcli;
    }

}
