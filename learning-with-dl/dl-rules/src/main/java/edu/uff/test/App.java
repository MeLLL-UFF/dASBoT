/*
 * UFF Project Semantic Learning
 */
package edu.uff.test;

/**
 * Hello world!
 *
 */
import edu.uff.util.StringResource;
import edu.uff.dl.rules.BKRules;
import edu.uff.dl.rules.example.AtomTerm;
import edu.uff.dl.rules.example.PosNegLPRules;
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
import edu.uff.drew.DReWRLCLILiteral;
import edu.uff.expansion.set.ConcreteDataLogPredicate;
import edu.uff.expansion.set.DataLogLiteral;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;

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

    public static void main(String[] args) throws ParseException, ParseException, IOException, ReasoningMethodUnsupportedException, ComponentInitException, org.dllearner.parser.ParseException {
        String begin, end;
        begin = getTime();
        getTime();

        //testBKRules(false);
        //testCLICV();
        //testCLI();
        //test1();
        //testBeans();
        testDReW();
        System.out.println("");
        System.out.println("");
        testDReWReasoner();

        end = getTime();
        System.out.println("");
        System.out.println("Begin: " + begin);
        System.out.println("End:   " + end);
    }

    public static void testDataLogLiteral(Set<Literal> lits) {
        ConcreteDataLogPredicate dll;
        for (Literal l : lits) {
            dll = DataLogLiteral.getInstanceFromLiteral(l);
            System.out.println(dll);
        }
    }

    public static void testDReWReasoner() throws ComponentInitException, FileNotFoundException {
        String owlFilePath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        String dlpFilePath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        dlpFilePath = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/lattesRules.dlp";
        Set<Literal> samples = new HashSet<>();

        boolean isNetwork = false;
        //isNetwork = true;
        if (isNetwork) {
            owlFilePath = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
            dlpFilePath = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
            samples.add(new Literal("newnode", new Constant("x1")));
            samples.add(new Literal("newnode", new Constant("x2")));
        } else {
            //samples.add(new Literal("flies", new Constant("tweety")));
            //samples.add(new Literal("flies", new Constant("ollie")));
            //samples.add(new Literal("flies", new Constant("juju")));
            //strong(r168).
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r168")));
            samples.add(new Literal("strong", new Constant("r235")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
            samples.add(new Literal("strong", new Constant("r373")));
            samples.add(new Literal("strong", new Constant("r264")));
            samples.add(new Literal("strong", new Constant("r129")));
            samples.add(new Literal("strong", new Constant("r30")));
            samples.add(new Literal("strong", new Constant("r254")));
            samples.add(new Literal("strong", new Constant("r33")));
            samples.add(new Literal("strong", new Constant("r74")));
            samples.add(new Literal("strong", new Constant("r46")));
            samples.add(new Literal("strong", new Constant("r43")));
            samples.add(new Literal("strong", new Constant("r173")));
            samples.add(new Literal("strong", new Constant("r58")));
            samples.add(new Literal("strong", new Constant("r348")));
            samples.add(new Literal("strong", new Constant("r237")));
            samples.add(new Literal("strong", new Constant("r415")));
            samples.add(new Literal("strong", new Constant("r124")));
            samples.add(new Literal("strong", new Constant("r117")));
            samples.add(new Literal("strong", new Constant("r184")));
            samples.add(new Literal("strong", new Constant("r121")));
            samples.add(new Literal("strong", new Constant("r193")));
            samples.add(new Literal("strong", new Constant("r380")));
            samples.add(new Literal("strong", new Constant("r44")));
        }

        Scanner in = new Scanner(new File(dlpFilePath));
        StringBuilder sb = new StringBuilder();
        while (in.hasNext()) {
            sb.append(in.nextLine());
            sb.append("\n");
        }

        DReWReasoner dr = new DReWReasoner(owlFilePath, sb.toString(), false, samples);
        //DReWReasoner dr = new DReWReasoner(owlFilePath, dlpFilePath, true, samples);
        dr.init();
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
                arg[2] = "/Users/Victor/Dropbox/dl.rules/sample.owl";
                arg[4] = "/Users/Victor/Dropbox/dl.rules/sample.dlp";
                arg[4] = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/lattesRules-with-rule.dlp";

                //arg[4] = "/Users/Victor/Desktop/lattesRules-with-rule.dlp";
                System.out.println("My Sets");
                DReWRLCLILiteral d = DReWRLCLILiteral.run(arg);
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

    public static void test1() throws ParseException, org.dllearner.parser.ParseException {
        PrologParser pp = new PrologParser();
        String atomString = "couple('joao', joao).";
        Atom atom = pp.parseAtom(atomString);
        System.out.println(atom);

        for (int i = 0; i < atom.getArity(); i++) {
            System.out.println(atom.getArgument(i).toPLString());
        }
    }

    public static void testBKRules(boolean onlyDatalog) throws ComponentInitException {
        String myBK = "/Users/Victor/Dropbox/dl.rules/bk.pl";
        String networkBK = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
        String networkOWL = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
        //String archKB = "/Users/Victor/workspace/dllearner/examples/arch/arch.kb";
        //String archKB = "/Users/Victor/workspace/dllearner/examples/arch/arch.kb";
        //String archOWL = "/Users/Victor/workspace/dllearner/examples/arch/arch.owl";
        //String archOWL = "/Users/Victor/workspace/dllearner/examples/arch/arch.owl";
        BKRules bk;
        if (onlyDatalog) {
            bk = new BKRules(myBK);
        } else {
            bk = new BKRules(networkBK, networkOWL);
        }
        bk.init();
        if (bk.getFacts() != null) {
            System.out.println("Facts:");
            for (Clause c : bk.getFacts()) {
                System.out.println(c);
            }
        }

        if (bk.getRules() != null) {
            System.out.println("");
            System.out.println("Rules:");
            for (Clause c : bk.getRules()) {
                System.out.println(c);
            }
        }
    }

    public static void testBeans() throws IOException {

        PosNegLP lp;
        IConfiguration configuration;
        ApplicationContext context;

        String initialConf = "/Users/Victor/Dropbox/dl.rules/initialConf.txt";
        String fold = "/Users/Victor/Dropbox/dl.rules/fold.txt";
        String notFold = "/Users/Victor/Dropbox/dl.rules/notFold.txt";
        String paths[] = {initialConf, fold};

        AnnComponentManager.addComponentClassName(PosNegLPRules.class.getName());
        //Resource confFileR = new FileSystemResource(IOUtil.stringToFile(IOUtil.readFile(paths), "fold")); //Works
        // /var/folders/zp/xnfn64fx5ln8x0mt4qfvld8w0000gn/T
        Resource confFileR = new StringResource(notFold, IOUtil.readFile(paths), "UTF-8"); //Works

        List<Resource> springConfigResources = new ArrayList<>();
        configuration = new ConfParserConfiguration(confFileR); //Works
        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
        context = builder.buildApplicationContext(configuration, springConfigResources);

        lp = context.getBean(PosNegLP.class);

        System.out.println("Positivo");
        Set<AtomTerm> s = ((PosNegLPRules) lp).getPositiveAtoms();
        for (AtomTerm atom : s) {
            System.out.println(atom.toString());
        }

        System.out.println("Negativo");
        System.out.println("");
        s = ((PosNegLPRules) lp).getNegativeAtoms();
        for (AtomTerm atom : s) {
            System.out.println(atom.toString());
        }

        System.out.println("");
        System.out.println("Done!");
    }

    public static void print(String... strings) {
        for (String s : strings) {
            System.out.println(s);
        }
    }
}
