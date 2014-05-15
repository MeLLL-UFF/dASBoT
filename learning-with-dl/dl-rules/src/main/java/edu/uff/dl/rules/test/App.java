/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.uff.dl.rules.drew.DReWRLCLI;
import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.util.StringResource;
import edu.uff.dl.rules.util.BKRules;
import edu.uff.dl.rules.util.AtomTerm;
import edu.uff.dl.rules.util.PosNegLPRules;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.parser.PrologParser;
import org.dllearner.confparser3.ParseException;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.prolog.Atom;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import edu.uff.dllearnerUtil.cliUtil.IOUtil;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.rules.AnswerSetRule;
import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.expansion.set.ExpansionAnswerSet;
import edu.uff.dl.rules.expansion.set.IndividualTemplate;
import edu.uff.dl.rules.expansion.set.SampleExpansionAnswerSet;
import edu.uff.dl.rules.util.AlphabetCounter;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.NumericVariableGenerator;
import edu.uff.dl.rules.util.VariableGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.drew.dlprogram.model.Variable;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;

public class App {

    public static String getTime() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
        //System.out.println("");
    }

    public static String getTime(Box<Long> diference) {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        long resp = 0;
        resp += millis;
        resp += second * 1000;
        resp += minute * 60 * 1000;
        resp += hour * 60 * 60 * 1000;
        resp += day * 24 * 60 * 60 * 1000;
        resp += month * 30 * 24 * 60 * 60 * 1000;
        resp += year * 365 * 30 * 24 * 60 * 60 * 1000;

        diference.setContent(resp);

        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
    }

    public static void redirectOutputStrem(String filepath) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(filepath));
        System.setOut(out);
    }
    
    public static void main(String[] args) throws Exception {
        //allExamplesTest();
        //simpleExampleTest(53);
        //simpleExampleTest(0);
        //testDReW();
        testRun();
    }

    public static void testRun() throws FileNotFoundException {
        Set<String> dlpFilepaths = new LinkedHashSet<>();
        String owlFilepath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        String templateFilepath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/template.dlp";
        Set<String> dlpSamplesFilepath = new LinkedHashSet<>();
        Set<String> compareFilepaths = new LinkedHashSet<>();

        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/graphics.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/language.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/misc.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/systems.yap");
        dlpFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/theory.yap");

        dlpSamplesFilepath.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.f");

        compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/train1.n");
        compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.f");
        compareFilepaths.add("/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/fromVSC/test1.n");

        String defaultFilepath = "/Users/Victor/Desktop/out/";
        
        DLRulesRun run;
        int count = 0;
        double min = Double.MAX_VALUE, max = 0, sun = 0, aux = 0;
        int maxR = 0, minR = 0;
        int delay = 250;
        int wait;
        for (int i = 0; i < 78; i++) {
            redirectOutputStrem(defaultFilepath + "rule" + i + ".txt");
            run = new DLRulesRun(dlpFilepaths, owlFilepath, templateFilepath, dlpSamplesFilepath, compareFilepaths);
            run.setOffSet(i);
            run.start();
            try {
                wait = 0;
                while (run.getState() != Thread.State.TERMINATED) {
                    Thread.sleep(1000);
                    wait++;
                    if (wait >= delay) {
                        break;
                    }
                }
                
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

        redirectOutputStrem("statistics.txt");
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
            redirectOutputStrem(filepath);
            avg += simpleExampleTest(i);
        }
        redirectOutputStrem(pathPrefix + "avg.txt");
        System.out.println("Average time: " + (avg / (maxLoop - badExamples.size())));
    }

    public static double simpleExampleTest(int offSet) throws Exception {
        String begin, end;
        Box<Long> b = new Box<>(null), e = new Box(null);
        begin = getTime(b);
        getTime();

        testDReWReasoner(offSet);

        end = getTime(e);
        System.out.println("");
        System.out.println("Begin: " + begin);
        System.out.println("End:   " + end);
        double dif = e.getContent() - b.getContent();
        dif /= 1000;
        System.out.println("Total time: " + dif + "s");
        return dif;
    }

    public static void testDReWReasoner(int offSet) throws ComponentInitException, FileNotFoundException, IOException, org.semanticweb.drew.dlprogram.parser.ParseException {
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
        dr.setOffSet(offSet);
        Set<Constant> individuals = new HashSet<>();
        Set<DataLogPredicate> predicates = new HashSet<>();

        dr.loadIndividualsAndPredicates(individuals, predicates);
        dr.init();

        if (dr.getAnswerSetRules() == null || dr.getAnswerSetRules().size() < 1)
            return;
        AnswerSetRule asr = dr.getAnswerSetRules().get(0);

        String in = dlpContent + "\n" + asr.getRulesAsString();

        System.out.println(asr.getRulesAsString());
        System.out.println("");
        System.out.println("Rule's body: " + asr.getAnswerRule().getRules().iterator().next().getTerms().size() + " literals.");
        System.out.println("");
        System.out.println("Comparar a Regra: " + App.getTime());
        System.out.println("");
        boolean compare = false;
        compare = true;
        if (!compare)
            return;
        DReWRLCLILiteral drew = DReWRLCLILiteral.run(in, dr.getArg());
        Set<Literal> lits = drew.getLiteralModelHandler().getAnswerSets().get(0);

        System.out.println("Verificar exemplos positivos: " + App.getTime());

        int positive = 0;
        for (Literal s : dr.getSamples()) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }
        System.out.println("Exemplos Positivos Cobertos (train1.f) / Total : " + positive + " / " + dr.getSamples().size());
        System.out.println("");
        System.out.println("Verificar exemplos negativos: " + App.getTime());
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
        System.out.println("Verificar exemplos positivos: " + App.getTime());
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
        System.out.println("Verificar exemplos negativos: " + App.getTime());
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

    public static void testCLI() throws ParseException, IOException, ReasoningMethodUnsupportedException {
        String[] arg = {"/Users/Victor/workspace/trunk/examples/arch/arch_owl.conf"};
        CLI.main(arg);
        //uff.dl.rules.CLI.main(arg); //Meu CLI, usando o ParcelPosNegLPRules
        //org.dllearner.cli.CLI.main(arg); //Do DL-Learner
    }

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
