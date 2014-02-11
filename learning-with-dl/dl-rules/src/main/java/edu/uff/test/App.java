package edu.uff.test;

/**
 * Hello world!
 *
 */
import edu.uff.dl.rules.BKRules;
import edu.uff.dl.rules.example.AtomTerm;
import edu.uff.dl.rules.example.PosNegLPRules;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
//import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.confparser3.ParseException;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.prolog.Atom;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import edu.uff.dllearnerUtil.cliUtil.IOUtil;
import java.io.ByteArrayInputStream;
import java.util.Set;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;

public class App {

    public static void main(String[] args) throws ParseException, ParseException, IOException, ReasoningMethodUnsupportedException {
        //testBKRules(true);
        //testCLICV();
        //test1();
        testBeans();
    }

    public static void testCLICV() throws ParseException, IOException, ReasoningMethodUnsupportedException {
        String[] arg = new String[5];
        String path = "/Users/Victor/Dropbox/Iniciação Científica/dl/trainTest/";
        String prefixFile = "facultynear";
        String fullPath = path + prefixFile + "/" + prefixFile;

        arg[0] = "simpleCV";
        arg[1] = fullPath + ".conf";
        arg[2] = fullPath;
        arg[3] = "10";
        arg[4] = fullPath + "Rule.txt";

        CLICV.main(arg);
    }

    public static void testCLI() throws ParseException, IOException, ReasoningMethodUnsupportedException {
        String[] arg = {"/Users/Victor/Dropbox/Iniciação Científica/dl/oldConf/facultynearOld.conf"};
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

    public static void testBKRules(boolean onlyDatalog) {
        String myBK = "/Users/Victor/Desktop/bk.pl";
        String networkBK = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
        String networkOWL = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
        BKRules bk;
        if (onlyDatalog) {
            bk = new BKRules(myBK);
        } else {
            bk = new BKRules(networkBK, networkOWL);
        }

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
        Resource confFileR = new StringSystemResource(notFold, IOUtil.readFile(paths), "UTF-8"); //Works
        
        
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
}