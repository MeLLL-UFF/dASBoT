/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.cli;

import br.uff.dl.rules.datalog.ConcreteLiteral;
import br.uff.dl.rules.drew.DReWReasoner;
import br.uff.dl.rules.evaluation.CompressionMeasure;
import br.uff.dl.rules.evaluation.LaplaceMeasure;
import br.uff.dl.rules.evaluation.RuleMeasurer;
import br.uff.dl.rules.exception.TimeoutException;
import br.uff.dl.rules.rules.DLExamplesRules;
import br.uff.dl.rules.rules.evaluation.EvaluatedRule;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleComparator;
import br.uff.dl.rules.rules.evaluation.EvaluatedRuleExample;
import br.uff.dl.rules.rules.evaluation.RuleEvaluator;
import br.uff.dl.rules.rules.refinement.Refinement;
import br.uff.dl.rules.rules.refinement.RefinementFactory;
import br.uff.dl.rules.util.Box;
import br.uff.dl.rules.util.DReWDefaultArgs;
import br.uff.dl.rules.util.FileContent;
import br.uff.dl.rules.util.Time;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to call the program by command line interface. This class can be used
 * to generate rules for each example on the trainer file, refine and evaluate
 * the rules against test fold using cross validation.
 *
 * @author Victor Guimarães
 */
public class DLRulesCLI {

    public static final String RULE_MEASURE_PACKAGE_NAME = "br.uff.dl.rules.evaluation";
    public static final String DLV_FILE_SUFIX = "--rl.dlv";

    protected String owlFilepath;
    protected String outputDirectory;

    protected int timeout;

    protected String outER;
    protected String outRefinement;
    protected String outRefinementAll;
    protected String outCV;
    protected String dlpContent;
    protected String positiveTrainExample;
    protected String positiveTrainFilePath;
    protected String negativeTrainExample;
    protected String negativeTrainFilePath;
    protected String templateContent;
    protected String cvDirectory;
    protected String cvPrefix;
    protected int cvNumberOfFolds;

    protected boolean rule;
    protected boolean refinement;
    protected String refinementClass;
    protected boolean generic;
    protected boolean crossValidation;
    protected boolean recursiveRuleAllowed = true;
    protected String dlvPath = null;

    protected int depth;
    protected double threshold;

    protected String[] drewArgs = DReWDefaultArgs.getDefaultArgs();
    protected String[] initArgs;

    protected RuleMeasurer generateRuleMeasure;
    protected RuleMeasurer refinementRuleMeasure;

    public synchronized Queue<String> parseArguments(String[] args) throws FileNotFoundException {
        String template = null;
        int numberOfDLPFiles = 0;
        Queue<String> queue = new LinkedList<>();

        for (String arg : args) {
            queue.add(arg);
        }

        boolean lRule = false;
        boolean ref = false;
        boolean cv = false;
        boolean noRec = false;
        boolean lGeneric = false;
        String refClass = null;

        String lDLVPath = null;

        String peek;
        while (queue.peek().startsWith("-")) {
            peek = queue.remove().toLowerCase();
            switch (peek) {
                case "-rule":
                    lRule = true;
                    break;
                case "-refclass":
                    refClass = queue.remove();
                    ref = true;
                    break;
                case "-ref":
                    ref = true;
                    if (queue.peek().toLowerCase().equals("gen")) {
                        lGeneric = true;
                        queue.remove();
                    }
                    break;
                case "-cv":
                    cv = true;
                    break;
                case "-norec":
                    noRec = true;
                    break;
                case "-dlv":
                    lDLVPath = queue.remove();
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

        Set<String> dlpFilepaths = new LinkedHashSet<>(numberOfDLPFiles);
        for (int i = 0; i < numberOfDLPFiles; i++) {
            dlpFilepaths.add(queue.remove());
        }

        String lOWLFilepath = queue.remove();
        String positeveTrain = queue.remove();
        String negativeTrain = queue.remove();

        if (queue.peek().toLowerCase().equals("-tp")) {
            queue.remove();
            template = queue.remove();
        }

        String lOutputDirectory = queue.remove();
        if (!lOutputDirectory.endsWith("/")) {
            lOutputDirectory += "/";
        }

        int lTimeout = Integer.parseInt(queue.remove());

        String lCVDirectory = null;
        String lCVPrefix = null;
        int lCVNumberOfFolds = 0;

        if (cv) {
            lCVDirectory = queue.remove();
            lCVPrefix = queue.remove();
            lCVNumberOfFolds = Integer.parseInt(queue.remove());
        }

        int lDepth = (!queue.isEmpty() ? Integer.parseInt(queue.remove()) : 0);
        double lThreshold = (!queue.isEmpty() ? Double.parseDouble(queue.remove()) : 0.0);

        String generateMeasure = (!queue.isEmpty() ? queue.remove() : "CompressionMeasure");
        String refinementMeasure = (!queue.isEmpty() ? queue.remove() : "LaplaceMeasure");

        this.setDLPFilepaths(dlpFilepaths);
        this.setOWLFilepath(lOWLFilepath);
        this.setPositiveTrainFilepath(positeveTrain);
        this.setNegativeTrainFilepath(negativeTrain);
        this.setOutputDirectory(lOutputDirectory);
        this.setTimeout(lTimeout);
        this.setTemplateFilepath(template);
        this.setCvDirectory(lCVDirectory);
        this.setCvPrefix(lCVPrefix);
        this.setCvNumberOfFolds(lCVNumberOfFolds);

        this.setRule(lRule);
        this.setRefinement(ref);
        this.setRefinementClass(refClass);
        this.setGeneric(lGeneric);
        this.setCrossValidation(cv);
        this.setDLVPath(lDLVPath);
        this.setRecursiveRuleAllowed(!noRec);
        this.setDepth(lDepth);
        this.setThreshold(lThreshold);
        this.setInitArgs(args);
        this.setGenerateRuleMeasure(generateMeasure);
        this.setRefinementRuleMeasure(refinementMeasure);

        return queue;
    }

    /**
     * Main function, used to start the program.
     *
     * @param args the parameters needed for the program execution.<br>-rule to
     * generate the rules (optional),<br>-ref to refine the rules (optional),
     * gen, right after -ref, to refine most generic rules (option),<br>-cv to
     * cross validate the rules (optional),<br>-norec to not allow recursive
     * rules,<br>an integer number of bk files (omitted = 1),<br>a set of paths
     * for the bk files, according with the number of files previous
     * setted,<br>-tp to use template (file to type the individuos according
     * with its relationships) (optional),<br>if -tp was used, the path of the
     * template file,<br>an output directory for the program's output,<br>a
     * timeout for the rule's inferences,<br>the cross validation directory with
     * the folds,<br>the fold's prefix name.
     * @throws FileNotFoundException in case of a file path does not exist.
     */
    public static void main(String[] args) throws FileNotFoundException {
        try {
            DLRulesCLI dlrcli = new DLRulesCLI();
            dlrcli.parseArguments(args);

            dlrcli.init();
        } catch (NoSuchElementException | NumberFormatException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DLRulesCLI() {
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
     * @throws FileNotFoundException in case of any file path does not exist.
     */
    public DLRulesCLI(Set<String> dlpFilepaths, String owlFilepath, String positiveTrainFilepath, String negativeTrainFilepath, String outputDirectory, int timeout, String templateFilepath, String cvDirectory, String cvPrefix, int cvNumberOfFolds) throws FileNotFoundException {
        this.owlFilepath = owlFilepath;
        drewArgs[2] = owlFilepath;

        this.outputDirectory = outputDirectory;
        this.timeout = timeout;

        this.dlpContent = FileContent.getStringFromFile(dlpFilepaths);
        this.outER = outputDirectory + "ER" + "/";
        this.outRefinement = outputDirectory + "refinement/";
        this.outRefinementAll = outRefinement + "all/";
        this.outCV = outputDirectory + "CV" + "/";

        this.positiveTrainExample = FileContent.getStringFromFile(positiveTrainFilepath);
        this.negativeTrainExample = FileContent.getStringFromFile(negativeTrainFilepath);

        if (templateFilepath != null && !templateFilepath.isEmpty()) {
            templateContent = FileContent.getStringFromFile(templateFilepath);
        }

        this.cvDirectory = cvDirectory;
        this.cvPrefix = cvPrefix;
        this.cvNumberOfFolds = cvNumberOfFolds;
    }

    /**
     * Create all the output directories.
     */
    protected void createOutputDirectories() {
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
        createOutputDirectories();

        if (generateRuleMeasure == null) {
            setGenerateRuleMeasure(null);
        }

        if (refinementRuleMeasure == null) {
            setRefinementRuleMeasure(null);
        }

        StringBuilder sb = new StringBuilder();

        Box<Long> globalBegin = new Box<>(null);
        Box<Long> globalEnd = new Box<>(null);
        Box<Long> localBegin;
        Box<Long> localEnd;

        if (initArgs != null) {
            for (int i = 0; i < initArgs.length; i++) {
                sb.append(initArgs[i]).append("\n");
            }
            sb.append("\n");
        }

        Time.getTime(globalBegin);
        if (rule) {
            localBegin = new Box<>(null);
            localEnd = new Box<>(null);
            Time.getTime(localBegin);

            generateRuleForEachExample();

            Time.getTime(localEnd);
            sb.append("Rules Total Time:\t").append(Time.getDiference(localBegin, localEnd)).append("\n");
        }

        if (refinement) {
            localBegin = new Box<>(null);
            localEnd = new Box<>(null);
            Time.getTime(localBegin);

            refinement();

            Time.getTime(localEnd);
            sb.append("Refinement Total Time:\t").append(Time.getDiference(localBegin, localEnd)).append("\n");
        }

        if (crossValidation) {
            localBegin = new Box<>(null);
            localEnd = new Box<>(null);
            Time.getTime(localBegin);

            crossValidation();

            Time.getTime(localEnd);
            sb.append("Cross Val Total Time:\t").append(Time.getDiference(localBegin, localEnd)).append("\n");
        }

        Time.getTime(globalEnd);
        sb.append("Global Total Time:\t").append(Time.getDiference(globalBegin, globalEnd));

        try {
            FileUtils.writeStringToFile(new File(outputDirectory + "globalStatistics.txt"), sb.toString().trim(), true);
        } catch (IOException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    /**
     * Handle all the generation part. For each example on the trainer file, try
     * to create a rule. If succeeded, store the rule on the ER directory on the
     * serialized version of the <code>{@link EvaluatedRuleExample}</code>
     */
    protected void generateRuleForEachExample() {
        try {
            DLExamplesRules run;

            int count = 0;
            double min = Double.MAX_VALUE, max = 0, sun = 0, aux = 0;
            int maxR = 0, minR = 0;
            Box<Long> begin = new Box<>(null), end = new Box(null);
            int size;
            EvaluatedRule er = null;
            EvaluatedRuleExample evaluatedRuleExample;
            File fOut;
            String ruleName;
            List<ConcreteLiteral> examples;
            ConcreteLiteral example;
            Time.getTime(begin);
            DReWReasoner reasoner = new DReWReasoner(dlvPath, owlFilepath, dlpContent, positiveTrainExample, templateContent, null);
            reasoner.setDepth(depth);
            reasoner.setRecursiveRuleAllowed(recursiveRuleAllowed);
            reasoner.init();

            size = reasoner.getExamples().size();

            List<EvaluatedRuleExample> evaluatedRuleExamples = new ArrayList<>();
            PrintStream outStream = null;
            for (int i = 0; i < size; i++) {
                ruleName = "rule" + i + ".txt";
                try {
//                    redirectOutputStream(outputDirectory + ruleName);
                    outStream = new PrintStream(outputDirectory + ruleName);
                    reasoner.setOutStream(outStream);
                    run = new DLExamplesRules(dlpContent, reasoner, positiveTrainExample, negativeTrainExample, outStream);
                    run.setOffset(i);
                    run.start();
                    run.join(timeout * 1000);

                    if (run.getState() == Thread.State.TERMINATED) {
                        aux = run.getDuration();
                        sun += aux;
                        if (aux < min) {
                            min = aux;
                            minR = i;
                        }

                        if (aux > max) {
                            max = aux;
                            maxR = i;
                        }

                        count++;
                        outStream.println("It takes " + aux + "s to finish!");
                        er = run.getEvaluatedRule();

                    } else {
                        run.interrupt();
                        outStream.println("Stoped on " + timeout + "s!");
                    }

                    if (er != null) {
                        evaluatedRuleExample = new EvaluatedRuleExample(er, run.getExamples().get(0));
                    } else {
                        examples = run.getExamples();

                        if (examples.size() == 1) {
                            example = examples.get(0);
                        } else {
                            example = examples.get(run.getOffset());
                        }
                        FileContent.getRuleFromString(run.getAnwserSetRule().getAnswerRule().getRule().toString());
                        evaluatedRuleExample = new EvaluatedRuleExample(run.getAnwserSetRule().getAnswerRule().getRule(), 0, 0, 0, 0, generateRuleMeasure, example);
                    }

                    fOut = new File(outER + ruleName);
                    evaluatedRuleExample.serialize(fOut);

                    evaluatedRuleExamples.add(evaluatedRuleExample);
                } catch (InterruptedException | FileNotFoundException | NullPointerException | ParseException ex) {
                    if (outStream != null) {
                        outStream.println(ex.getClass() + ": " + ex.getMessage());
                    }
                } finally {
                    if (outStream != null) {
                        outStream.close();
                    }
                }
            }

            Collections.sort(evaluatedRuleExamples, new EvaluatedRuleComparator());
            outStream = new PrintStream(outputDirectory + "statistics.txt");
            try {
                Time.getTime(end);
//                redirectOutputStream(outputDirectory + "statistics.txt");

                outStream.println("Total of " + count + " infered rule(s)");
                outStream.println("Max time:\t" + max + "\tfor rule " + maxR);
                outStream.println("Min time:\t" + min + "\tfor rule " + minR);
                outStream.println("Avg time:\t" + (sun / (double) count));
                outStream.println("Total time:\t" + Time.getDiference(begin.getContent(), end.getContent()));
                outStream.println("\n");
                printMeasure(evaluatedRuleExamples, outStream);
            } catch (IOException ex) {
                Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Print Error:\t1");
            } finally {
                outStream.close();
            }
        } catch (ComponentInitException | FileNotFoundException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Print Error:\t2");
        }
    }

    /**
     * Handle all the refinement part. Try to refine each rule on the ER output
     * directory. If succeeded, store the rule on the refinement directory on
     * the serialized version of the <code>{@link EvaluatedRuleExample}</code>
     */
    @SuppressWarnings("UseSpecificCatch")
    protected void refinement() {
        Box<Long> b = new Box<>(null), e = new Box(null);
        try (PrintStream outStream = new PrintStream(new FileOutputStream(outRefinement + "statistics.txt", true))) {
//            PrintStream outStream = new PrintStream(new FileOutputStream(outRefinement + "statistics.txt", true));
//            redirectOutputStream(outRefinement + "statistics.txt", true);

            outStream.println("Begin time:\t" + Time.getTime(b));
            outStream.println("Refinement Threshold: " + threshold);

            Set<Literal> positiveExamples, negativeExamples;
            positiveExamples = FileContent.getExamplesLiterals(positiveTrainExample);
            negativeExamples = FileContent.getExamplesLiterals(negativeTrainExample);
            EvaluatedRuleExample serializeRule;

            File[] listFiles = (new File(outER)).listFiles();
//            String eqvFile = "/Users/Victor/Desktop/ER" + outER.substring(outER.length() - 5, outER.length() - 4) + "/";
            for (File file : listFiles) {
//                if (!new File(eqvFile + file.getName()).exists()) {
//                    continue;
//                }
                if (file.isFile() && file.getName().startsWith("rule") && file.getName().endsWith(".txt")) {
                    try {
                        outStream.println(Time.getTime(b));
                        outStream.println("File: " + file.getName());
                        EvaluatedRuleExample genericRuleExample;

                        genericRuleExample = new EvaluatedRuleExample(file);

                        Refinement r = RefinementFactory.getRefinement(refinementClass);
                        r.setArgs(drewArgs);
                        r.setDlpContent(dlpContent);
                        r.setBoundRule(genericRuleExample);
                        r.setThreshold(threshold);
                        r.setPositiveExamples(positiveExamples);
                        r.setNegativeExamples(negativeExamples);
                        r.setTimeout(timeout);
                        r.setRuleMeasure(refinementRuleMeasure);
                        r.setOutStream(outStream);

                        r.start();
                        r.join();
                        String fileName = file.getName();
                        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                        String outPath = outRefinementAll + fileName + "_";

                        Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                        List<Integer> keys = new ArrayList<>(rules.keySet());
                        if (keys.isEmpty()) {
                            if (genericRuleExample.getPositives() != 0) {
                                keys.add(genericRuleExample.getRule().getBody().size());
                                rules.put(genericRuleExample.getRule().getBody().size(), genericRuleExample);
                            } else {
                                continue;
                            }
                        }

                        Collections.sort(keys);
                        Time.getTime(e);

                        File outputFile;

                        for (Integer key : keys) {
                            outputFile = new File(outPath + key + ".txt");
                            serializeRule = new EvaluatedRuleExample(rules.get(key), genericRuleExample.getExample());
                            serializeRule.serialize(outputFile);
                        }

                        outPath = outRefinement + fileName;
                        outputFile = new File(outPath + ".txt");

                        int refinedRuleIndex = keys.size() - 1;
                        serializeRule = new EvaluatedRuleExample(rules.get(keys.get(refinedRuleIndex)), genericRuleExample.getExample(), refinementRuleMeasure);
                        double localMeasure = serializeRule.getMeasure();
                        if (generic) {
                            EvaluatedRuleExample otherRule;
                            double otherMeasure;
                            for (int i = refinedRuleIndex - 1; i > -1; i--) {
                                otherRule = new EvaluatedRuleExample(rules.get(keys.get(i)), genericRuleExample.getExample(), refinementRuleMeasure);
                                otherMeasure = otherRule.getMeasure();
                                if (otherMeasure == localMeasure) {
                                    serializeRule = otherRule;
                                }
                            }
                        }

                        serializeRule.serialize(outputFile);

                        outStream.println(Time.getTime(e));
                        double dif = e.getContent() - b.getContent();
                        dif /= 1000;
                        outStream.println("Total time for file(" + file.getName() + "): " + dif + "s");
                        outStream.println("\n");
                    } catch (IOException | InterruptedException | NullPointerException ex) {
                        System.out.println("DLV Error! 1");
                        Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        System.out.println("DLV Error! 2");
                        Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            outStream.println("End time:\t\t" + Time.getTime(e));
            outStream.println("Total time:\t\t" + Time.getDiference(b, e));
        } catch (FileNotFoundException | ParseException ex) {
            System.out.println("DLV Error! 3");
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Handle all the cross validation part. Try to cross validate each rule on
     * the refinement output directory. If succeeded, store the rule on the CV
     * directory on the serialized version of the
     * <code>{@link EvaluatedRuleExample}</code>
     */
    protected void crossValidation() {
        Box<Long> b = new Box<>(null), e = new Box(null);
        try (PrintStream outStream = new PrintStream(new FileOutputStream(outRefinement + "statistics.txt", true))) {
            outStream.println("Begin Time:\t" + Time.getTime(b));
            outStream.println("");
            outStream.println("");
            File[] listFiles = (new File(outRefinement)).listFiles();
            List<Set<Literal>> positiveFolds = new ArrayList<>(cvNumberOfFolds);
            List<Set<Literal>> negativeFolds = new ArrayList<>(cvNumberOfFolds);

            for (int i = 1; i < cvNumberOfFolds + 1; i++) {
                positiveFolds.add(FileContent.getExamplesLiterals(FileContent.getStringFromFile(cvDirectory + cvPrefix + i + ".f")));
                negativeFolds.add(FileContent.getExamplesLiterals(FileContent.getStringFromFile(cvDirectory + cvPrefix + i + ".n")));
            }

            for (File file : listFiles) {
                if (file.isFile() && file.getName().startsWith("rule") && file.getName().endsWith(".txt")) {
                    crossValidate(file, positiveFolds, negativeFolds, outStream);
                }
            }
            outStream.println("");
            outStream.println("End Time:\t" + Time.getTime(e));
            outStream.println("Total Time:\t" + Time.getDiference(b, e));
        } catch (IOException | ParseException | TimeoutException ex) {
            Logger.getLogger(DLRulesCLI.class.getName()).log(Level.SEVERE, null, ex);
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
    protected void crossValidate(File ruleFile, List<Set<Literal>> positiveFolds, List<Set<Literal>> negativeFolds, PrintStream outStream) throws IOException, TimeoutException, ParseException {
        Box<Long> b = new Box<>(null), e = new Box(null);
        EvaluatedRuleExample cvRule = new EvaluatedRuleExample(ruleFile);
        EvaluatedRuleExample crossEvaluated;
        EvaluatedRule er;
        RuleEvaluator re;
        File out;
        Time.getTime(b);
        String fileName = ruleFile.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        re = new RuleEvaluator(cvRule.getRule(), drewArgs, dlpContent, positiveFolds.get(0), negativeFolds.get(0));
        er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
        if (er == null) {
            return;
        }
        crossEvaluated = new EvaluatedRuleExample(er, cvRule.getExample());

        out = new File(outCV + fileName + "_Fold" + 1 + ".txt");
        crossEvaluated.serialize(out);
        for (int i = 1; i < positiveFolds.size(); i++) {
            er = RuleEvaluator.reEvaluateRule(re, positiveFolds.get(i), negativeFolds.get(i));
            if (er == null) {
                return;
            }
            crossEvaluated = new EvaluatedRuleExample(er, cvRule.getExample());

            out = new File(outCV + fileName + "_Fold" + (i + 1) + ".txt");
            crossEvaluated.serialize(out);
        }
        Time.getTime(e);
        outStream.println("Total Time for rules " + fileName + ":\t" + Time.getDiference(b, e));
        outStream.println("");
    }

    /**
     * Print a measure of the generated rules. Format: Rule name, measure, based
     * example.
     *
     * @param ers List of sorted rules to print
     * @throws FileNotFoundException in case of the ER output directory does not
     * exist.
     */
    protected void printMeasure(List<EvaluatedRuleExample> ers, PrintStream outStream) throws IOException {
        int count = 1;
        String line, name;
        for (EvaluatedRuleExample evaluatedRule : ers) {
            name = evaluatedRule.getSerializedFile().getName();
            line = count + ":\tRule: " + name;
            if (Integer.parseInt(name.substring(4, name.lastIndexOf("."))) < 10) {
                line += "\t";
            }
            line += "\tMeasure: " + evaluatedRule.getMeasure() + "\tExamaple: " + evaluatedRule.getExample().toString();
            outStream.println(line);
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

    public String getRefinementClass() {
        return refinementClass;
    }

    public void setRefinementClass(String refinementClass) {
        this.refinementClass = refinementClass;
    }

    /**
     * Getter for most generic rule option at refinement.
     *
     * @return return true if most generic rule was setted, false otherwise.
     */
    public boolean isGeneric() {
        return generic;
    }

    /**
     * Setter for most generic rule option at refinement.
     *
     * @param generic true to enable most generic rule, false to most specific
     * rule.
     */
    public void setGeneric(boolean generic) {
        this.generic = generic;
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

    /**
     * Getter for -norec. It is true if recursion is allowed at the rule, false
     * otherwise.
     * <br> It is true by default.
     *
     * @return the {@link #recursiveRuleAllowed}.
     */
    public boolean isRecursiveRuleAllowed() {
        return recursiveRuleAllowed;
    }

    /**
     * Setter for -norec. Set true to allow the recursion, false to do not.
     * <br> It is true by default.
     *
     * @param recursiveRuleAllowed the {@link #recursiveRuleAllowed}.
     */
    public void setRecursiveRuleAllowed(boolean recursiveRuleAllowed) {
        this.recursiveRuleAllowed = recursiveRuleAllowed;
    }

    public String getDLVPath() {
        return dlvPath;
    }

    public void setDLVPath(String dlvPath) {
        this.dlvPath = dlvPath;
        if (dlvPath != null && !dlvPath.isEmpty()) {
            drewArgs[drewArgs.length - 1] = dlvPath;
        }
    }

    /**
     * Getter for the depth of the transitivity on the Expansion Answer Set.
     *
     * @return the depth of the transitivity.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Setter for the depth of the transitivity on the Expansion Answer Set.
     *
     * @param depth the depth of the transitivity.
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Getter for the refinement threshold.
     *
     * @return the refinement threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Setter for the refinement threshold.
     *
     * @param threshold the refinement threshold
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    protected void setInitArgs(String[] initArgs) {
        this.initArgs = initArgs;
    }

    public RuleMeasurer getGenerateRuleMeasure() {
        return generateRuleMeasure;
    }

    public void setGenerateRuleMeasure(String generateRuleMeasureClassName) {
        try {
            this.generateRuleMeasure = (RuleMeasurer) Class.forName(RULE_MEASURE_PACKAGE_NAME + "." + generateRuleMeasureClassName).newInstance();
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | NullPointerException |
                ClassCastException ex) {
            this.generateRuleMeasure = new CompressionMeasure();
        }
    }

    public RuleMeasurer getRefinementRuleMeasure() {
        return refinementRuleMeasure;
    }

    public void setRefinementRuleMeasure(String refinementRuleMeasureClassName) {
        try {
            this.refinementRuleMeasure = (RuleMeasurer) Class.forName(RULE_MEASURE_PACKAGE_NAME + "." + refinementRuleMeasureClassName).newInstance();
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | NullPointerException |
                ClassCastException ex) {
            this.refinementRuleMeasure = new LaplaceMeasure();
        }
    }

    public void setDLPFilepaths(Set<String> dlpFilepaths) throws FileNotFoundException {
        this.dlpContent = FileContent.getStringFromFile(dlpFilepaths);
    }

    public void setOWLFilepath(String owlFilepath) {
        this.owlFilepath = owlFilepath;
        drewArgs[2] = owlFilepath;
    }

    public void setPositiveTrainFilepath(String positiveTrainFilepath) throws FileNotFoundException {
        this.positiveTrainFilePath = positiveTrainFilepath;
        this.positiveTrainExample = FileContent.getStringFromFile(positiveTrainFilepath);
    }

    public String getPositiveTrainFilePath() {
        return positiveTrainFilePath;
    }

    public void setNegativeTrainFilepath(String negativeTrainFilepath) throws FileNotFoundException {
        this.negativeTrainFilePath = negativeTrainFilepath;
        this.negativeTrainExample = FileContent.getStringFromFile(negativeTrainFilepath);
    }

    public String getNegativeTrainFilePath() {
        return negativeTrainFilePath;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;

        this.outER = outputDirectory + "ER" + "/";
        this.outRefinement = outputDirectory + "refinement/";
        this.outRefinementAll = outRefinement + "all/";
        this.outCV = outputDirectory + "CV" + "/";
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setTemplateFilepath(String templateFilepath) throws FileNotFoundException {
        if (templateFilepath != null && !templateFilepath.isEmpty()) {
            templateContent = FileContent.getStringFromFile(templateFilepath);
        }
    }

    public void setCvDirectory(String cvDirectory) {
        this.cvDirectory = cvDirectory;
    }

    public void setCvPrefix(String cvPrefix) {
        this.cvPrefix = cvPrefix;
    }

    public void setCvNumberOfFolds(int cvNumberOfFolds) {
        this.cvNumberOfFolds = cvNumberOfFolds;
    }

    public String getOwlFilepath() {
        return owlFilepath;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getOutER() {
        return outER;
    }

    public String getOutRefinement() {
        return outRefinement;
    }

    public String getOutRefinementAll() {
        return outRefinementAll;
    }

    public String getOutCV() {
        return outCV;
    }

    public String getDlpContent() {
        return dlpContent;
    }

    public String getPositiveTrainExample() {
        return positiveTrainExample;
    }

    public String getNegativeTrainExample() {
        return negativeTrainExample;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public String getCvDirectory() {
        return cvDirectory;
    }

    public String getCvPrefix() {
        return cvPrefix;
    }

    public int getCvNumberOfFolds() {
        return cvNumberOfFolds;
    }

    public String getDlvPath() {
        return dlvPath;
    }

    public String[] getDrewArgs() {
        return drewArgs;
    }

    public String[] getInitArgs() {
        return initArgs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Arrays.deepHashCode(this.initArgs);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DLRulesCLI other = (DLRulesCLI) obj;
        if (!Arrays.deepEquals(this.initArgs, other.initArgs)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("DLRulesCLI:\n");
        sb.append("\tOWL Filepath:\t").append(owlFilepath).append("\n");
        sb.append("\tOutput Directory:\t").append(outputDirectory).append("\n");
        sb.append("\tTimeout:\t\t").append(timeout).append("\n");
        sb.append("\tRefinement:\t\t").append(refinement).append("\n");
        sb.append("\tGeneric:\t\t").append(generic).append("\n");
        sb.append("\tCross Validation:\t").append(crossValidation).append("\n");
        sb.append("\tRecursive Rule Allowed:\t").append(recursiveRuleAllowed).append("\n");
        sb.append("\tDLV Path:\t\t").append(dlvPath).append("\n");
        sb.append("\tDepth:\t\t").append(depth).append("\n");
        sb.append("\tThreshold:\t\t").append(threshold).append("\n");

        if (drewArgs != null) {
            sb.append("\tDrew Arguments:\n");
            for (int i = 0; i < drewArgs.length; i++) {
                sb.append("\t\t").append(drewArgs[i]).append("\n");
            }
        }

        if (drewArgs != null) {
            sb.append("\tInit Arguments:\n");
            for (int i = 0; i < initArgs.length; i++) {
                sb.append("\t\t").append(initArgs[i]).append("\n");
            }
        }

        sb.append("\tGenerate Rule Measure:\t").append(generateRuleMeasure).append("\n");
        sb.append("\tRefinement Rule Measure:\t").append(refinementRuleMeasure);
        sb.append("\tRefinement Class Name:\t").append(refinementClass);

        return sb.toString().trim();
    }

}
