/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import edu.uff.dl.rules.datalog.ConcreteLiteral;
import edu.uff.dl.rules.datalog.DataLogLiteral;
import edu.uff.dl.rules.rules.Rule;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.ClauseType;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * Class to retrieve information from files. It can be used for get the file's
 * content on different formats.
 * <br> This class has only static methods, so has no need for instanciate it.
 *
 * @author Victor Guimarães
 */
public class FileContent {

    /**
     * Get the file's content as a {@link String}.
     *
     * @param filepath the file's path.
     * @return the content as {@link String}.
     * @throws FileNotFoundException if the does not exists.
     */
    public static String getStringFromFile(String filepath) throws FileNotFoundException {
        return getStringFromFile(new File(filepath));
    }

    /**
     * Get the content of one or more files as a {@link String}.
     *
     * @param filepath a set of file's paths.
     * @return the content as {@link String}.
     * @throws FileNotFoundException if the does not exists.
     */
    public static String getStringFromFile(String... filepath) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        for (String string : filepath) {
            sb.append(getStringFromFile(string));
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Get the content of one or more files as a {@link String}.
     *
     * @param filepath a collection of file's paths.
     * @return the content as {@link String}.
     * @throws FileNotFoundException if the does not exists.
     */
    public static String getStringFromFile(Collection<String> filepath) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        for (String string : filepath) {
            sb.append(getStringFromFile(string));
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Get the content of one file as a {@link String}.
     *
     * @param file the file.
     * @return the content as {@link String}.
     * @throws FileNotFoundException if the does not exists.
     */
    public static String getStringFromFile(File file) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        Scanner in = new Scanner(file);

        while (in.hasNext()) {
            sb.append(in.nextLine());
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Get the a {@link Reader} from a file.
     *
     * @param file the file.
     * @return the {@link Reader}.
     * @throws FileNotFoundException if the does not exists.
     */
    public static Reader getReaderFromFile(File file) throws FileNotFoundException {
        return new StringReader(getStringFromFile(file));
    }

    /**
     * Get the a {@link Reader} from a file.
     *
     * @param filepath the file's path.
     * @return the {@link Reader}.
     * @throws FileNotFoundException if the does not exists.
     */
    public static Reader getReaderFromFile(String filepath) throws FileNotFoundException {
        return new StringReader(getStringFromFile(filepath));
    }

    /**
     * Get the file's content as a {@link Set} of {@link Literal}.
     *
     * @param content the file's content.
     * @return the {@link Set} of {@link Literal}.
     * @throws ParseException if the file's content does not accord with the
     * language.
     */
    public static Set<Literal> getExamplesLiterals(String content) throws ParseException {
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

    /**
     * Get the file's content as a {@link List} of {@link ProgramStatement}.
     *
     * @param content the file's content.
     * @return the {@link List} of {@link ProgramStatement}.
     * @throws ParseException if the file's content does not accord with the
     * language.
     */
    public static List<ProgramStatement> getProgramStatements(String content) throws ParseException {
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

    /**
     * Get a {@link ConcreteLiteral} from a {@link String}.
     *
     * @param literalLine the {@link String}.
     * @return the {@link ConcreteLiteral}.
     */
    public static ConcreteLiteral getLiteralFromString(String literalLine) throws ParseException {
        List<ProgramStatement> programStatements = getProgramStatements(literalLine);

        Clause c;
        for (ProgramStatement ps : programStatements) {
            if (ps.isClause() && (c = ps.asClause()).isFact()) {
                String predicate = c.getHead().getPredicate().toString();
                predicate = predicate.substring(0, predicate.indexOf("/"));
                return new DataLogLiteral(predicate, c.getHead().getTerms(), c.getHead().isNegative());
            }
        }

        return null;
    }

    /**
     * Get a {@link Rule} from a {@link String}.
     *
     * @param rule the {@link String}.
     * @return the {@link Rule}.
     */
    public static Rule getRuleFromString(String rule) throws ParseException {
        ProgramStatement ps = getProgramStatements(rule).get(0);

        if (!ps.isClause()) {
            return null;
        }
        Clause c = ps.asClause();

        if (c.getType() != ClauseType.RULE) {
            return null;
        }

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

    /**
     * Method used internally to remove a slash from a predicate's notation used
     * by DReW.
     *
     * @param s a predicate as a {@link String} (with the slash).
     * @return the predicate as a {@link String} (without the slash).
     */
    private static String removeSlash(String s) {
        return s.substring(0, s.indexOf("/"));
    }

    /**
     * Method to save the content of a {@link String} into a file. This method
     * creates the file if it does not exists or overwrites it if it does.
     *
     * @param filepath the file path
     * @param content the content
     *
     * @throws java.io.IOException in case something goes wrong with the file
     */
    public static void saveToFile(String filepath, String content) throws IOException {
        saveToFile(filepath, content, false);
    }

    /**
     * Method to save the content of a {@link String} into a file. If it is
     * true, the content is appended on the bottom of the file. If append is
     * false, this method creates the file if it does not exists or overwrites
     * it if it does.
     *
     * @param filepath the file path
     * @param content the content
     * @param append true to append on the bottom, false to overwrite
     *
     * @throws java.io.IOException in case something goes wrong with the file
     */
    public static void saveToFile(String filepath, String content, boolean append) throws IOException {
        OutputStream output = new FileOutputStream(filepath, append);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        BufferedWriter bw = new BufferedWriter(writer);

        bw.write(content);

        bw.close();
    }

    public static void saveConcreteLiteralToFile(Collection<? extends ConcreteLiteral> concreteLiterals, File outputFile, String encode) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encode))) {
            for (ConcreteLiteral concreteLiteral : concreteLiterals) {
                bw.write(concreteLiteral.toString() + "\n");
            }
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static Set<ConcreteLiteral> readConcreteLiteralToFile(String fileContent) throws ParseException {
        Set<Literal> literals = getExamplesLiterals(fileContent);

        return DataLogLiteral.getSetOfLiterals(literals);
    }

}
