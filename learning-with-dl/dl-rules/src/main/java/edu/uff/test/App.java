package edu.uff.test;

/**
 * Hello world!
 *
 */
import edu.uff.dl.rules.BKRules;
import java.io.IOException;
//import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.confparser3.ParseException;
import org.dllearner.prolog.Atom;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.semanticweb.drew.dlprogram.model.Clause;

public class App {

    public static void main(String[] args) throws ParseException, ParseException, IOException, ReasoningMethodUnsupportedException {
        testBKRules(true);
        //testCLICV();
        //test1();
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
}
