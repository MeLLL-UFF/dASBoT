/*
 * UFF Project Semantic Learning
 */
package edu.uff.test;

import edu.uff.drew.DReWRLCLILiteral;
import edu.uff.drew.LiteralModelHandler;
import edu.uff.expansion.set.AnswerRule;
import edu.uff.expansion.set.DataLogPredicate;
import edu.uff.expansion.set.ExpansionAnswerSet;
import edu.uff.expansion.set.SimplePredicate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLAtomPredicate;
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
@ComponentAnn(name = "DReWReasoner", shortName = "drewreas", version = 0.1)
public class DReWReasoner implements Component {

    DReWRLCLILiteral drew;
    OWLOntology ontology;
    Set<Constant> individuals;
    Set<DataLogPredicate> predicates;
    Set<Literal> samples;
    String dlpContent;

    private String[] arg = {
        "-rl",
        "-ontology",
        "",
        "-dlp",
        "",
        "-dlv",
        "/usr/lib/dlv.i386-apple-darwin-iodbc.bin"
    };

    public DReWReasoner() {
        this.individuals = new HashSet<>();
        this.predicates = new HashSet<>();
    }

    public DReWReasoner(String owlFilePath, String dlp, boolean isFilePath, Set<Literal> samples) {
        this();
        this.arg[2] = owlFilePath;
        if (isFilePath) {
            this.arg[4] = dlp;
        } else {
            this.dlpContent = dlp;
        }
        this.samples = samples;
    }

    @Override
    public void init() throws ComponentInitException {
        try {
            loadIndividualsAndPredicates(individuals, predicates);
            drew = DReWRLCLILiteral.run(dlpContent, arg);

            ExpansionAnswerSet e;
            for (Set<Literal> answerSet : getAnswerSets()) {
                e = new ExpansionAnswerSet(answerSet, samples, individuals, predicates);
                e.init();
                AnswerRule ar = new AnswerRule(e.getSamples(), e.getExpansionSet());
                //System.out.println(e);
                ar.init();
                System.out.println(ar.getRules().iterator().next().toString());
                System.out.println("\n\n\n");
                //System.out.println(ar.getRules().iterator().next().asClause().toString());
            }

        } catch (FileNotFoundException | ParseException ex) {
            throw new ComponentInitException(ex.getMessage());
        }
    }

    private void loadIndividualsAndPredicates(Set<Constant> individuals, Set<DataLogPredicate> predicates) throws FileNotFoundException, ParseException {
        loadOntology(individuals, predicates);
        loadDLP(individuals, predicates);
        loadSamples(individuals, predicates);
    }

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

    private void loadDLP(Set<Constant> individuals, Set<DataLogPredicate> predicates) throws FileNotFoundException, ParseException {
        DLProgramKB kb = new DLProgramKB();
        kb.setOntology(ontology);
        DLProgram elprogram = null;

        DLProgramParser parser;

        Reader reader;
        if (dlpContent != null) {
            reader = new StringReader(dlpContent);
        } else {
            reader = new FileReader(getDlpFilePath());
        }
        parser = new DLProgramParser(reader);
        parser.setOntology(ontology);
        elprogram = parser.program();
        kb.setProgram(elprogram);

        List<ProgramStatement> programs = elprogram.getStatements();
        Clause c;
        SimplePredicate sp;
        Predicate p;
        NormalPredicate np;

        for (ProgramStatement ps : programs) {
            if (ps.isClause()) {
                c = ps.asClause();
                if (c.isFact()) {
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

    }

    private void loadSamples(Set<Constant> individuals, Set<DataLogPredicate> predicates) {
        Predicate p;
        NormalPredicate np;
        SimplePredicate sp;
        for (Literal l : samples) {
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

    public String getOwlFilePath() {
        return arg[2];
    }

    public void setOwlFilePath(String owlFilePath) {
        arg[2] = owlFilePath;
    }

    public String getDlpFilePath() {
        return arg[4];
    }

    public void setDlpFilePath(String dlpFilePath) {
        arg[4] = dlpFilePath;
    }

    public List<Set<Literal>> getAnswerSets() {
        return drew.getLiteralModelHandler().getAnswerSets();
    }

    public Set<Constant> getIndividuals() {
        return individuals;
    }

    public Set<DataLogPredicate> getPredicates() {
        return predicates;
    }

    public Set<Literal> getSamples() {
        return samples;
    }

    public void setSamples(Set<Literal> samples) {
        this.samples = samples;
    }

}
