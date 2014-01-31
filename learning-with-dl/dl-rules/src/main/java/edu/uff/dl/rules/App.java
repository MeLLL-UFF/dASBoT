package edu.uff.dl.rules;

/**
 * Hello world!
 *
 */
import java.io.IOException;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;
import org.dllearner.core.ReasoningMethodUnsupportedException;

public class App {

    public static void main(String[] args) throws ParseException, org.dllearner.confparser3.ParseException, IOException, ReasoningMethodUnsupportedException {
        testCLI();
        //test1();
    }
    
    public static void testCLICV() throws org.dllearner.confparser3.ParseException, IOException, ReasoningMethodUnsupportedException {
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

    public static void testCLI() throws org.dllearner.confparser3.ParseException, IOException, ReasoningMethodUnsupportedException {
        String[] arg = {"/Users/Victor/Dropbox/Iniciação Científica/dl/oldConf/facultynearOld.conf"};
        CLI.main(arg);
        //uff.dl.rules.CLI.main(arg); //Meu CLI, usando o ParcelPosNegLPRules
        //org.dllearner.cli.CLI.main(arg); //Do DL-Learner
    }
    
    public static void test1() throws ParseException {
        PrologParser pp = new PrologParser();
        String atomString = "couple('joao', joao).";
        Atom atom = pp.parseAtom(atomString);
        System.out.println(atom);

        for (int i = 0; i < atom.getArity(); i++) {
            System.out.println(atom.getArgument(i).toPLString());
        }
    }
}
