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
import edu.uff.dl.rules.util.FileContent;
import edu.uff.dl.rules.util.VariableGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.drew.dlprogram.model.Variable;

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
   
    public static void main(String[] args) throws ParseException, ParseException, IOException, ReasoningMethodUnsupportedException, ComponentInitException, org.dllearner.parser.ParseException, FileNotFoundException, org.semanticweb.drew.dlprogram.parser.ParseException {
        String begin, end;
        begin = getTime();
        getTime();

        //testDReW();
        testDReWReasoner();
        //testSimpleExpansionAnswerSet();
        
        //testIndividualTemplate();
        
        end = getTime();
        System.out.println("");
        System.out.println("Begin: " + begin);
        System.out.println("End:   " + end);
    }

    public static void testIndividualTemplate() throws FileNotFoundException, ComponentInitException {
        
    }
    
    public static void testDReWReasoner() throws ComponentInitException, FileNotFoundException, IOException, org.semanticweb.drew.dlprogram.parser.ParseException {
        DReWReasoner dr;
        String samples;
        String owlFilePath = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        String dlpFilePath = "/Users/Victor/Dropbox/dl.rules/sample.dlp";
        dlpFilePath = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/lattesRules.dlp";
        String samplesFilePath = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/samples.dlp";

        //Arity = 2
        samplesFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/f0.f";
        dlpFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap";
        boolean isNetwork = false;
        //isNetwork = true;
        if (isNetwork) {
            owlFilePath = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
            dlpFilePath = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
            samples = "newnode(x1).\nnewnode(x2).";

        } else {
            samples = FileContent.getStringFromFile(samplesFilePath);
            //samples = "flies(tweety).";
        }
        String dlpContent = FileContent.getStringFromFile(dlpFilePath);
        String templateContent = FileContent.getStringFromFile("/Users/Victor/Desktop/template.dlp");
        dr = new DReWReasoner(owlFilePath, dlpContent, samples, templateContent);
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
        System.out.println("Comparar a Regra: " + App.getTime());
        boolean compare = false;
        //compare = true;
        if (!compare) return;
        DReWRLCLILiteral drew = DReWRLCLILiteral.run(in, dr.getArg());
        Set<Literal> lits = drew.getLiteralModelHandler().getAnswerSets().get(0);

        int positive = 0;
        for (Literal s : dr.getSamples()) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }       
        }
        System.out.println("Exmplos Cobertos / Total : " + positive + " / " + dr.getSamples().size());
        //System.out.println("Gerar arquivo de saída: " + App.getTime());
        //asr.setRulesToFile("/Users/Victor/Desktop/rules.dlp", false);
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
                arg[4] = "";
                //arg[4] = "/Users/Victor/Dropbox/dl.rules/results-drew-rules/lattesRules-with-rule.dlp";
                String samplesFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/f0.f";
                String dlpFilePath = "/Users/Victor/Dropbox/dl.rules/uw-cse-testebinario/ai.yap";
                String out = FileContent.getStringFromFile(dlpFilePath) + FileContent.getStringFromFile(samplesFilePath);
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
