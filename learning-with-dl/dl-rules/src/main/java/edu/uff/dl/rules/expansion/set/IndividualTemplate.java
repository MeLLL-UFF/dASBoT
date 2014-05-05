/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.expansion.set;

import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.datalog.SimplePredicate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author Victor
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

    public IndividualTemplate() {
        this.singleFacts = new LinkedHashSet<>();
        this.repeatedFacts = new LinkedHashSet<>();
        this.individualsGroups = new HashMap<>();
        this.individuals = new LinkedHashSet<>();
        this.predicates = new LinkedHashSet<>();
    }

    public IndividualTemplate(final String templateContent, final String dlpContent, final String owlContent) {
        this();
        this.templateContent = templateContent;
        this.dlpContent = dlpContent;
        this.owlContent = owlContent;
        if (templateContent != null) {
            this.templateContent = templateContent.replace("#", "");
        }
    }
    //TODO: getters ands setters for the essentials variables. Parameters of above constructor.

    @Override
    public void init() throws ComponentInitException {
        Set<Clause> clauses;
        DLProgram program;
        try {
            clauses = getClausesFromContent();
            program = getDLProgram(dlpContent, owlContent);
            loadClauses(clauses);
            loadIndividualsAndPredicates(this.individuals, predicates, program);
            Set<Constant> localIndividuals = new HashSet(individuals);
            loadIndividualsGroups(program, localIndividuals);
        } catch (Exception ex) {
            throw new ComponentInitException(ex.getMessage());
        }

    }

    private Set<Clause> getClausesFromContent() throws Exception {
        Set<Clause> resp = new LinkedHashSet<>();
        try {
            DLProgram program = getDLProgram(templateContent);

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



    private DLProgram getDLProgram(String dlpContent) throws ParseException {
        try {
            return getDLProgram(dlpContent, null);
        } catch (OWLOntologyCreationException ex) {
            throw new ParseException(ex.getMessage());
        }
    }

    private DLProgram getDLProgram(String dlpContent, String owlContent) throws ParseException, OWLOntologyCreationException {
        DLProgramParser parser;
        parser = new DLProgramParser(new StringReader(dlpContent));

        if (owlContent != null && !owlContent.isEmpty()) {
            parser.setOntology(getOntology());
        }
        return parser.program();
    }

    private OWLOntology getOntology() throws OWLOntologyCreationException {
        if (ontology == null && owlContent != null && ! owlContent.isEmpty()) {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            ontology = man.loadOntologyFromOntologyDocument(new StringBufferInputStream(owlContent));
        }
        return ontology;
    }

    public void loadIndividualsAndPredicates(Set<Constant> individuals, Set<DataLogPredicate> predicates, DLProgram program) throws FileNotFoundException, ParseException, OWLOntologyCreationException {
        loadOntology(individuals, predicates);
        loadDLP(individuals, predicates, program);
    }

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
    
    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public void setProgramContent(String programContent) {
        this.dlpContent = programContent;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public String getProgramContent() {
        return dlpContent;
    }

    public Set<Clause> getSingleFacts() {
        return singleFacts;
    }

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
        Set<Clause> resp = new LinkedHashSet<>();
        
        for (Clause fact : singleFacts) {
            if (hasPredicateInClause(fact, pred)) {
                resp.add(fact);
            }
        }
        
        for (Clause fact : repeatedFacts) {
            if (hasPredicateInClause(fact, pred)) {
                resp.add(fact);
            }
        }
        
        return resp;
    }
    
    private boolean hasPredicateInClause(Clause c, DataLogPredicate p) {
        return c.getHead().getPredicate().toString().equals(p.toString());
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

}

//    private void loadDLPIndividuals(List<ProgramStatement> programs, Set<Constant> individuals) throws FileNotFoundException, ParseException {
//
//        Clause c;
//        Predicate p;
//
//        for (ProgramStatement ps : programs) {
//            if (ps.isClause() && (c = ps.asClause()).isFact()) {
//                p = c.getHead().getPredicate();
//                if (p instanceof NormalPredicate) {
//                    for (Term term : c.getHead().getTerms()) {
//                        if (term instanceof Constant) {
//                            individuals.add((Constant) term);
//                        }
//                    }
//                }
//            }
//        }
//
//    }
