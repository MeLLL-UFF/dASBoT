/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.test;

import br.uff.dl.rules.drew.DReWRLCLI;
import br.uff.dl.rules.drew.DReWRLCLILiteral;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Set;
import org.semanticweb.drew.dlprogram.model.CacheManager;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 *
 * @author Victor
 */
public class MyCommandLine {

    public static void main(String[] args) {
        String[] arg = new String[7];
        arg[0] = "-rl";
        arg[1] = "-ontology";
        arg[2] = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.owl";
        //arg[2] = "/Users/Victor/Dropbox/dl.rules/sample.owl";
        arg[3] = "-dlp";
        arg[4] = "/Users/Victor/NetBeansProjects/drew-master/sample_data/network.dlp";
        //arg[4] = "/Users/Victor/Dropbox/dl.rules/sample.dlp";
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
                }
            } else {
                System.out.println("DReW's Sets");
                DReWRLCLI.main(arg);
            }
        } catch (Exception ex) {
            System.err.println(ex.getClass().getName());
        }
    }

    public static void testDLProgram(String owlFile, String dlpFile) throws OWLOntologyCreationException, FileNotFoundException, ParseException {
        //File file = new File(owlFile);

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        //OWLOntology ontology;
        //ontology = man.loadOntologyFromOntologyDocument(file);

        //DLProgramKB kb = new DLProgramKB();
        //kb.setOntology(ontology);
        DLProgram elprogram = null;

        DLProgramParser parser;
        parser = new DLProgramParser(new FileReader(dlpFile));
        //parser.setOntology(ontology);
        elprogram = parser.program();

        CacheManager cm = CacheManager.getInstance();
        //NormalPredicate np = cm.getPredicate("", 1);

        //List<Clause> cl = elprogram.getClausesAboutPredicate(null, ClauseType.CONSTRAINT);
        List<ProgramStatement> l = elprogram.getStatements();
        Clause c;
        for (ProgramStatement ps : l) {
            c = ps.asClause();
            if (c.getType() == ClauseType.FACT) {
                System.out.println(c);
            }
        }

        //System.out.println(elprogram);
    }

}
