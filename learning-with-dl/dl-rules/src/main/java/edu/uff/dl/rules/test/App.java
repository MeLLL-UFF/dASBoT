/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.uff.dl.rules.cli.DLRulesCLI;
import edu.uff.dl.rules.cli.DLRulesHillClimbCLI;
import edu.uff.dl.rules.cli.parallel.DLRulesCLIParallel;
import edu.uff.dl.rules.cli.parallel.RefinementParallel;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.datalog.DataLogRule;
import edu.uff.dl.rules.datalog.SimplePredicate;
import edu.uff.dl.rules.drew.DReWRLCLI;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.exception.VariableGenerator;
import edu.uff.dl.rules.rules.AnswerSetRule;
import edu.uff.dl.rules.rules.DLExamplesRules;
import edu.uff.dl.rules.rules.avaliation.CompressionMeasure;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRule;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRuleComparator;
import edu.uff.dl.rules.rules.avaliation.EvaluatedRuleExample;
import edu.uff.dl.rules.rules.avaliation.LaplaceMeasure;
import edu.uff.dl.rules.rules.avaliation.RuleEvaluator;
import edu.uff.dl.rules.rules.avaliation.RuleMeasurer;
import edu.uff.dl.rules.rules.refinement.Refinement;
import edu.uff.dl.rules.rules.refinement.TopDownBoundedRefinement;
import edu.uff.dl.rules.util.AlphabetCounter;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.CLIArgumentsParser;
import edu.uff.dl.rules.util.DReWDefaultArgs;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.RuleFileNameComparator;
import edu.uff.dl.rules.util.Time;
import edu.uff.dl.rules.exception.TimeoutException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dllearner.confparser3.ParseException;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;

public class App {

    public static void redirectOutputStream(String filepath) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(filepath));
        System.setOut(out);
    }

    private static void evaluateCrossValidation(String cvDirectory, String out) throws IOException {
        CrossValidationEvaluator cve = new CrossValidationEvaluator(cvDirectory, "Fold", 5, new LaplaceMeasure());
        cve.run();
        System.out.println(cve);
        cve.saveToFile(out);
    }

    public static void checkParameters() throws FileNotFoundException {
        String[] parameters = FileContent.getStringFromFile("/Users/Victor/Desktop/parameters.txt").split("\n\n");

        for (String parameter : parameters) {
            try {
                CLIArgumentsParser ap = new CLIArgumentsParser(parameter.trim().split(" "));
                ap.checkParameters();
            } catch (org.semanticweb.drew.dlprogram.parser.ParseException ex) {
                System.out.println(parameter.trim());
                System.out.println(ex.getMessage());
                System.out.println("\n");
            }
        }

        //ap.checkParameters();
    }

    public static void evaluateAll() throws IOException {
        String cv1 = "/Users/Victor/Desktop/webkb/courseProfTrain";//1/CV/";
        String cv2 = "/Users/Victor/Desktop/webkb/courseTATrain";//1/CV";
        String out = "/Users/Victor/Desktop/";
        for (int i = 1; i < 5; i++) {
            evaluateCrossValidation(cv1 + i + "/CV/", out + "courseProfTrain" + i + ".txt");
            System.out.println("");
            evaluateCrossValidation(cv2 + i + "/CV/", out + "courseTATrain" + i + ".txt");
        }
    }

    public static void main(String[] args) throws Exception {
        //evaluateAll();
        evaluateCrossValidation("/Users/Victor/Desktop/sameAuthorTrain1/CV/", "/Users/Victor/Desktop/ecvCoraSameAuthorTrain1.txt");
        //testCLI();
        //testRun();
        //testMeasure("/Users/Victor/Desktop/out/ER/");
        //evaluateCrossValidation("/Users/Victor/Desktop/clusterResults/imdb/imdbTrain2/CV/");
        //System.out.println("test");
        //checkParameters();
    }

    private static void testCLI() throws FileNotFoundException {
        String[] args = {
            "-rule",
            "-ref",
            "-cv",
            "6",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap",
            "/Users/Victor/Desktop/kbs/uw-cse-testebinario/sample.owl",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.f",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.n",
            "-tp",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/template.dlp",
            "/Users/Victor/Desktop/out/",
            "300",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/",
            "test",
            "5"
        };
        String[] parameters = FileContent.getStringFromFile("/Users/Victor/Desktop/parameters.txt").split("\n\n");
        DLRulesCLI.main(parameters[2].trim().split(" "));
        //DLRulesHillClimbCLI.main(args);
    }

    public static void callCroosValidation() {
        String ruleDirectory = "/Users/Victor/Desktop/ER/";
        String ruleName = "rule";
        String outDirectory = "/Users/Victor/Desktop/CV/";
        String rule;
        File[] listOfFiles = (new File(ruleDirectory)).listFiles();
        for (File ruleFile : listOfFiles) {
            if (ruleFile.isFile() && ruleFile.getName().startsWith("rule")) {
                rule = ruleDirectory + ruleFile.getName();
                try {
                    System.out.println("Testing rule:\t" + rule);
                    crossValidationAvaliation(rule, "/Users/Victor/Desktop/kbs/cora/sameauthor/", 5, outDirectory + rule.substring(0, rule.lastIndexOf(".")) + "_test", 300);

                } catch (Exception e) {

                }
            }
        }
    }

    public static void crossValidationAvaliation(String ruleFile, String testFilePrefix, int tests, String outFilePrefix, int timeout) throws FileNotFoundException, org.semanticweb.drew.dlprogram.parser.ParseException, TimeoutException {
        EvaluatedRuleExample rule = new EvaluatedRuleExample(new File(ruleFile));

        Set<String> dlpFilepaths = new LinkedHashSet<>();

        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap");

        String dlpContent = FileContent.getStringFromFile(dlpFilepaths);

        String dlpPositiveFilePath;
        String dlpNegativeFilePath;

        Set<Literal> positiveExamples;
        Set<Literal> negativeExamples;

        RuleEvaluator re;

        EvaluatedRuleExample crossEvaluated;
        EvaluatedRule er;
        File out;

        String[] args = DReWDefaultArgs.ARGS;
        args[2] = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        for (int i = 1; i <= tests; i++) {
            dlpPositiveFilePath = testFilePrefix + i + ".f";
            dlpNegativeFilePath = testFilePrefix + i + ".n";

            positiveExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpPositiveFilePath));
            negativeExamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpNegativeFilePath));

            re = new RuleEvaluator(rule.getRule(), args, dlpContent, positiveExamples, negativeExamples);
            er = RuleEvaluator.evaluateRuleWithTimeout(re, timeout);
            if (er == null)
                continue;
            crossEvaluated = new EvaluatedRuleExample(er, rule.getExample());
            out = new File(outFilePrefix + i + ".txt");
            crossEvaluated.serialize(out);
        }

    }

    public static void testRefinement() throws FileNotFoundException, org.semanticweb.drew.dlprogram.parser.ParseException, InterruptedException {
        Set<String> dlpFilepaths = new LinkedHashSet<>();
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap");
        String dlpContent = FileContent.getStringFromFile(dlpFilepaths);

        List<String> dlpSamplesFilepath = new ArrayList<>(2);
        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.f");
        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");

        Set<Literal> positiveSamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpSamplesFilepath.get(0)));
        Set<Literal> negativeSamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpSamplesFilepath.get(1)));

        int timeout = 250;

        RuleMeasurer ruleMeasure = new LaplaceMeasure();

        String[] args = DReWDefaultArgs.ARGS;
        args[2] = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        double threshold = 0.01;
        Box<Long> b = new Box<>(null), e = new Box(null);
        File[] listFiles = (new File("/Users/Victor/Desktop/outER/")).listFiles();
        //File test = new File("/Users/Victor/Desktop/outER/rule43.txt");
        //File[] listFiles = {test};
        for (File file : listFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                System.out.println(Time.getTime(b));
                System.out.println("File: " + file.getName());
                EvaluatedRule genericRule = new EvaluatedRule(file);
                Refinement r = new TopDownBoundedRefinement(args, dlpContent, genericRule, threshold, positiveSamples, negativeSamples, timeout, ruleMeasure);
                r.start();
                r.join();

                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                String outPath = "/Users/Victor/Desktop/evaluated/" + fileName + "_";

                Map<Integer, EvaluatedRule> rules = r.getRefinedRules();
                Set<Integer> keys = rules.keySet();
                Time.getTime(e);

                File outputFile;

                for (Integer key : keys) {
                    outputFile = new File(outPath + key + ".txt");
                    rules.get(key).serialize(outputFile);
                }

                /*
                 Comparator com = new EvaluatedRuleComparator();
                 //SortedSet<EvaluatedRule> rules = new TreeSet<>(com);
                 //rules.addAll(r.getRefinedRules().values());
                 int count = 0;
                 for (EvaluatedRule evaluatedRule : rules) {
                 outputFile = new File(outPath + count + ".txt");
                 evaluatedRule.serialize(outputFile);
                 count++;
                 }
                 */
                System.out.println(Time.getTime(e));
                double dif = e.getContent() - b.getContent();
                dif /= 1000;
                System.out.println("Total time for file(" + file.getName() + "): " + dif + "s");
                System.out.println("");
            }
        }
    }

    public static void getInfo() throws FileNotFoundException {
        File[] listFiles = (new File("/Users/Victor/Desktop/outER/")).listFiles();
        File[] rules = (new File("/Users/Victor/Desktop/evaluated/")).listFiles();

        Comparator com1 = new RuleFileNameComparator("_");
        Comparator com2 = new RuleFileNameComparator("rule");
        Arrays.sort(listFiles, com2);
        Arrays.sort(rules, com1);

        String name, rname;
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        EvaluatedRule er;
        RuleMeasurer measure = new LaplaceMeasure();
        int count, sum = 0;
        boolean sb2Write;
        for (File file : listFiles) {
            if (file.isHidden())
                continue;

            name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));

            count = 0;
            sb2Write = false;
            for (File rule : rules) {
                if (!rule.getName().startsWith(name + "_")) {
                    continue;
                }
                count++;
                er = new EvaluatedRule(rule);
                er.setRuleMeasureFunction(measure);

                if (er.getPositivesCovered() > 0 || er.getNegativesCovered() > 0) {
                    if (!sb2Write) {
                        sb2.append(name);
                        sb2.append(":");
                        sb2.append("\n");
                        sb2Write = true;
                    }
                    rname = rule.getName();
                    rname = rname.substring(0, rname.lastIndexOf('.'));
                    sb2.append(rname);
                    sb2.append("\tCovered Positives:\t");
                    sb2.append(er.getPositivesCovered());
                    sb2.append("\tCovered Negatives:\t");
                    sb2.append(er.getNegativesCovered());
                    sb2.append("\tMeasure:\t");
                    sb2.append(er.getMeasure());
                    sb2.append("\n");
                }

            }
            if (sb2Write)
                sb2.append("\n");

            sb1.append(name);
            sb1.append(":\tRefined rules:\t");
            sb1.append(count);
            sb1.append("\n");
            sum += count;
        }

        sb1.append("Total: ");
        sb1.append(sum);
        sb1.append(" file(s).");

        System.out.println(sb1.toString().trim());
        System.out.println();
        System.out.println(sb2.toString().trim());
    }

    public static void testMeasure(String baseFilepath) throws FileNotFoundException {
        File folder = new File(baseFilepath);
        File[] listOfFiles = folder.listFiles();
        Set<EvaluatedRuleExample> ers = new TreeSet<>(new EvaluatedRuleComparator());
        EvaluatedRuleExample er;
        RuleMeasurer measure = new CompressionMeasure();
        int best = Integer.MIN_VALUE;
        for (File file : listOfFiles) {
            if (file.isFile() && !file.isHidden()) {
                er = new EvaluatedRuleExample(file);
                er.setRuleMeasureFunction(measure);

                ers.add(er);

                //System.out.println("Rule: " + file.getName() + " Measure: " + er.getMeasure());
            }
        }
        int count = 1;
        for (EvaluatedRuleExample evaluatedRule : ers) {
            System.out.println(count + ":\tRule: " + evaluatedRule.getSerializedFile().getName() + "\tMeasure: " + evaluatedRule.getMeasure() + "\tExamaple: " + evaluatedRule.getExample().toString());
            count++;
        }

    }

    public static void testRun() throws FileNotFoundException, ComponentInitException {
        Set<String> dlpFilepaths = new LinkedHashSet<>();
        String owlFilepath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        String templateFilepath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/template.dlp";
        List<String> dlpSamplesFilepath = new ArrayList<>(2);
        //Set<String> compareFilepaths = new LinkedHashSet<>();

        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap");

        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.f");
        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");
        //compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");

        //compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.f");
        //compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.n");
        String defaultFilepath = "/Users/Victor/Desktop/out/";

        DLExamplesRules run;

        String dlpContent = FileContent.getStringFromFile(dlpFilepaths);
        String samplesContent = FileContent.getStringFromFile(dlpSamplesFilepath.get(0));
        String templateContent = FileContent.getStringFromFile(templateFilepath);

        DReWReasoner reasoner = new DReWReasoner(owlFilepath, dlpContent, samplesContent, templateContent);
        reasoner.init();

        int count = 0;
        double min = Double.MAX_VALUE, max = 0, sun = 0, aux = 0;
        int maxR = 0, minR = 0;
        int delay = 250;
        EvaluatedRule er;
        EvaluatedRuleExample ere;
        File fOut;
        String outPath;
        for (int i = 0; i < 78; i++) {
            outPath = defaultFilepath + "rule" + i + ".txt";
            redirectOutputStream(outPath);
            run = new DLExamplesRules(FileContent.getStringFromFile(dlpFilepaths), reasoner, FileContent.getStringFromFile(dlpSamplesFilepath.get(0)), FileContent.getStringFromFile(dlpSamplesFilepath.get(1)));
            run.setOffset(i);
            run.start();
            try {
                run.join(delay * 1000);

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
                    System.out.println("It takes " + aux + "s to finish!");
                } else {
                    run.interrupt();
                    System.out.println("Stoped on " + delay + "s!");
                }

                er = run.getEvaluatedRule();
                ere = new EvaluatedRuleExample(er, run.getExamples().get(0));
                if (run.getEvaluatedRule() != null) {
                    fOut = new File(outPath.replace("out/", "outER/"));
                    ere.serialize(fOut);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        redirectOutputStream("statistics.txt");
        System.out.println("Max time: " + max + "\tfor rule " + maxR);
        System.out.println("Max time: " + min + "\tfor rule " + minR);
        System.out.println("Avg time: " + (sun / (double) count));
    }

    public static void testRunOutputFile() throws FileNotFoundException {
        Set<String> dlpFilepaths = new LinkedHashSet<>();
        String owlFilepath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        String templateFilepath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/template.dlp";
        List<String> dlpSamplesFilepath = new ArrayList<>(2);
        Set<String> compareFilepaths = new LinkedHashSet<>();

        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap");

        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.f");
        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");
        //compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");

        compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.f");
        compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.n");

        String defaultFilepath = "/Users/Victor/Desktop/out/";

        DLExamplesRulesFileOutput run;
        int count = 0;
        double min = Double.MAX_VALUE, max = 0, sun = 0, aux = 0;
        int maxR = 0, minR = 0;
        int delay = 250;
        for (int i = 0; i < 78; i++) {
            redirectOutputStream(defaultFilepath + "rule" + i + ".txt");
            run = new DLExamplesRulesFileOutput(dlpFilepaths, owlFilepath, templateFilepath, dlpSamplesFilepath.get(0),
                    dlpSamplesFilepath.get(1), compareFilepaths);
            run.setOffset(i);
            run.start();
            try {
                run.join(delay * 1000);

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
                    System.out.println("It takes " + aux + "s to finish!");
                } else {
                    run.interrupt();
                    System.out.println("Stoped on " + delay + "s!");
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        redirectOutputStream("statistics.txt");
        System.out.println("Max time: " + max + "\tfor rule " + maxR);
        System.out.println("Max time: " + min + "\tfor rule " + minR);
        System.out.println("Avg time: " + (sun / (double) count));
    }

    public static void allExamplesTest() throws Exception {
        double avg = 0;
        int maxLoop = 77; //77
        Set<Integer> badExamples = new HashSet<>();
        badExamples.add(35);
        badExamples.add(41);
        //badExamples.add(53);
        String pathPrefix = "/Users/Victor/Desktop/out3/";

        for (int i = 0; i < maxLoop; i++) {
            if (badExamples.contains(i))
                continue;
            String filepath = pathPrefix + "rule" + i + ".txt";
            redirectOutputStream(filepath);
            avg += simpleExampleTest(i);
        }
        redirectOutputStream(pathPrefix + "avg.txt");
        System.out.println("Average time: " + (avg / (maxLoop - badExamples.size())));
    }

    public static double simpleExampleTest(int offset) throws Exception {
        String begin, end;
        Box<Long> b = new Box<>(null), e = new Box(null);
        begin = Time.getTime(b);
        Time.getTime();

        testDReWReasoner(offset);

        end = Time.getTime(e);
        System.out.println("");
        System.out.println("Begin: " + begin);
        System.out.println("End:   " + end);
        double dif = e.getContent() - b.getContent();
        dif /= 1000;
        System.out.println("Total time: " + dif + "s");
        return dif;
    }

    public static void testDReWReasoner(int offset) throws ComponentInitException, FileNotFoundException, IOException, ParseException, org.semanticweb.drew.dlprogram.parser.ParseException {
        DReWReasoner dr;
        String samples;
        String negativeSamplesContent;
        String owlFilePath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        String dlpFilepath = "/Users/Victor/Dropbox/dl.rules/sample.dlp";
        dlpFilepath = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/lattesRules.dlp";
        String samplesFilePath = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/samples.dlp";

        //Arity = 2
        samplesFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.f";
        dlpFilepath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/all.yap";
        negativeSamplesContent = FileContent.getStringFromFile("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");
        boolean isNetwork = false;
        //isNetwork = true;
        if (isNetwork) {
            owlFilePath = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
            dlpFilepath = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
            samples = "newnode(x1).\nnewnode(x2).";

        } else {
            samples = FileContent.getStringFromFile(samplesFilePath);
            //samples = "flies(tweety).";
        }
        //String dlpContent = FileContent.getStringFromFile(dlpFilePath);
        String[] filepaths = {
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap",
            "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap",};

        String dlpContent = FileContent.getStringFromFile(filepaths);
        //String dlpContent = FileContent.getStringFromFile(dlpFilepath);
        String templateContent = FileContent.getStringFromFile("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/template.dlp");
        dr = new DReWReasoner(owlFilePath, dlpContent, samples, templateContent);
        dr.setOffset(offset);
        Set<Constant> individuals = new HashSet<>();
        Set<DataLogPredicate> predicates = new HashSet<>();

        dr.loadIndividualsAndPredicates(individuals, predicates);
        dr.init();
        dr.run();

        if (dr.getAnswerSetRules() == null || dr.getAnswerSetRules().size() < 1)
            return;
        AnswerSetRule asr = dr.getAnswerSetRules().get(0);

        String in = dlpContent + "\n" + asr.getRulesAsString();

        System.out.println(asr.getRulesAsString());
        System.out.println("");
        System.out.println("Rule's body: " + asr.getAnswerRule().getRules().iterator().next().getBody().size() + " literals.");
        System.out.println("");
        System.out.println("Comparar a Regra: " + Time.getTime());
        System.out.println("");
        boolean compare = false;
        compare = true;
        if (!compare)
            return;
        DReWRLCLILiteral drew = DReWRLCLILiteral.run(in, dr.getArg());
        Set<Literal> lits = drew.getLiteralModelHandler().getAnswerSets().get(0);

        System.out.println("Verificar exemplos positivos: " + Time.getTime());

        int positive = 0;
        for (Literal s : dr.getExamples()) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }
        System.out.println("Exemplos Positivos Cobertos (train1.f) / Total : " + positive + " / " + dr.getExamples().size());
        System.out.println("");
        System.out.println("Verificar exemplos negativos: " + Time.getTime());
        Set<Literal> listSamples = getSamplesLiterals(negativeSamplesContent);
        positive = 0;
        for (Literal s : listSamples) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }

        System.out.println("Exemplos Negativos Cobertos (train1.n) / Total : " + positive + " / " + listSamples.size());
        System.out.println("");

        listSamples = getSamplesLiterals(FileContent.getStringFromFile("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.f"));
        System.out.println("Verificar exemplos positivos: " + Time.getTime());
        positive = 0;
        for (Literal s : listSamples) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }

        System.out.println("Exemplos Positivos Cobertos (test1.f) / Total : " + positive + " / " + listSamples.size());
        System.out.println("");

        listSamples = getSamplesLiterals(FileContent.getStringFromFile("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.n"));
        System.out.println("Verificar exemplos negativos: " + Time.getTime());
        positive = 0;
        for (Literal s : listSamples) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }

        System.out.println("Exemplos Negativos Cobertos (test1.n) / Total : " + positive + " / " + listSamples.size());
        System.out.println("");
        //asr.setRulesToFile("/Users/Victor/Desktop/rules.dlp", false);
    }

    private static Set<Literal> getSamplesLiterals(String content) throws org.semanticweb.drew.dlprogram.parser.ParseException {
        List<ProgramStatement> programs = getProgramStatements(content);
        Set<Literal> samples = new HashSet<>();
        Clause c;
        Literal l;

        for (ProgramStatement ps : programs) {
            if (ps.isClause() && (c = ps.asClause()).isFact()) {
                l = new Literal(c.getHead().getPredicate(), c.getHead().getTerms());
                samples.add(l);
            }
        }

        return samples;
    }

    private static List<ProgramStatement> getProgramStatements(String content) throws org.semanticweb.drew.dlprogram.parser.ParseException {
        DLProgramKB kb = new DLProgramKB();

        DLProgram elprogram = null;

        DLProgramParser parser;

        Reader reader;

        reader = new StringReader(content);

        parser = new DLProgramParser(reader);

        elprogram = parser.program();
        kb.setProgram(elprogram);
        return elprogram.getStatements();
    }

    public static void testDReW() {
        String[] arg = new String[7];
        arg[0] = "-rl";
        arg[1] = "-ontology";
        arg[2] = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
        arg[3] = "-dlp";
        arg[4] = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
        arg[5] = "-dlv";
        arg[6] = "/usr/lib/dlv.i386-apple-darwin-iodbc.bin";
        boolean printMySets = true;
        //String kb = "/Users/Victor/Desktop/kb.pl";
        try {
            //testDLProgram(arg[2], arg[4]);
            //testDLProgram(arg[2], kb);

            if (printMySets) {
                //arg[2] = "/Users/Victor/Dropbox/dl.rules/sample.owl";
                //arg[4] = "";
                //arg[4] = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/lattesRules-with-rule.dlp";
                //String samplesFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/f0.f";
                //String dlpFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap";
                String out = FileContent.getStringFromFile(arg[4]) + "newnode(x1). newnode(x2).";//FileContent.getStringFromFile(samplesFilePath);
                //arg[4] = "/Users/Victor/Desktop/lattesRules-with-rule.dlp";
                System.out.println("My Sets");
                DReWRLCLILiteral d = DReWRLCLILiteral.run(out, arg);
                //testDataLogLiteral(d.getLiteralModelHandler().getAnswerSets().get(0));

                for (Set<Literal> l : d.getLiteralModelHandler().getAnswerSets()) {
                    /*
                     StringBuilder sb = new StringBuilder();
                     sb.append("{ ");
                     for (Literal lit : l) {
                     sb.append(lit);
                     sb.append(" ");
                     //System.out.print(lit);
                     }
                     //System.out.println("{");
                     sb.append("}");
                     System.out.println(sb.toString().trim());
                     //System.out.println("}\n");
                     */

                    //AnswerRule ar = new AnswerRule(e.getSamples(), e.getExpansionSet());
                    System.out.println(l);
                    //ar.init();
                    //System.out.println(ar.getRules().iterator().next().toString());

                }
            } else {
                System.out.println("DReW's Sets");
                DReWRLCLI.main(arg);
            }
        } catch (Exception ex) {
            System.err.println(ex.getClass().getName());
        }
    }

    public static void testCLICV() throws ParseException, IOException, ReasoningMethodUnsupportedException {
        String[] arg = new String[5];
        String path = "/Users/Victor/Dropbox/Iniciação Científica/dl/trainTest/";
        String prefixFile = "facultynear";
        String fullPath = path + prefixFile + "/" + prefixFile;

        arg[0] = "simpleCV";
        arg[1] = fullPath + "2.conf";
        arg[2] = fullPath;
        arg[3] = "10";
        arg[4] = fullPath + "Rule.txt";

        CLICV.main(arg);
    }

//    public static void testCLI() throws ParseException, IOException, ReasoningMethodUnsupportedException {
//        String[] arg = {"/Users/Victor/workspace/trunk/examples/arch/arch_owl.conf"};
//        CLI.main(arg);
//        //uff.dl.rules.CLI.main(arg); //Meu CLI, usando o ParcelPosNegLPRules
//        //org.dllearner.cli.CLI.main(arg); //Do DL-Learner
//    }
    public static void testGenerator() {
        VariableGenerator vg = new AlphabetCounter();
        //((AlphabetCounter) vg).setCount(475253);
        List<String> list = new LinkedList<>();
        String in;
        int repeat = 0;
        for (int i = 0; i < 475253; i++) {
            in = vg.getNextName();
            if (list.contains(in)) {
                repeat++;
            } else {
                list.add(vg.getNextName());
            }
            System.out.println(in);
        }
        System.out.println("Repetidos: " + repeat);
    }

}
