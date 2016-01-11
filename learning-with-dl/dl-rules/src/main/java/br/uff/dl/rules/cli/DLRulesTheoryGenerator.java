package br.uff.dl.rules.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.test.ResultSet;
import br.uff.dl.rules.util.FileContent;
import org.apache.commons.io.FileUtils;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Created by Victor Guimarães on 1/11/16.
 */
public class DLRulesTheoryGenerator extends Thread {

    public static final String ENCODE = "UTF8";

    protected String dlRulesOutputDirectory;
    protected File theoryFile;
    protected RuleMeasurer theoryMeasure;
    protected double theoryThreshold;
    protected int sideWayMovements;

    public void parseArgs(String args[]) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        dlRulesOutputDirectory = args[0];
        theoryFile = new File(args[1]);
        theoryMeasure = (RuleMeasurer) Class.forName("br.uff.dl.rules.evaluation." + args[2]).newInstance();
        theoryThreshold = Double.parseDouble(args[3]);
        sideWayMovements = Integer.parseInt(args[4]);
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        DLRulesTheoryGenerator gen = new DLRulesTheoryGenerator();
        gen.parseArgs(args);

        gen.run();
    }

    protected DLRulesTheoryGenerator() {
    }

    public DLRulesTheoryGenerator(String dlRulesOutputDirectory, File theoryFile, RuleMeasurer theoryMeasure, double theoryThreshold, int sideWayMovements) {
        this.dlRulesOutputDirectory = dlRulesOutputDirectory;
        this.theoryFile = theoryFile;
        this.theoryMeasure = theoryMeasure;
        this.theoryThreshold = theoryThreshold;
        this.sideWayMovements = sideWayMovements;
    }

    @Override
    public void run() {
        try {
            String[] args = FileContent.extractArgsFromGlobalStatistics(new File(dlRulesOutputDirectory, "globalStatistics.txt"), ENCODE);
            DLRulesCLI cli = new DLRulesCLI();
            cli.parseArguments(args);

            ResultSet rs = new ResultSet(cli.getDlpContent(), cli.getPositiveTrainFilePath(), cli.getNegativeTrainFilePath(), cli.getOutputDirectory(), theoryMeasure, cli.getDrewArgs(), theoryThreshold, sideWayMovements);

            FileUtils.writeStringToFile(theoryFile, rs.toString(), ENCODE);
        } catch (ParseException | IOException ex) {
            ex.printStackTrace();
        }
    }


}
