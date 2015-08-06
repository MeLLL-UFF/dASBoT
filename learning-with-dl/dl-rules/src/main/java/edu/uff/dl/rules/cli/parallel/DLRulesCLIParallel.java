/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.cli.parallel;

import edu.uff.dl.rules.exception.TimeoutException;
import edu.uff.dl.rules.evaluation.CompressionMeasure;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRule;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import edu.uff.dl.rules.evaluation.LaplaceMeasure;
import edu.uff.dl.rules.rules.evaluation.RuleEvaluator;
import edu.uff.dl.rules.evaluation.RuleMeasurer;
import edu.uff.dl.rules.rules.refinement.Refinement;
import edu.uff.dl.rules.rules.refinement.TopDownBoundedRefinement;
import static edu.uff.dl.rules.test.App.redirectOutputStream;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.DReWDefaultArgs;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to call the program by command line interface (Parallel Version). This
 * class can be used to generate rules for each example on the trainer file,
 * refine and evaluate the rules against test fold using cross validation.
 * <br><br>
 * Parallel Version of {@link edu.uff.dl.rules.cli.DLRulesCLI}
 * <br><br>
 * (Not Working Yet)
 *
 * @author Victor Guimarães
 */
public class DLRulesCLIParallel {

    private String owlFilepath;
    private String outputDirectory;

    private int timeout;

    private String outER;
    private String outRefinement;
    private String outRefinementAll;
    private String outCV;
    private String dlpContent;
    private String positiveTrainExample;
    private String negativeTrainExample;
    private String templateContent;
    private String cvDirectory;
    private String cvPrefix;
    private int cvNumberOfFolds;

    private boolean rule;
    private boolean refinement;
    private boolean crossValidation;

    private String[] args = DReWDefaultArgs.ARGS;

    private int numberOfThreads;

    /**
     * Main function, used to start the program.
     *
     * @param args the parameters needed for the program execution.<br>-rule to
     * generate the rules (optional),<br>-ref to refine the rules (optional),<br>-cv
     * to cross validate the rules (optional),<br>an integer number of bk files
     * (omitted = 1),<br>a set of paths for the bk files, according with the number
     * of files previous setted,<br>-tp to use template (file to type the
     * individuos according with its relationships) (optional),<br>if -tp was used,
     * the path of the template file,<br>an output directory for the program's
     * output,<br>a timeout for the rule's inferences,<br>the cross validation
     * directory with the folds,<br>the fold's prefix name,<br>the number of folds,
     * <br>the number of threads that should be created simultaneously (usually, the
     * number of available cores).
     * @throws FileNotFoundException in case of a file path does not exist.
     */
    public static void main(String[] args) throws FileNotFoundException {
        String template = null;
        int numberOfDLPFiles = 0;
        Queue<String> queue = new LinkedList<>();

        for (String arg : args) {
            queue.add(arg);
        }

        boolean rule = false;
        boolean ref = false;
        boolean cv = false;
        try {

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

            Set<String> dlpFilepaths = new LinkedHashSet<>(numberOfDLPFiles);
            for (int i = 0; i < numberOfDLPFiles; i++) {
                dlpFilepaths.add(queue.remove());
            }

            String owlFilepath = queue.remove();
            String positeveTrain = queue.remove();
            String negativeTrain = queue.remove();

            if (queue.peek().toLowerCase().equals("-tp")) {
                queue.remove();
                template = queue.remove();
            }

            String outputDirectory = queue.remove();
            if (!outputDirectory.endsWith("/")) {
                outputDirectory += "/";
            }

            int timeout = Integer.parseInt(queue.remove());

            String cvDirectory;
            String cvPrefix;
            int cvNumberOfFolds;

            cvDirectory = queue.remove();
            cvPrefix = queue.remove();
            cvNumberOfFolds = Integer.parseInt(queue.remove());

            int numberOfThreads = Integer.parseInt(queue.remove());

            DLRulesCLIParallel dlrcli = new DLRulesCLIParallel(dlpFilepaths, owlFilepath, positeveTrain, negativeTrain, outputDirectory, timeout, template, cvDirectory, cvPrefix, cvNumberOfFolds, numberOfThreads);
            dlrcli.setRule(rule);
            dlrcli.setRefinement(ref);
            dlrcli.setCrossValidation(cv);
            dlrcli.init();
        } catch (NoSuchElementException | NumberFormatException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The constructor of the class with the needed parameters.
     *
     * @param dlpFilepaths a set of paths for the bk files.
     * @param owlFilepath a path for an owl file (future use).
     * @param positiveTrainFilepath a set of positive examples.
     * @param negativeTrainFilepath a set of negative examples.
     * @param outputDirectory an output directory for the program's output.
     * @param timeout a timeout for the rule's inferences.
     * @param templateFilepath if -tp was used, the path of the template file.
     * @param cvDirectory the cross validation directory with the folds.
     * @param cvPrefix the fold's prefix name.
     * @param cvNumberOfFolds the number of folds.
     * @param numberOfThreads the number of threads that should be created.
     * simultaneously (usually, the number of available cores).
     * @throws FileNotFoundException in case of any file path does not exist.
     */
    public DLRulesCLIParallel(Set<String> dlpFilepaths, String owlFilepath, String positiveTrainFilepath, String negativeTrainFilepath, String outputDirectory, int timeout, String templateFilepath, String cvDirectory, String cvPrefix, int cvNumberOfFolds, int numberOfThreads) throws FileNotFoundException {
        this.owlFilepath = owlFilepath;
        this.outputDirectory = outputDirectory;
        this.timeout = timeout;

        this.dlpContent = FileContent.getStringFromFile(dlpFilepaths);
        this.outER = outputDirectory + "ER" + "/";
        this.outRefinement = outputDirectory + "refinement/";
        this.outRefinementAll = outRefinement + "all/";
        this.outCV = outputDirectory + "CV" + "/";
        createOutputDirectories();

        this.positiveTrainExample = FileContent.getStringFromFile(positiveTrainFilepath);
        this.negativeTrainExample = FileContent.getStringFromFile(negativeTrainFilepath);

        if (templateFilepath != null && !templateFilepath.isEmpty()) {
            templateContent = FileContent.getStringFromFile(templateFilepath);
        }

        this.cvDirectory = cvDirectory;
        this.cvPrefix = cvPrefix;
        this.cvNumberOfFolds = cvNumberOfFolds;

        args[2] = owlFilepath;

        this.numberOfThreads = numberOfThreads;
    }

    /**
     * Create all the output directories.
     */
    private void createOutputDirectories() {
        File fout;
        fout = new File(outputDirectory);
        if (!fout.exists()) {
            fout.mkdirs();
        }

        fout = new File(outER);
        if (!fout.exists()) {
            fout.mkdirs();
        }

        fout = new File(outRefinement);
        if (!fout.exists()) {
            fout.mkdirs();
        }

        fout = new File(outRefinementAll);
        if (!fout.exists()) {
            fout.mkdirs();
        }

        fout = new File(outCV);
        if (!fout.exists()) {
            fout.mkdirs();
        }
    }

    /**
     * Function used to initiate the process.
     */
    public void init() {
        if (rule)
            generateRuleForEachExample();
        if (refinement)
            refinement();
        if (crossValidation)
            crossValidation();
    }

    /**
     * Handle all the generation part. For each example on the trainer file, try
     * to create a rule. If succeeded, store the rule on the ER directory on the
     * serialized version of the <code>{@link EvaluatedRuleExample}</code>
     */
    private void generateRuleForEachExample() {
        try {
            int size = FileContent.getExamplesLiterals(positiveTrainExample).size();

            RuleParallel[] rps = new RuleParallel[numberOfThreads];

            Queue<Integer> examples[] = new ConcurrentLinkedQueue[numberOfThreads];

            for (int i = 0; i < examples.length; i++) {
                examples[i] = new ConcurrentLinkedQueue<>();
            }

            for (int i = 0; i < size; i++) {
                examples[i % numberOfThreads].add(i);
            }
            String name;
            for (int i = 0; i < rps.length; i++) {
                name = "RuleParalle-Thread-" + i;
                rps[i] = new RuleParallel(name, owlFilepath, dlpContent, positiveTrainExample, negativeTrainExample, templateContent, outER, timeout, examples[i]);
                rps[i].start();
            }

            for (int i = 0; i < rps.length; i++) {
                rps[i].join();
                System.out.println(rps[i] + "\n");
            }

        } catch (ParseException | InterruptedException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handle all the refinement part. Try to refine each rule on the ER output
     * directory. If succeeded, store the rule on the refinement directory on
     * the serialized version of the <code>{@link EvaluatedRuleExample}</code>
     */
    private void refinement() {
        try {
            redirectOutputStream(outRefinement + "statistics.txt");
            RuleMeasurer ruleMeasure = new LaplaceMeasure();
            Set<Literal> positiveExamples, negativeExamples;
            positiveExamples = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeExamples = FileContent.getExamplesLiterals(negativeTrainExample);
            EvaluatedRuleExample serializeRule;
            double threshold = 0.01;
            Box<Long> b = new Box<>(null), e = new Box(null);
            File[] listFiles = (new File(outER)).listFiles();
            for (File file : listFiles) {
                if (file.isFile() && file.getName().startsWith("rule") && file.getName().endsWith(".txt")) {
                    try {
                        System.out.println(Time.getTime(b));
                        System.out.println("File: " + file.getName());
                        EvaluatedRuleExample genericRuleExample;

                        genericRuleExample = new EvaluatedRuleExample(file);

                        Refinement r = new TopDownBoundedRefinement(args, dlpContent, genericRuleExample, threshold, positiveExamples, negativeExamples, timeout, ruleMeasure);
                        r.start();
                        r.join();

                        String fileName = file.getName();
                        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                        String outPath = outRefinementAll + fileName + "_";

                        Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                        Set<Integer> keys = rules.keySet();
                        Time.getTime(e);

                        File outputFile;
                        Integer biggestKey = 0;

                        for (Integer key : keys) {
                            outputFile = new File(outPath + key + ".txt");
                            serializeRule = new EvaluatedRuleExample(rules.get(key), genericRuleExample.getExample());
                            serializeRule.serialize(outputFile);
                            if (key > biggestKey) {
                                biggestKey = key;
                            }
                        }

                        outPath = outRefinement + fileName;
                        outputFile = new File(outPath + ".txt");

                        serializeRule = new EvaluatedRuleExample(rules.get(biggestKey), genericRuleExample.getExample());
                        serializeRule.serialize(outputFile);

                        System.out.println(Time.getTime(e));
                        double dif = e.getContent() - b.getContent();
                        dif /= 1000;
                        System.out.println("Total time for file(" + file.getName() + "): " + dif + "s");
                        System.out.println("\n");
                    } catch (IOException | InterruptedException ex) {
                        Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (FileNotFoundException | ParseException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Handle all the cross validation part. Try to cross validate each rule on
     * the refinement output directory. If succeeded, store the rule on the CV
     * directory on the serialized version of the
     * <code>{@link EvaluatedRuleExample}</code>
     */
    private void crossValidation() {
        File[] listFiles = (new File(outRefinement)).listFiles();
        List<Set<Literal>> positiveFolds = new ArrayList<>(cvNumberOfFolds);
        List<Set<Literal>> negativeFolds = new ArrayList<>(cvNumberOfFolds);

        try {
            for (int i = 1; i < cvNumberOfFolds + 1; i++) {
                positiveFolds.add(FileContent.getExamplesLiterals(FileContent.getStringFromFile(cvDirectory + cvPrefix + i + ".f")));
                negativeFolds.add(FileContent.getExamplesLiterals(FileContent.getStringFromFile(cvDirectory + cvPrefix + i + ".n")));
            }

            for (File file : listFiles) {
                if (file.isFile() && file.getName().startsWith("rule") && file.getName().endsWith(".txt")) {
                    crossValidate(file, positiveFolds, negativeFolds);
                }
            }
        } catch (IOException | ParseException | TimeoutException ex) {
            Logger.getLogger(DLRulesCLIParallel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Cross validate a single rule.
     *
     * @param ruleFile The rule which will be cross validated.
     * @param positiveFolds The set of positive folds.
     * @param negativeFolds The set of negative folds.
     * @throws FileNotFoundException In case of any file does not exist.
     * @throws TimeoutException In case of a rule does not be infered in the
     * timeout.
     * @throws ParseException In case of a file does not accord with the input
     * language.
     */
    private void crossValidate(File ruleFile, List<Set<Literal>> positiveFolds, List<Set<Literal>> negativeFolds) throws IOException, TimeoutException, ParseException {
        EvaluatedRuleExample cvRule = new EvaluatedRuleExample(ruleFile);
        EvaluatedRuleExample crossEvaluated;
        EvaluatedRule er;
        RuleEvaluator re;
        File out;

        String fileName = ruleFile.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        re = new RuleEvaluator(cvRule.getRule(), args, dlpContent, positiveFolds.get(0), negativeFolds.get(0));
        er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
        if (er == null)
            return;
        crossEvaluated = new EvaluatedRuleExample(er, cvRule.getExample());

        out = new File(outCV + fileName + "_Fold" + 1 + ".txt");
        crossEvaluated.serialize(out);
        for (int i = 1; i < positiveFolds.size(); i++) {
            er = RuleEvaluator.reEvaluateRule(re, positiveFolds.get(i), negativeFolds.get(i));
            if (er == null)
                return;
            crossEvaluated = new EvaluatedRuleExample(er, cvRule.getExample());

            out = new File(outCV + fileName + "_Fold" + (i + 1) + ".txt");
            crossEvaluated.serialize(out);
        }

    }

    /**
     * Print a measure of the generated rules. Format: Rule name, measure, based
     * example.
     *
     * @throws FileNotFoundException in case of the ER output directory does not
     * exist.
     */
    private void printMeasure() throws IOException {
        File folder = new File(outER);
        File[] listOfFiles = folder.listFiles();
        Set<EvaluatedRuleExample> ers = new TreeSet<>(new EvaluatedRuleComparator());
        EvaluatedRuleExample er;
        RuleMeasurer measure = new CompressionMeasure();

        for (File file : listOfFiles) {
            if (file.isFile() && !file.isHidden()) {
                er = new EvaluatedRuleExample(file);
                er.setRuleMeasureFunction(measure);

                ers.add(er);
            }
        }
        int count = 1;
        for (EvaluatedRuleExample evaluatedRule : ers) {
            System.out.println(count + ":\tRule: " + evaluatedRule.getSerializedFile().getName() + "\tMeasure: " + evaluatedRule.getMeasure() + "\tExamaple: " + evaluatedRule.getExample().toString());
            count++;
        }
    }

    /**
     * Getter for -rule.
     *
     * @return return true if the option -rule was setted, false otherwise.
     */
    public boolean isRule() {
        return rule;
    }

    /**
     * Setter for -rule
     *
     * @param rule true to enable -rule option, false disable off.
     */
    public void setRule(boolean rule) {
        this.rule = rule;
    }

    /**
     * Getter for -ref.
     *
     * @return return true if the option -ref was setted, false otherwise.
     */
    public boolean isRefinement() {
        return refinement;
    }

    /**
     * Setter for -ref
     *
     * @param refinement true to enable -ref option, false disable off.
     */
    public void setRefinement(boolean refinement) {
        this.refinement = refinement;
    }

    /**
     * Getter for -cv.
     *
     * @return return true if the option -cv was setted, false otherwise.
     */
    public boolean isCrossValidation() {
        return crossValidation;
    }

    /**
     * Setter for -cv
     *
     * @param crossValidation true to enable -cv option, false disable off.
     */
    public void setCrossValidation(boolean crossValidation) {
        this.crossValidation = crossValidation;
    }

}
