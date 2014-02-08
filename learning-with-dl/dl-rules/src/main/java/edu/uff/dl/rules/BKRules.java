/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uff.dl.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author Victor
 */
public class BKRules {

    private Set<Clause> facts;
    private Set<Clause> rules;
    private DLProgram program;

    /**
     * This method implements the constructor to get facts and rules from a datalog file without linked OWL data.
     * @param bkFilePath path to datalog file
     * 
     */
    public BKRules(String bkFilePath) {
        this(bkFilePath, null);
    }

    /**
     * This method implements the constructor to get facts and rules from a datalog file with linked OWL data.
     * @param bkFilePath path to datalog file
     * @param owlFilePath path to owl file
     */
    public BKRules(String bkFilePath, String owlFilePath) {
        try {
            DLProgramParser parser;
            parser = new DLProgramParser(new FileReader(bkFilePath));
            if (owlFilePath != null) {

                File file = new File(owlFilePath);

                OWLOntologyManager man = OWLManager.createOWLOntologyManager();
                OWLOntology ontology;
                ontology = man.loadOntologyFromOntologyDocument(file);

                DLProgramKB kb = new DLProgramKB();
                kb.setOntology(ontology);
                parser.setOntology(ontology);
            }

            program = parser.program();

            loadClausesSets();
        } catch (OWLOntologyCreationException ex) {
            System.err.println(ex.getClass().getName());
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getClass().getName());
        } catch (ParseException ex) {
            System.err.println(ex.getClass().getName());
        }
    }

    public Set<Clause> getFacts() {
        return facts;
    }

    public Set<Clause> getRules() {
        return rules;
    }

    private void loadClausesSets() {
        List<ProgramStatement> l = program.getStatements();
        Clause c = null;

        if (l == null) {
            return;
        }
        
        if (facts == null) {
            facts = new HashSet<Clause>();
        }

        if (rules == null) {
            rules = new HashSet<Clause>();
        }

        for (ProgramStatement ps : l) {
            if (ps.isClause()) {
                c = ps.asClause();
                if (c != null) {

                    if (c.getType() == ClauseType.FACT) {
                        facts.add(c);
                    } else if (c.getType() == ClauseType.RULE) {
                        rules.add(c);
                    }

                }
            }
        }
    }
    
    

}
