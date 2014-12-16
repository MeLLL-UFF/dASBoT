/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.template;

import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.datalog.SimplePredicate;
import static edu.uff.dl.rules.drew.DReWReasoner.PREFIX_SEPARATOR;
import java.io.FileNotFoundException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;
import org.semanticweb.drew.dlprogram.model.Predicate;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Class that typifies the individuals from a specific problem.
 * <br> This class is very useful when you know which type of individuals fits
 * in a especific predicate. This may reduces the permutation possibilities
 * dramatically, making the creation of the {
 *
 * @ling ExpansionAnswerSet} much faster.
 *
 * @author Victor Guimarães
 */
public class IndividualTemplate implements TypeTemplate, Component {

    protected String templateContent;
    protected String dlpContent;
    protected String owlContent;
    protected OWLOntology ontology;

    protected Set<Constant> individuals;
    protected Map<String, Set<? extends Constant>> individualsGroups;
    protected Set<DataLogPredicate> predicates;

    protected Set<Clause> singleFacts;
    protected Set<Clause> repeatedFacts;

    protected Map<String, List<List<TermType>>> constantsMap;
    protected Map<String, Set<Clause>> factsMap;

    /**
     * Constructor that only allocate the variables. Using this constructor will
     * be needed set other variables before to call {@link #init()}.
     */
    public IndividualTemplate() {
        this.singleFacts = new LinkedHashSet<>();
        this.repeatedFacts = new LinkedHashSet<>();
        this.individualsGroups = new HashMap<>();
        this.individuals = new LinkedHashSet<>();
        this.predicates = new LinkedHashSet<>();
        this.constantsMap = new HashMap<>();
        this.factsMap = new HashMap<>();
    }

    /**
     * Constructor with all needed parameters.
     *
     * @param templateContent the template's file content.
     * @param dlpContent the DLP's content.
     * @param owlContent the owl's content.
     */
    public IndividualTemplate(final String templateContent, final String dlpContent, final String owlContent) {
        this();
        this.templateContent = templateContent;
        this.dlpContent = dlpContent;
        this.owlContent = owlContent;
        /*
         if (templateContent != null) {
         this.templateContent = templateContent.replace("#", "");
         }
         */
    }

    /**
     * Method which does all the necessary process to prepare this classe to be
     * used by {@link ExpansionAnswerSet}.
     *
     * @throws ComponentInitException a possible exception during the process.
     */
    @Override
    public void init() throws ComponentInitException {
        Set<Clause> clauses;
        DLProgram program;
        try {
            clauses = getClausesFromTemplate();
            program = getDLProgram(dlpContent, owlContent);
            loadClauses(clauses);
            loadIndividualsAndPredicates(this.individuals, predicates, program);
            Set<Constant> localIndividuals = new HashSet(individuals);
            loadIndividualsGroups(program, localIndividuals);
            loadConstants();
        } catch (Exception ex) {
            throw new ComponentInitException(ex.getMessage());
        }

    }

    /**
     * Creates the {@link Map} of constants.
     */
    private void loadConstants() {
        if (templateContent == null)
            return;
        String[] auxs = templateContent.replace('.', ';').split(";");
        Set<String> lines = new LinkedHashSet<>(auxs.length);
        for (String aux : auxs) {
            if (!aux.isEmpty() && aux.indexOf("#") > 0) {
                lines.add(aux);
            }
        }

        String terms[];
        String key;
        List<TermType> list;
        for (String line : lines) {
            terms = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(",");
            list = new ArrayList<>(terms.length);
            key = line.substring(0, line.indexOf("(")).trim();
            if (constantsMap.get(key) == null) {
                constantsMap.put(key, new LinkedList<List<TermType>>());
            }
            for (String term : terms) {
                list.add(new TermType(term.trim()));
            }
            constantsMap.get(key).add(list);
        }
    }

    /**
     * Get the clauses from the template.
     *
     * @return a {@link Set} of {@link Clause} extracted from the template.
     * @throws Exception in case the template does not to follow the language
     * rules.
     */
    private Set<Clause> getClausesFromTemplate() throws Exception {
        Set<Clause> resp = new LinkedHashSet<>();
        try {
            DLProgram program = getDLProgram(templateContent.replace("#", ""));

            Clause c;
            for (ProgramStatement ps : program.getStatements()) {
                if (ps.isClause()) {
                    c = ps.asClause();
                    if (!c.isFact()) {
                        throw new Exception("Invalid template format!");
                    }
                    resp.add(c);
                }
            }

            return resp;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Loads the clauses ({@link Clause}) separating into the ones which are
     * repeated and the ones which are not.
     *
     * @param clauses the clauses that will be loaded.
     */
    private void loadClauses(Set<Clause> clauses) {
        Set<Predicate> singlePred = new LinkedHashSet<>();
        Set<Predicate> repeatedPred = new LinkedHashSet<>();

        Predicate p;
        Clause c;

        for (Clause cla : clauses) {
            //repeatedClause.add(c);
            p = cla.getHead().getPredicate();
            if (!singlePred.contains(p)) {
                singlePred.add(p);
                singleFacts.add(cla);
            } else {
                repeatedPred.add(p);
            }
        }

        Iterator<Clause> it;
        for (Predicate pred : repeatedPred) {
            it = singleFacts.iterator();
            while (it.hasNext()) {
                c = it.next();
                if (c.getHead().getPredicate().equals(pred)) {
                    it.remove();
                }
            }
        }

        for (Predicate pred : repeatedPred) {
            it = clauses.iterator();
            while (it.hasNext()) {
                c = it.next();
                if (c.getHead().getPredicate().equals(pred)) {
                    repeatedFacts.add(c);
                }
            }
        }
    }

    /**
     * Loads the {@link Map} with all the individuals and its groups.
     * <br>(An individual can be in more than one group)
     *
     * @param program the input program (the problem).
     * @param individuals the set of individuals to be loaded into the groups.
     * @throws ParseException in case the input program does not accord with the
     * specified language.
     * @throws FileNotFoundException in case a filepath do not exist.
     */
    private void loadIndividualsGroups(DLProgram program, Set<Constant> individuals) throws ParseException, FileNotFoundException {
        //DLProgram program = getDLProgram(dlpContent);

        //loadDLPIndividuals(program.getStatements(), this.individuals);
        Predicate p;
        for (Clause c : singleFacts) {
            p = c.getHead().getPredicate();
            loadIndividualsGroupsFromFacts(c.getHead(), program.getClausesAboutPredicate((NormalPredicate) p, ClauseType.FACT), individuals);
        }

        if (individuals.isEmpty())
            return;

        for (Clause c : repeatedFacts) {
            p = c.getHead().getPredicate();
            loadIndividualsGroupsFromFacts(c.getHead(), program.getClausesAboutPredicate((NormalPredicate) p, ClauseType.FACT), individuals);
        }

        if (individuals.isEmpty())
            return;

        Set<Constant> others = new HashSet<>(individuals);

        individualsGroups.put(OTHER_INDIVIDUALS, others);
    }

    /**
     * Loads the {@link Map} with the individuals and its groups from a single
     * fact.
     * <br>(An individual can be in more than one group)
     *
     * @param template the {@link Literal} that represents the template to be
     * loaded.
     * @param clause the input clause of the program.
     * @param individuals the set of individuals to be loaded into the groups.
     */
    @SuppressWarnings("element-type-mismatch")
    private void loadIndividualsGroupsFromFacts(Literal template, List<Clause> clauses, Set<Constant> individuals) {
        List<Term> terms = template.getTerms();
        Set[] setsClasses = new Set[terms.size()];

        for (int i = 0; i < setsClasses.length; i++) {
            setsClasses[i] = new HashSet<>();
        }
        List<Term> localTerms;
        int i;
        for (Clause clause : clauses) {
            localTerms = clause.getHead().getTerms();
            i = 0;
            for (Term term : localTerms) {
                setsClasses[i].add(term);
                individuals.remove(term);

                i++;
            }
        }

        String name;
        i = 0;
        for (Term term : template.getTerms()) {
            name = term.getName();
            if (individualsGroups.containsKey(name)) {
                individualsGroups.get(name).addAll(setsClasses[i]);
            } else {
                individualsGroups.put(name, setsClasses[i]);
            }
            i++;
        }
    }

    /**
     * Get a {@link DLProgram} class with the program from the DLP's content.
     *
     * @param dlpContent the DLP's content.
     * @return a {@link DLProgram} class with the program.
     * @throws ParseException in case the input program does not accord with the
     * specified language.
     */
    private DLProgram getDLProgram(String dlpContent) throws ParseException {
        try {
            return getDLProgram(dlpContent, null);
        } catch (OWLOntologyCreationException ex) {
            throw new ParseException(ex.getMessage());
        }
    }

    /**
     * Get a {@link DLProgram} class with the program from the DLP's content and
     * an owl's content.
     *
     * @param owlContent the olw's content.
     * @param dlpContent the DLP's content.
     * @return a {@link DLProgram} class with the program.
     * @throws ParseException in case the input program does not accord with the
     * specified language.
     * @throws OWLOntologyCreationException in case the owl program does not
     * accord with the specified language.
     */
    private DLProgram getDLProgram(String dlpContent, String owlContent) throws ParseException, OWLOntologyCreationException {
        DLProgramParser parser;
        parser = new DLProgramParser(new StringReader(dlpContent));

        if (owlContent != null && !owlContent.isEmpty()) {
            parser.setOntology(getOntology());
        }
        return parser.program();
    }

    /**
     * Get the ontology from the owl file.
     *
     * @return the ontology from the owl file.
     * @throws OWLOntologyCreationException in case the owl program does not
     * accord with the specified language.
     */
    private OWLOntology getOntology() throws OWLOntologyCreationException {
        if (ontology == null && owlContent != null && !owlContent.isEmpty()) {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            ontology = man.loadOntologyFromOntologyDocument(new StringBufferInputStream(owlContent));
        }
        return ontology;
    }

    /**
     * Loads the individuals and predicates from both the ontology's and DLP's
     * contents.
     * <br>This method is public to allow other classes to uses those
     * individuals and predicates.
     *
     * @param individuals the {@link Set} of {@link Constant} of the
     * individuals.
     * @param predicates the {@link Set} of {@link DataLogPredicate} of the
     * predicates.
     * @param program the input program.
     * @throws FileNotFoundException in case a filepath do not exist.
     * @throws ParseException in case the input program does not accord with the
     * specified language.
     * @throws OWLOntologyCreationException in case the owl program does not
     * accord with the specified language.
     */
    public void loadIndividualsAndPredicates(Set<Constant> individuals, Set<DataLogPredicate> predicates, DLProgram program) throws FileNotFoundException, ParseException, OWLOntologyCreationException {
        loadOntology(individuals, predicates);
        loadDLP(individuals, predicates, program);
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
            head = dataLogPredicate.getHead();
            if (head.startsWith("<") && head.endsWith(">")) {
                head = head.substring(head.lastIndexOf(PREFIX_SEPARATOR) + PREFIX_SEPARATOR.length(), head.lastIndexOf(">"));
            }
            dataLogPredicate.setHead(head);
        }
    }

    /**
     * Loads the individuals and predicates from both the ontology's contents.
     *
     * @param individuals the {@link Set} of {@link Constant} of the
     * individuals.
     * @param predicates the {@link Set} of {@link DataLogPredicate} of the
     * predicates.
     * @throws OWLOntologyCreationException in case the owl program does not
     * accord with the specified language.
     */
    private void loadOntology(Set<Constant> individuals, Set<DataLogPredicate> predicates) throws OWLOntologyCreationException {
        if (individuals == null || predicates == null || getOntology() == null)
            return;

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
     * Loads the individuals and predicates from both the DLP's contents.
     *
     * @param individuals the {@link Set} of {@link Constant} of the
     * individuals.
     * @param predicates the {@link Set} of {@link DataLogPredicate} of the
     * predicates.
     * @param program the input program.
     * @throws FileNotFoundException in case a filepath do not exist.
     * @throws ParseException in case the input program does not accord with the
     * specified language.
     */
    private void loadDLP(Set<Constant> individuals, Set<DataLogPredicate> predicates, DLProgram program) throws FileNotFoundException, ParseException {
        List<ProgramStatement> programs = program.getStatements();
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
     * Setter for the template's content.
     *
     * @param templateContent the template's content.
     */
    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    /**
     * Setter for the program's content.
     *
     * @param programContent the program's content.
     */
    public void setProgramContent(String programContent) {
        this.dlpContent = programContent;
    }

    /**
     * Getter for the template's content.
     *
     * @return the template's content.
     */
    public String getTemplateContent() {
        return templateContent;
    }

    /**
     * Getter for the program's content.
     *
     * @return the program's content.
     */
    public String getProgramContent() {
        return dlpContent;
    }

    /**
     * Getter for the single facts.
     *
     * @return the single facts.
     */
    public Set<Clause> getSingleFacts() {
        return singleFacts;
    }

    /**
     * Getter for the repeated facts.
     *
     * @return the repeated facts.
     */
    public Set<Clause> getRepeatedFacts() {
        return repeatedFacts;
    }

    @Override
    public Set<Clause> getTemplateFacts() {
        Set<Clause> resp = new HashSet<>();
        resp.addAll(singleFacts);
        resp.addAll(repeatedFacts);

        return resp;
    }

    @Override
    public Set<Clause> getTemplateFactsForPredicate(DataLogPredicate pred) {
        if (!factsMap.containsKey(pred.getHead())) {
            Set<Clause> answer = new LinkedHashSet<>();

            for (Clause fact : singleFacts) {
                if (hasPredicateInClause(fact, pred)) {
                    answer.add(fact);
                }
            }

            for (Clause fact : repeatedFacts) {
                if (hasPredicateInClause(fact, pred)) {
                    answer.add(fact);
                }
            }

            factsMap.put(pred.getHead(), answer);
        }

        return factsMap.get(pred.getHead());
    }

    /**
     * Verifies if the {@link Clause} has the specified
     * {@link DataLogPredicate}.
     *
     * @param c the {@link Clause}.
     * @param p the {@link DataLogPredicate}.
     * @return true if it has, false otherwise.
     */
    private boolean hasPredicateInClause(Clause c, DataLogPredicate p) {
        String cs = c.getHead().getPredicate().toString();
        cs = cs.substring(0, cs.lastIndexOf("/"));
        return cs.equals(p.getHead());
    }

    @Override
    public Set<? extends DataLogPredicate> getProgramPredicates() {
        return predicates;
    }

    @Override
    public Map<String, Set<? extends Constant>> getIndividualsGroups() {
        return individualsGroups;
    }

    @Override
    public Set<Constant> getIndividuals() {
        return individuals;
    }

    @Override
    public Map<String, List<List<TermType>>> getConstantMap() {
        return constantsMap;
    }

}
