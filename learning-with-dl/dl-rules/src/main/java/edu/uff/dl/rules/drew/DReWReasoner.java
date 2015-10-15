/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.drew;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.datalog.SimplePredicate;
import edu.uff.dl.rules.expansionset.ExampleExpansionAnswerSet;
import edu.uff.dl.rules.expansionset.ExpansionAnswerSet;
import edu.uff.dl.rules.rules.AnswerRule;
import edu.uff.dl.rules.rules.AnswerSetRule;
import edu.uff.dl.rules.template.IndividualTemplate;
import edu.uff.dl.rules.util.DReWDefaultArgs;
import edu.uff.dl.rules.util.FileContent;

import static edu.uff.dl.rules.util.Time.getTime;

import it.unical.mat.wrapper.DLVInvocationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;
import org.semanticweb.drew.dlprogram.model.Predicate;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Class to do all the reasoner to generate the rules. <br>
 * Run the DReW, creates the Expansion Answer Set and generates the rule based
 * on a example.
 *
 * @author Victor Guimarães
 */
@ComponentAnn(name = "DReWReasoner", shortName = "drewreas", version = 0.1)
public class DReWReasoner implements Component {

    public static final String PREFIX_SEPARATOR = "#";

    protected DReWRLCLILiteral drew;
    protected OWLOntology ontology;
    protected Set<Constant> individuals;
    protected Set<DataLogPredicate> predicates;
    protected Set<Literal> examples;
    protected String dlpContent;
    protected String examplesContent;
    protected List<AnswerSetRule> answerSetRules;
    protected String templateContent;
    protected int offset = 0;
    protected List<ConcreteLiteral> examplesForRule;

    protected IndividualTemplate individialTemplate;

    protected boolean recursiveRuleAllowed = true;

    protected int depth;
    
    protected PrintStream outStream;
    
    private String[] args;

    /**
     * Constructor without parameters. Needed to load the class by a file
     * (Spring).
     */
    public DReWReasoner() {
        args = DReWDefaultArgs.ARGS;
        this.individuals = new HashSet<>();
        this.predicates = new HashSet<>();
        this.examples = new HashSet<>();
        answerSetRules = new ArrayList<>();
        depth = 0;
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param owlFilePath the path to the owl file.
     * @param dlpContent the DLP's content.
     * @param examplesContent the examples's content.
     * @param templateContent the template's content.
     */
    public DReWReasoner(String owlFilePath, String dlpContent, String examplesContent, String templateContent, PrintStream outStream) {
        this();
        this.args[2] = owlFilePath;
        this.dlpContent = dlpContent;
        this.examplesContent = examplesContent;
        this.templateContent = templateContent;
        this.outStream = outStream;
    }
    
    public DReWReasoner(String dlvPath, String owlFilePath, String dlpContent, String examplesContent, String templateContent, PrintStream outStream) {
        this(owlFilePath, dlpContent, examplesContent, templateContent, outStream);
        this.args[args.length - 1] = dlvPath;
    }

    /**
     * The method that prepare the class to do the process.
     * <br>DReW is called here.
     *
     * @throws ComponentInitException a possible exception during the process.
     */
    @Override
    public void init() throws ComponentInitException {
        try {
            loadIndividualsAndPredicates(individuals, predicates);

            loadExample(individuals, predicates);
            drew = DReWRLCLILiteral.get(args);
            drew.setDLPContent(getDlpAndExamples());
            drew.go();

            templateContent = (templateContent == null ? "" : templateContent);
            individialTemplate = new IndividualTemplate(templateContent, dlpContent + examplesContent, FileContent.getStringFromFile(getOwlFilePath()));
            individialTemplate.init();
        } catch (ParseException | ComponentInitException | FileNotFoundException ex) {
            Logger.getLogger(DReWReasoner.class.getName()).log(Level.SEVERE, null, ex);
            throw new ComponentInitException(ex.getMessage());
        }
    }

    /**
     * The method that does all the process for a single example.
     * <br>Call {@link #init()} before.
     * <br>This method gets the previously obtained results from DReW by the
     * {@link #init()} and creates the Expansion Answer Set.
     * <br>With the Exampasion Answer Set, it creates a rule based on a example
     * from the examples's content. The example is choosed according with the
     * offset. The offset can be between [0, N) where N is the total number of
     * examples within the givan content.
     */
    public void run() {
        AnswerSetRule aes;
        //loadFromAnswerSet(individuals, predicates);
        ExpansionAnswerSet e;
        try {
            for (Set<Literal> answerSet : drew.getLiteralModelHandler().getAnswerSets()) {
                outStream.println("Iniciar Configuração do Template: " + getTime());

                outStream.println("Iniciar Geração do Conjunto Expandido: " + getTime());
                e = new ExampleExpansionAnswerSet(answerSet, examples, individialTemplate, outStream);
                ((ExampleExpansionAnswerSet) e).setOffset(offset);
                outStream.println("");
                outStream.println(e.getClass());
                outStream.println("");
                //e = s;

                e.init();

                outStream.println("Iniciar Geração da Regra: " + getTime());
                outStream.println("");
                //depth = 1;
                outStream.println("Gerando regra com profundidade de variáveis: " + depth);
                AnswerRule ar = new AnswerRule(e.getExamples(), e.getExpansionSet(), depth, individialTemplate, outStream);
                ar.setRecursive(recursiveRuleAllowed);
                ar.init();
                aes = new AnswerSetRule(e, ar);
                answerSetRules.add(aes);
                examplesForRule = e.getExamples();
            }
        } catch (ComponentInitException ex) {
            Logger.getLogger(DReWReasoner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads all the individuals and predicates from the problem. This method is
     * public to allow that the individuals and predicates be used outside this
     * class.
     *
     * @param individuals a set of individual to load in.
     * @param predicates a set of predicates to load in.
     * @throws FileNotFoundException in case a file path does not exist.
     * @throws ParseException in case a file does not accord with the sintax
     * rules.
     */
    public void loadIndividualsAndPredicates(Set<Constant> individuals, Set<DataLogPredicate> predicates) throws FileNotFoundException, ParseException {
        loadOntology(individuals, predicates);
        loadDLP(individuals, predicates);
        loadExample(individuals, predicates);
        removePredicatesPrefix(predicates);
    }

    /**
     * Remove the web prefix of the predicates.
     *
     * @param predicates the predicates
     */
    private void removePredicatesPrefix(Set<DataLogPredicate> predicates) {
        String head;
        for (DataLogPredicate dataLogPredicate : predicates) {
            head = dataLogPredicate.getPredicate();
            if (head.startsWith("<") && head.endsWith(">")) {
                head = head.substring(head.lastIndexOf(PREFIX_SEPARATOR) + PREFIX_SEPARATOR.length(), head.lastIndexOf(">"));
            }
            dataLogPredicate.setHead(head);
        }
    }

    /**
     * Used by {@link #loadIndividualsAndPredicates(java.util.Set, java.util.Set)
     * } to load only the individual and predicates from the ontology.
     *
     * @param individuals a set of individual to load in.
     * @param predicates a set of predicates to load in.
     */
    private void loadOntology(Set<Constant> individuals, Set<DataLogPredicate> predicates) {
        if (individuals == null || predicates == null)
            return;

        File file = new File(getOwlFilePath());
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try {
            ontology = man.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        Constant c;
        Set<OWLNamedIndividual> individualsInSignature = ontology.getIndividualsInSignature();
        for (OWLNamedIndividual o : individualsInSignature) {
            c = new Constant(o.toString());
            individuals.add(c);
        }

        SimplePredicate sp;
        Set<OWLClass> classInSingnature = ontology.getClassesInSignature();
        for (OWLClass o : classInSingnature) {
            sp = new SimplePredicate(o.toString(), 1);
            predicates.add(sp);
        }

        Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature();
        for (OWLObjectProperty o : objectProperties) {
            sp = new SimplePredicate(o.toString(), 2);
            predicates.add(sp);
        }

        predicates.remove(new SimplePredicate("owl:Thing", 1));
    }

    /**
     * Used by {@link #loadIndividualsAndPredicates(java.util.Set, java.util.Set)
     * } to load only the individual and predicates from the DLP's content.
     *
     * @param individuals a set of individual to load in.
     * @param predicates a set of predicates to load in.
     */
    private void loadDLP(Set<Constant> individuals, Set<DataLogPredicate> predicates) throws FileNotFoundException, ParseException {
        List<ProgramStatement> programs = getProgramStatements(dlpContent);

        Clause c;
        SimplePredicate sp;
        Predicate p;
        NormalPredicate np;

        for (ProgramStatement ps : programs) {
            if (ps.isClause() && (c = ps.asClause()).isFact()) {

                p = c.getHead().getPredicate();
                if (p instanceof NormalPredicate) {
                    np = (NormalPredicate) p;

                    sp = new SimplePredicate(np);
                    predicates.add(sp);

                    for (Term term : c.getHead().getTerms()) {
                        if (term instanceof Constant) {
                            individuals.add((Constant) term);
                        }
                    }
                }

            }
        }

    }

    /**
     * Used by {@link #loadIndividualsAndPredicates(java.util.Set, java.util.Set)
     * } to load only the individual and predicates from the examples's content.
     *
     * @param individuals a set of individual to load in.
     * @param predicates a set of predicates to load in.
     */
    private void loadExample(Set<Constant> individuals, Set<DataLogPredicate> predicates) throws ParseException {
        loadExamplesLiterals();
        Predicate p;
        NormalPredicate np;
        SimplePredicate sp;
        for (Literal l : examples) {
            p = l.getPredicate();
            if (p instanceof NormalPredicate) {
                np = (NormalPredicate) p;

                sp = new SimplePredicate(np);
                predicates.add(sp);
                for (Term term : l.getTerms()) {
                    if (term instanceof Constant) {
                        individuals.add((Constant) term);
                    }
                }
            }
        }
        removePredicatesPrefix(predicates);
    }

    /**
     * Used by {@link #loadIndividualsAndPredicates(java.util.Set, java.util.Set)
     * } to load only the individual and predicates from the DReW's answer.
     *
     * @param individuals a set of individual to load in.
     * @param predicates a set of predicates to load in.
     */
    private void loadFromAnswerSet(Set<Constant> individuals, Set<DataLogPredicate> predicates) {

        Predicate p;
        NormalPredicate np;
        SimplePredicate sp;

        for (Set<Literal> set : drew.getLiteralModelHandler().getAnswerSets()) {
            for (Literal l : set) {
                p = l.getPredicate();
                if (p instanceof NormalPredicate) {
                    np = (NormalPredicate) p;

                    sp = new SimplePredicate(np);
                    predicates.add(sp);
                    for (Term term : l.getTerms()) {
                        if (term instanceof Constant) {
                            individuals.add((Constant) term);
                        }
                    }
                }
            }
        }

    }

    /**
     * Load the literals from the examples's content.
     *
     * @throws ParseException in case a file does not accord with the sintax
     * rules.
     */
    private void loadExamplesLiterals() throws ParseException {
        List<ProgramStatement> programs = getProgramStatements(examplesContent);
        Clause c;
        Literal l;

        for (ProgramStatement ps : programs) {
            if (ps.isClause() && (c = ps.asClause()).isFact()) {
                l = new Literal(c.getHead().getPredicate(), c.getHead().getTerms());
                examples.add(l);
            }
        }
    }

    /**
     * Gets a list of {@link ProgramStatement} from a {@link String}. It means,
     * transforme the content from a {@link String} into Java classes of the
     * problem.
     *
     * @param content the content.
     * @return a list of {@link ProgramStatement}.
     * @throws ParseException in case a file does not accord with the sintax
     * rules.
     */
    private List<ProgramStatement> getProgramStatements(String content) throws ParseException {
        DLProgramKB kb = new DLProgramKB();
        kb.setOntology(ontology);
        DLProgram elprogram = null;

        DLProgramParser parser;

        Reader reader;

        reader = new StringReader(content);

        parser = new DLProgramParser(reader);

        if (ontology != null)
            parser.setOntology(ontology);
        elprogram = parser.program();
        kb.setProgram(elprogram);
        return elprogram.getStatements();
    }

    /**
     * Getter for the DLP's content plus the examples's content.
     *
     * @return the DLP's content plus the examples's content.
     */
    private String getDlpAndExamples() {
        return dlpContent + examplesContent;
    }

    /**
     * Setter for the DLP's content.
     *
     * @param dlpContent the DLP's content.
     */
    public void setDlpContent(String dlpContent) {
        this.dlpContent = dlpContent;
    }

    /**
     * Setter for the examples's content.
     *
     * @param examplesContent the examples's content.
     */
    public void setExamplesContent(String examplesContent) {
        this.examplesContent = examplesContent;
    }

    /**
     * Getter for the individuals of the problem.
     *
     * @return the individuals of the problem.
     */
    public Set<Constant> getIndividuals() {
        return individuals;
    }

    /**
     * Getter for the predicates of the problem.
     *
     * @return the predicates of the problem.
     */
    public Set<DataLogPredicate> getPredicates() {
        return predicates;
    }

    /**
     * Getter for the examples of the problem.
     *
     * @return the examples of the problem.
     */
    public Set<Literal> getExamples() {
        return examples;
    }

    /**
     * Getter for DLP's content of the problem.
     *
     * @return the DLP's content of the problem.
     */
    public String getDlpContent() {
        return dlpContent;
    }

    /**
     * Getter for examples's content of the problem.
     *
     * @return the examples's content of the problem.
     */
    public String getExamplesContent() {
        return examplesContent;
    }

    /**
     * Getter for {@link AnswerSetRule} of the problem. It is a pair of a
     * {@link ExpansionAnswerSet} and a {@link AnswerRule}. This class generates
     * one of this pairs on each iteration ({@link #run()} call).
     *
     * @return the {@link AnswerSetRule} of the problem.
     */
    public List<AnswerSetRule> getAnswerSetRules() {
        return answerSetRules;
    }

    /**
     * Getter for the owl's filepath of the problem.
     *
     * @return the owl's filepath of the problem.
     */
    public String getOwlFilePath() {
        return args[2];
    }

    /**
     * Setter for the owl filepath.
     *
     * @param owlFilePath the owl filepath.
     */
    public void setOwlFilePath(String owlFilePath) {
        args[2] = owlFilePath;
    }

    /**
     * Getter for the command line arguments of the problem.
     *
     * @return the command line arguments of the problem.
     */
    public String[] getArg() {
        return args;
    }

    /**
     * Getter for template's content of the problem.
     *
     * @return the template's content of the problem.
     */
    public String getTemplateContent() {
        return templateContent;
    }

    /**
     * Setter for the template's content.
     *
     * @param templateContent the template's content.
     */
    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    /**
     * Getter for the offset of the problem.
     *
     * @return the offset of the problem.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Setter for the offset.
     *
     * @param offset the offset.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Method to kill the DLV's execution. <br>
     * Usiful when the program takes to long to be executed and exceeds the
     * problem's timeout.
     *
     * @throws DLVInvocationException a possible exception during the DLV
     * invocation.
     */
    public void killDLV() throws DLVInvocationException {
        if (drew != null) {
            drew.killDLV();
        }
    }

    /**
     * Getter for the examples which was proved by the generated rule.
     *
     * @return the examples for the generated rule.
     */
    public List<ConcreteLiteral> getExamplesForRule() {
        return examplesForRule;
    }

    /**
     * Getter for the {@link #recursiveRuleAllowed}. It is true if recursion is
     * allowed at the rule, false otherwise.
     * <br> It is true by default.
     *
     * @return the {@link #recursiveRuleAllowed}.
     */
    public boolean isRecursiveRuleAllowed() {
        return recursiveRuleAllowed;
    }

    /**
     * Setter for the {@link #recursiveRuleAllowed}. Set true to allow the
     * recursion, false to do not.
     * <br> It is true by default.
     *
     * @param recursiveRuleAllowed the {@link #recursiveRuleAllowed}.
     */
    public void setRecursiveRuleAllowed(boolean recursiveRuleAllowed) {
        this.recursiveRuleAllowed = recursiveRuleAllowed;
    }

    /**
     * Getter for the depth of the transitivity on the Expansion Answer Set.
     *
     * @return the depth of the transitivity.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Setter for the depth of the transitivity on the Expansion Answer Set.
     *
     * @param depth the depth of the transitivity.
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    public PrintStream getOutStream() {
        return outStream;
    }

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }
    
}
