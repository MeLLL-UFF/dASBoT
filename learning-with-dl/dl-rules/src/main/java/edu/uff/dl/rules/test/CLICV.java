package edu.uff.dl.rules.test;

import edu.uff.dl.rules.util.AtomTerm;
import edu.uff.dl.rules.util.BKRules;
import edu.uff.dl.rules.util.KBReader;
import edu.uff.dl.rules.util.PosNegLPRules;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.xmlbeans.XmlObject;
import org.dllearner.cli.CLI;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.configuration.util.SpringConfigurationXMLBeanConverter;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.confparser3.ParseException;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

// CLI is from DL Learner
public class CLICV extends CLI {

    private static Logger logger = LoggerFactory.getLogger(CLI.class);

    private ApplicationContext context;
    private IConfiguration configuration;
    private String prefixExFile;
    private File confFile;
    private int nOfFolds;
    //in case you want to use internal cross validation to, eg., set parameters
    private int nOfInternalFolds = 0;
    private String measure = "accuracy";
    private String outputFile;

    //private LearningAlgorithm algorithm;
    private KnowledgeSource knowledgeSource;
    private BKRules rules;
    
    private AbstractReasonerComponent rs;
    private AbstractCELA la;
    // TODO usar a classe pai, para permitir que PosOnly (e outros) tb possam ser usados
    private PosNegLP lp;

    // some CLI options
    private boolean writeSpringConfiguration = false;
    private boolean simpleCV = true;
    private boolean internalCV = false;

    // constructor: no internal CV	
    public CLICV(File confFile, String exFile, int kfolds, String outputVal, String algoType) {
        this.confFile = confFile;
        this.prefixExFile = exFile;
        this.nOfFolds = kfolds;
        this.outputFile = outputVal;
        if (algoType.equalsIgnoreCase("CVAndVal")) {
            this.simpleCV = false;

        }
    }

    // constructor: internal CV
    public CLICV(File confFile, String exFile, int kfolds, int xfolds, String measure, String outputVal, String algoType) {
        this(confFile, exFile, kfolds, outputVal, algoType);
        this.nOfInternalFolds = xfolds;
        this.measure = measure;
        this.outputFile = outputVal;
        this.simpleCV = false;
        this.internalCV = true;
    }

    // separate init methods, because some scripts may want to just get the application
    // context from a conf file without actually running it
    public void init() throws IOException {
        if (context == null) {
            AnnComponentManager.addComponentClassName(PosNegLPRules.class.getName());
            AnnComponentManager.addComponentClassName(KBReader.class.getName());
            AnnComponentManager.addComponentClassName(BKRules.class.getName());
            Resource confFileR = new FileSystemResource(confFile);
            List<Resource> springConfigResources = new ArrayList<Resource>();

            try {
                configuration = new ConfParserConfiguration(confFileR);
                ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
                context = builder.buildApplicationContext(configuration, springConfigResources);

            } catch (Exception e) {
                String stacktraceFileName = "log/error.log";

//	            e.printStackTrace();
                //Find the primary cause of the exception.
                Throwable primaryCause = findPrimaryCause(e);

                // Get the Root Error Message
                logger.error("An Error Has Occurred During Processing.");
                logger.error(primaryCause.getMessage());
                logger.debug("Stack Trace: ", e);
                logger.error("Terminating DL-Learner...and writing stacktrace to: " + stacktraceFileName);
                FileOutputStream fos = new FileOutputStream(stacktraceFileName);
                PrintStream ps = new PrintStream(fos);
                e.printStackTrace(ps);
            }

            //rules = context.getBean(BKRules.class);
            
            //knowledgeSource = context.getBean(KnowledgeSource.class);
            
            rs = context.getBean(AbstractReasonerComponent.class);
            
            //BKRules bkRules = new BKRules("/Users/Victor/Dropbox/Iniciação Científica/dl/lattesRules.kb");
            //KBReader reader = new KBReader(bkRules);
            
            //rs = new FastInstanceChecker(reader);
            
            
            la = context.getBean(AbstractCELA.class);

            //TODO Parcel e também posonly
            //this test is added for PDLL algorithm since it does not use the PosNegLP			
            //try {
            //	lp = (ParCELPosNegLP)context.getBean(ParCELPosNegLP.class);
            //}
            //catch (BeansException be) {
            lp = context.getBean(PosNegLP.class);

            System.out.println("Positivo");
            Set<AtomTerm> s = ((PosNegLPRules) lp).getPositiveAtoms();
            for (AtomTerm atom : s) {
                System.out.println(atom.toString());
            }

            System.out.println("Negativo");
            s = ((PosNegLPRules) lp).getNegativeAtoms();
            for (AtomTerm atom : s) {
                System.out.println(atom.toString());
            }
            //}
        }
    }

    public void run() throws IOException {

        if (writeSpringConfiguration) {
            SpringConfigurationXMLBeanConverter converter = new SpringConfigurationXMLBeanConverter();
            XmlObject xml;
            if (configuration == null) {
                Resource confFileR = new FileSystemResource(confFile);
                configuration = new ConfParserConfiguration(confFileR);
                xml = converter.convert(configuration);
            } else {
                xml = converter.convert(configuration);
            }
            String springFilename = confFile.getCanonicalPath().replace(".conf", ".xml");
            File springFile = new File(springFilename);
            if (springFile.exists()) {
                logger.warn("Cannot write Spring configuration, because " + springFilename + " already exists.");
            } else {
                Files.createFile(springFile, xml.toString());
            }
        }
        CrossValidationFromFiles cv = new CrossValidationFromFiles(la, lp, rs, prefixExFile);
        if (simpleCV == true) {
            cv.runCVFromFiles(nOfFolds, outputFile);
        } else if (internalCV == true) {
            cv.runCVInternalVal(nOfFolds, nOfInternalFolds, prefixExFile, measure, outputFile);
        } else {
            cv.runCVFromFilesWithValSet(nOfFolds, outputFile);
        }
        //		
    }

    /**
     * @param args
     * @throws ParseException
     * @throws IOException
     * @throws ReasoningMethodUnsupportedException
     */
    public static void main(String[] args) throws ParseException, IOException, ReasoningMethodUnsupportedException {

        // usage
        // arg[0] = type of the test: simpleCV, CVandVal or InternalVal
        // arg[0] = conffile; arg[1] = prefix for examples files, arg[2] = number of kfolds
        // arg[3] = number of x folds; arg[4] = measure (accuracy or fmeasure), to decide the best description for
        // validation set
        // arg[5] = outputfile for validation results
        // currently, CLI has two parameters - the conf file and the prefix for examples file
        int xfolds = 0, kfolds = 0;
        String measure = "", output = "", prefixFile = "", algoType = "";
        File file = null;
        CLICV cli;

        //for (int i = 0; i < 6; i++){
        //	System.out.println(args[i]);			
        //}
        if (args.length < 5) {
            System.out.println("You need to give the type of test (simpleCV, CVAndVal, InternalVal), a conf file, the prefix for examples file, the number of folds as arguments and the file for writing rules.");
            System.exit(0);
        } else if (args.length == 6) {
            xfolds = Integer.parseInt(args[5]);
        } else if (args.length == 7) {
            measure = args[6];
        }

        // read file and print and print a message if it does not exist	
        file = new File(args[1]);
        if (!file.exists()) {
            System.out.println("Configuration File \"" + file + "\" does not exist.");
            System.exit(0);
        }
        algoType = args[0];
        prefixFile = args[2];
        kfolds = Integer.parseInt(args[3]);
        output = args[4];

        /*System.out.println(args[args.length - 3]);
         System.out.println(args[args.length - 2]);
         System.out.println(args[args.length - 1]);*/
        //System.out.println(args[1]);
        //List<Resource> springConfigResources = new ArrayList<Resource>();
        try {
            //DL-Learner Configuration Object
            //IConfiguration configuration = new ConfParserConfiguration(confFile);

            //ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
            //ApplicationContext context =  builder.buildApplicationContext(configuration,springConfigResources);
            // TODO: later we could check which command line interface is specified in the conf file
            // for now we just use the default one
            if (algoType.equalsIgnoreCase("InternalVal")) {
                cli = new CLICV(file, prefixFile, kfolds, xfolds, measure, output, "InternalVal");
            } else {
                cli = new CLICV(file, prefixFile, kfolds, output, algoType);
            }

            cli.init();
            cli.run();

        } catch (Exception e) {
            String stacktraceFileName = "log/error.log";

//            e.printStackTrace();
            //Find the primary cause of the exception.
            //Throwable primaryCause = findPrimaryCause(e);
            // Get the Root Error Message
            logger.error("An Error Has Occurred During Processing.");
            //logger.error(primaryCause.getMessage());
            logger.debug("Stack Trace: ", e);
            logger.error("Terminating DL-Learner...and writing stacktrace to: " + stacktraceFileName);
            FileOutputStream fos = new FileOutputStream(stacktraceFileName);
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
        }

    }

    /**
     * Find the primary cause of the specified exception.
     *
     * @param e The exception to analyze
     * @return The primary cause of the exception.
     */
    private static Throwable findPrimaryCause(Exception e) {
        // The throwables from the stack of the exception
        Throwable[] throwables = ExceptionUtils.getThrowables(e);

        //Look For a Component Init Exception and use that as the primary cause of failure, if we find it
        int componentInitExceptionIndex = ExceptionUtils.indexOfThrowable(e, ComponentInitException.class);

        Throwable primaryCause;
        if (componentInitExceptionIndex > -1) {
            primaryCause = throwables[componentInitExceptionIndex];
        } else {
            //No Component Init Exception on the Stack Trace, so we'll use the root as the primary cause.
            primaryCause = ExceptionUtils.getRootCause(e);
        }
        return primaryCause;
    }

    /* teste


     IConfiguration configuration;
     File confFile = new File("/home/aline/research/software/dl-learner/dllearner-1.0-beta-2/examples/father.conf");
		
     Resource confFileR = new FileSystemResource(confFile);
     List<Resource> springConfigResources = new ArrayList<Resource>();
     configuration = new ConfParserConfiguration(confFileR);

     ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
     context =  builder.buildApplicationContext(configuration,springConfigResources);
        
     //List<String> lst = new ArrayList<String>(); //esse <String> é para dizer que o tipo de ArrayList é uma String. Assim não precisa fazer casting
     //lst.add("a");
     //String s = lst.get(0);
     //for (String s : lst) {}  //itera nos elementos de lst
		        
        
     LearningAlgorithm alg = context.getBean(LearningAlgorithm.class);
        
     alg.start();
     // faz um for no conjunto dos tipos de algoritmos, que foi definido no conf. (alg.type)
     // context.... retorna map. EntrySet é para permitir iterar sobre os elementos
     //        for(Entry<String, LearningAlgorithm> entry : context.getBeansOfType(LearningAlgorithm.class).entrySet()){
     //        	algorithm = entry.getValue();
     //			logger.info("Running algorithm instance \"" + entry.getKey() + "\" (" + algorithm.getClass().getSimpleName() + ")");
     //			algorithm.start();
     //		}*/
}
