package edu.uff.dllearnerUtil.cliCV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
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
//import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class CliCV extends CLI{
	
	private static Logger logger = LoggerFactory.getLogger(CLI.class);

	private ApplicationContext context;
	private IConfiguration configuration;
	private String prefixExFile;
	private File confFile;
	private int nOfFolds;
	private int nOfInternalFolds = 0;
	private String measure = "accuracy";
	private String outputFile;
	
	private AbstractReasonerComponent rs;
	private AbstractCELA la;
	private PosNegLP lp;
	//private KnowledgeSource ks;
	// some CLI options
	private boolean writeSpringConfiguration = false;
	//some CV options
	private boolean simpleCV = true;
	private boolean internalCV = false;

	public CliCV(){
	}
		
	public CliCV(File confFile, String exFile, int kfolds, String outputVal, String cvType) {
		this.confFile = confFile;
		this.prefixExFile = exFile;
		this.nOfFolds = kfolds;
		this.outputFile = outputVal;
		if (cvType.equalsIgnoreCase("CVAndVal")){
			this.simpleCV = false;
			
		}
	}
	
		
	public CliCV(File confFile, String exFile, int kfolds, String outputVal, String cvType, int xfolds, String evalfn) {
		this(confFile, exFile, kfolds, outputVal, cvType);
		this.nOfInternalFolds = xfolds;
		this.measure = evalfn;
		this.simpleCV = false;
		this.internalCV = true;
	}
	
	// separate init methods, because some scripts may want to just get the application
	// context from a conf file without actually running it
	public void init() throws IOException {    	
	   	if(context == null) {
	   		//ToDo: colocar em um local mais apropriado, senão daqui a pouco está cheio com as classes novas. Pode ser um arquivo separado
	   		//AnnComponentManager.addComponentClassName("edu.uff.dlrules.input.RulesLPStandard");
	   		loadNewComponents();
	   		Resource confFileR = new FileSystemResource(confFile);
	   		List<Resource> springConfigResources = new ArrayList<Resource>();
	   		
	   		try{
	   			configuration = new ConfParserConfiguration(confFileR);
	   			ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
	   			context =  builder.buildApplicationContext(configuration,springConfigResources);	   				   				   			   			
	   		} catch (Exception e) {
	            String stacktraceFileName = "log/error.log";

	            e.printStackTrace();
	            
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
	   		//knowledge source
	   		//ks = context.getBean(KnowledgeSource.class);
	   		// reasoner
            rs = context.getBean(AbstractReasonerComponent.class);
            // learning Algorithm: não queria ter qe usar Axiom learning, pois não aprenderei apenas axiomas
            // tenho que usar por causa dos métodos
			la = context.getBean(AbstractCELA.class);
			// learning problem
			// eu preciso dos métodos dessa classe para configurar os exemplos
			lp = context.getBean(PosNegLP.class);
    	}
	}
	
	public void run() throws IOException {
    	
		if (writeSpringConfiguration) {
        	SpringConfigurationXMLBeanConverter converter = new SpringConfigurationXMLBeanConverter();
        	XmlObject xml;
        	if(configuration == null) {
        		Resource confFileR = new FileSystemResource(confFile);
        		configuration = new ConfParserConfiguration(confFileR);
        		xml = converter.convert(configuration);
        	} else {
        		xml = converter.convert(configuration);
        	}
        	String springFilename = confFile.getCanonicalPath().replace(".conf", ".xml");
        	File springFile = new File(springFilename);
        	if(springFile.exists()) {
        		logger.warn("Cannot write Spring configuration, because " + springFilename + " already exists.");
        	} else {
        		Files.createFile(springFile, xml.toString());
        	}		
		}   
		CrossValidationPreDefinedFolds cv = new CrossValidationPreDefinedFolds(la, lp, rs, prefixExFile);
    	if (simpleCV == true){
    		cv.runCV(nOfFolds, outputFile); 
    	} else if (internalCV == true){
    		cv.runCVInternalVal(nOfFolds, nOfInternalFolds, prefixExFile, measure, outputFile); 
    	} else {
    		cv.runCVWithValSet(nOfFolds, outputFile); 
    	}
		//		
	}
		
	// add new classes to the pool of components
	public static void loadNewComponents(){
		BufferedReader components = null;
		try{
			 InputStream in = CliCV.class.getClassLoader().getResourceAsStream("components.ini");
			 if (in == null)
				 return;
			 components = new BufferedReader(new InputStreamReader(in));
			 if (components != null){
				 String newClass; 
				 while ((newClass = components.readLine()) != null)
					 if (newClass.charAt(0) != '/') 
						 AnnComponentManager.addComponentClassName(newClass);
			 }
		}catch (Exception e){
			System.err.println("Error when opening output file: " + e.getMessage());
		}finally {
			try {
				if (components != null)
					components.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
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
		String evalfn = "", output = "", prefixFile = "", cvType = "";
		File file = null;
		CliCV cli;
		
		
		//for (int i = 0; i < 6; i++){
		//	System.out.println(args[i]);			
		//}
		
		if (args.length < 5) {
			System.out.println("You need to give the type of test (simpleCV, CVAndVal, InternalVal), a conf file, the prefix for examples file, the number of folds as arguments and the file for writing rules.");
			System.exit(0);
		} else if (args.length == 6){
			xfolds = Integer.parseInt(args[5]);
		} else if (args.length ==7){
		    evalfn = args[6];			
		}
		
		// read file and print and print a message if it does not exist	
		file = new File(args[1]);
		if (!file.exists()) {
			System.out.println("Configuration File \"" + file + "\" does not exist.");
			System.exit(0);			
		}
		cvType = args[0];
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
        	if (cvType.equalsIgnoreCase("InternalVal")){
        		cli = new CliCV(file, prefixFile, kfolds, output, "InternalVal", xfolds, evalfn);        		
        	} else {
        		cli = new CliCV(file, prefixFile, kfolds, output, cvType);
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
        if(componentInitExceptionIndex > -1) {
            primaryCause = throwables[componentInitExceptionIndex];
        }else {
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
