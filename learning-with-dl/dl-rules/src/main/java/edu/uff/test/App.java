/*
 * UFF Project Semantic Learning
 */
package edu.uff.test;

/**
 * Hello world!
 *
 */
import edu.uff.dl.rules.BKRules;
import edu.uff.dl.rules.HeadPredicate;
import edu.uff.dl.rules.SimplePredicate;
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
import edu.uff.drew.DReWRLCLI;
import edu.uff.drew.DReWRLCLILiteral;
import edu.uff.drew.ExpansionAnswerSet;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import static org.dllearner.core.OntologyFormat.KB;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.core.owl.KB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;

public class App {

    public static void main(String[] args) throws ParseException, ParseException, IOException, ReasoningMethodUnsupportedException, ComponentInitException, org.dllearner.parser.ParseException {
        //testBKRules(false);
        //testCLICV();
        //testCLI();
        //test1();
        //testBeans();
        testDReW();
        //testOWLFile();
        //testPermutate();
    }
    
    public static void testPermutate() {
        String[] indS = {"Joao", "Maria", "Jose", "bird" };
        Set<String> ind = new HashSet<>();
        
        for (int i = 0; i < indS.length; i++) {
            ind.add(indS[i]);
        }
       
        ExpansionAnswerSet e = new ExpansionAnswerSet();
        int permute = 3;
        List<List<Term>> resp = e.permuteIndividuals(ind, permute);
        
        for (List<Term> list : resp) {
            System.out.print("{ ");
            for (Term term : list) {
                System.out.print(term);
                System.out.print(" ");
            }
            System.out.println("}");
        }
        System.out.println("Count: " + resp.size());
        System.out.println((resp.size() == Math.pow(indS.length, permute) ? "Ok." : "Error."));
    }

    public static void testOWLFile() throws ComponentInitException, org.dllearner.parser.ParseException {
        File file = new File("/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        try {
            ontology = man.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

//        Set<OWLAxiom> axioms = ontology.getABoxAxioms(true);
        ontology.getIndividualsInSignature();
        ontology.getRBoxAxioms(true);

        for (Object o : ontology.getClassesInSignature()) {
            System.out.println(o.toString());
        }
        System.out.println("\n");

        for (Object o : ontology.getIndividualsInSignature()) {
            System.out.println(o.toString());
        }
        System.out.println("\n");
        
        for (Object o : ontology.getObjectPropertiesInSignature()) {
            System.out.println(o.toString());
        }
        System.out.println("\n");
        
        /*
        for (Object o : ontology.getSignature()) {
            System.out.println(o.toString());
        }
        System.out.println("\n");
        */
        
        /*
         for (OWLAxiom a : axioms) {
         Set<OWLNamedIndividual> individual = ontology.getIndividualsInSignature();
         for (OWLNamedIndividual i : individual) {
         System.out.println(i.toString());
         }
         System.out.println(a + "\n");
         //System.out.println(a.toString());
         }
         */
    }

    public static void testDReW() {
        String[] arg = new String[7];
        arg[0] = "-rl";
        arg[1] = "-ontology";
        arg[2] = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
        arg[2] = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        arg[3] = "-dlp";
        arg[4] = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
        arg[4] = "/Users/Victor/Dropbox/dl.rules/sample.dlp";
        arg[5] = "-dlv";
        arg[6] = "/usr/lib/dlv.i386-apple-darwin-iodbc.bin";
        boolean printMySets = true;
        //String kb = "/Users/Victor/Desktop/kb.pl";
        try {
            //testDLProgram(arg[2], arg[4]);
            //testDLProgram(arg[2], kb);

            if (printMySets) {
                System.out.println("My Sets");
                DReWRLCLILiteral d = DReWRLCLILiteral.run(arg);
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
                    Set<String> ind = new HashSet<>();
                    ind.add("tweety");
                    ind.add("polly");
                    
                    Set<HeadPredicate> pred = new HashSet<>();
                    pred.add(new SimplePredicate("penguin", 1));
                    pred.add(new SimplePredicate("bird", 1));
                    pred.add(new SimplePredicate("flies", 1));
                    
                    ExpansionAnswerSet e = new ExpansionAnswerSet(l, ind, pred);
                    //e.init();
                    System.out.println(e);
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
