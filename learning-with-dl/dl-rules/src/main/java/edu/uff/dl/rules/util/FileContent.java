/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.rules.Rule;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 *
 * @author Victor
 */
public class FileContent {

    public static String getStringFromFile(String filePath) throws FileNotFoundException {
        return getStringFromFile(new File(filePath));
    }

    public static String getStringFromFile(String... filePath) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        for (String string : filePath) {
            sb.append(getStringFromFile(new File(string)));
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    public static String getStringFromFile(Collection<String> filePath) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        for (String string : filePath) {
            sb.append(getStringFromFile(new File(string)));
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    public static String getStringFromFile(File file) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        Scanner in = new Scanner(file);

        while (in.hasNext()) {
            sb.append(in.nextLine());
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    public static Reader getReaderFromFile(File file) throws FileNotFoundException {
        return new StringReader(getStringFromFile(file));
    }

    public static Reader getReaderFromFile(String filePath) throws FileNotFoundException {
        return new StringReader(getStringFromFile(filePath));
    }

    public static Set<Literal> getExamplesLiterals(String content) throws org.semanticweb.drew.dlprogram.parser.ParseException {
        List<ProgramStatement> programs = getProgramStatements(content);
        Set<Literal> samples = new HashSet<>();
        Clause c;
        Literal l;

        for (ProgramStatement ps : programs) {
            if (ps.isClause() && (c = ps.asClause()).isFact()) {
                l = new Literal(c.getHead().getPredicate(), c.getHead().getTerms());
                samples.add(l);
            }
        }

        return samples;
    }

    public static List<ProgramStatement> getProgramStatements(String content) throws org.semanticweb.drew.dlprogram.parser.ParseException {
        DLProgramKB kb = new DLProgramKB();

        DLProgram elprogram = null;

        DLProgramParser parser;

        Reader reader;

        reader = new StringReader(content);

        parser = new DLProgramParser(reader);

        elprogram = parser.program();
        kb.setProgram(elprogram);
        return elprogram.getStatements();
    }

    public static ConcreteLiteral getLiteralFromString(String literalLine) {
        int indexOfFirstParenteses = literalLine.indexOf("(");
        String termLine = literalLine.substring(indexOfFirstParenteses + 1, literalLine.lastIndexOf(")"));
        String[] termsString = termLine.split(",");
        List<Term> terms = new ArrayList<>(termsString.length);
        literalLine = literalLine.substring(0, indexOfFirstParenteses);
        
        boolean negative = literalLine.startsWith("-");
        if (negative) {
            literalLine = literalLine.substring(1);
        }
        
        int j = 0;
        for (String term : termsString) {
            terms.add(new Constant(term.trim()));
            j++;
        }
        ConcreteLiteral literal = new DataLogLiteral(literalLine, terms, negative);
        
        //ConcreteLiteral literal = new ConcreteLiteral(literalLine, terms);
        return literal;
    }
    
    public static Rule getRuleFromString(String rule) throws ParseException {
        ProgramStatement ps = getProgramStatements(rule).get(0);

        if (!ps.isClause())
            return null;
        Clause c = ps.asClause();

        if (c.getType() != ClauseType.RULE)
            return null;
        
        SortedSet<DataLogLiteral> lits = new TreeSet<>();

        boolean fail;
        DataLogLiteral d;
        for (Literal literal : c.getBody()) {
            fail = false;
            String head = removeSlash(literal.getPredicate().toString());
            if (head.startsWith("not")) {
                fail = true;
                head = head.substring(3);
            }
            d = new DataLogLiteral(head, literal.getTerms(), literal.isNegative());
            d.setFailed(fail);
            lits.add(d);
        }

        return new Rule(new DataLogLiteral(removeSlash(c.getHead().getPredicate().toString()), c.getHead().getTerms()), lits);
    }

    private static String removeSlash(String s) {
        return s.substring(0, s.indexOf("/"));
    }

}
