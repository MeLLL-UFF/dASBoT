/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.uff.dl.rules.datalog.DataLogPredicate;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.rules.AnswerSetRule;
import static edu.uff.dl.rules.test.App.getTime;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.FileContent;
import it.unical.mat.wrapper.DLVInvocationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.DLProgramKB;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 *
 * @author Victor
 */
public class DLRulesRun extends Thread {

    //In parametres
    private Set<String> dlpFilepaths;
    private String owlFilepath;
    private String templateFilepath;

    private Set<String> dlpSamplesFilepath;

    private Set<String> compareFilepaths;
    
    //Out parametres
    private DReWReasoner reasoner;
    private double duration;
    AnswerSetRule anwserSetRule;
    
    //Optional parametres
    private int offSet;
    private boolean compareRule;
    
    //Private
    private DReWRLCLILiteral drew;

    public DLRulesRun(Set<String> dlpFilepaths, String owlFilepath, String templateFilepath, Set<String> dlpSamplesFilepath, Set<String> compareFilepaths) {
        this.dlpFilepaths = dlpFilepaths;
        this.owlFilepath = owlFilepath;
        this.templateFilepath = templateFilepath;
        this.dlpSamplesFilepath = dlpSamplesFilepath;
        this.compareFilepaths = compareFilepaths;
        
        this.compareRule = true;
        this.offSet = 0;
    }


    @Override
    public void run() {
        String begin, end;
        Box<Long> b = new Box<>(null), e = new Box(null);
        begin = getTime(b);
        getTime();
        try {
            runDLRulesReasoner(offSet);
        } catch (ComponentInitException | IOException | ParseException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }

        end = getTime(e);
        System.out.println("");
        System.out.println("Begin: " + begin);
        System.out.println("End:   " + end);
        double dif = e.getContent() - b.getContent();
        dif /= 1000;
        System.out.println("Total time: " + dif + "s");
        this.duration = dif;
    }

    public void runDLRulesReasoner(int offSet) throws ComponentInitException, FileNotFoundException, IOException, org.semanticweb.drew.dlprogram.parser.ParseException {
        String dlpContent = FileContent.getStringFromFile(dlpFilepaths);
        String samplesContent = FileContent.getStringFromFile(dlpSamplesFilepath);
        String templateContent = FileContent.getStringFromFile(templateFilepath);

        reasoner = new DReWReasoner(owlFilepath, dlpContent, samplesContent, templateContent);
        reasoner.setOffSet(offSet);
        Set<Constant> individuals = new HashSet<>();
        Set<DataLogPredicate> predicates = new HashSet<>();

        reasoner.loadIndividualsAndPredicates(individuals, predicates);
        reasoner.init();

        if (reasoner.getAnswerSetRules() == null || reasoner.getAnswerSetRules().size() < 1)
            return;
        anwserSetRule = reasoner.getAnswerSetRules().get(0);

        String in = dlpContent + "\n" + anwserSetRule.getRulesAsString();

        System.out.println(anwserSetRule.getRulesAsString());
        System.out.println("");
        System.out.println("Rule's body: " + anwserSetRule.getAnswerRule().getRules().iterator().next().getTerms().size() + " literals.");
        System.out.println("");
        System.out.println("Comparar a Regra: " + getTime());
        System.out.println("");
        
        if (compareRule) compareRuleWithSamples(in);
    }
    
    private void compareRuleWithSamples(String in) throws ParseException, FileNotFoundException {
        drew = DReWRLCLILiteral.get(reasoner.getArg());
        drew.setDLPContent(in);
        drew.go();
        Set<Literal> lits = drew.getLiteralModelHandler().getAnswerSets().get(0);
        String head = "Verificando exemplos positivos";
        String bottom = "Exemplos Positivos Cobertos (train1.f) / Total";

        for (String sample : dlpSamplesFilepath) {
            compareRuleWithSample(lits, sample, head, bottom);
        }

        for (String filepaths : compareFilepaths) {
            head = "Verificando exemplos cobertos (" + filepaths + ")";
            bottom = "Exemplos Cobertos / Total";
            compareRuleWithSample(lits, filepaths, head, bottom);
        }
    }

    private void compareRuleWithSample(Set<Literal> lits, String filepath, String head, String bottom) throws ParseException, FileNotFoundException {
        System.out.println(head + ": " + getTime());
        Set<Literal> listSamples = getSamplesLiterals(FileContent.getStringFromFile(filepath));
        int positive = 0;
        for (Literal s : listSamples) {
            if (lits.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }

        System.out.println(bottom + ": " + positive + " / " + listSamples.size());
        System.out.println("");
    }

    private static Set<Literal> getSamplesLiterals(String content) throws org.semanticweb.drew.dlprogram.parser.ParseException {
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

    private static List<ProgramStatement> getProgramStatements(String content) throws org.semanticweb.drew.dlprogram.parser.ParseException {
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

    public static String getTime() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
        //System.out.println("");
    }

    public static String getTime(Box<Long> diference) {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        long resp = 0;
        resp += millis;
        resp += second * 1000;
        resp += minute * 60 * 1000;
        resp += hour * 60 * 60 * 1000;
        resp += day * 24 * 60 * 60 * 1000;
        resp += month * 30 * 24 * 60 * 60 * 1000;
        resp += year * 365 * 30 * 24 * 60 * 60 * 1000;

        diference.setContent(resp);

        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
    }

    public int getOffSet() {
        return offSet;
    }

    public void setOffSet(int offSet) {
        this.offSet = offSet;
    }

    public boolean isCompareRule() {
        return compareRule;
    }

    public void setCompareRule(boolean compareRule) {
        this.compareRule = compareRule;
    }

    public DReWReasoner getReasoner() {
        return reasoner;
    }

    public double getDuration() {
        return duration;
    }

    public AnswerSetRule getAnwserSetRule() {
        return anwserSetRule;
    }

    @Override
    public void interrupt() {
        try {
            drew.killDLV();
            reasoner.killDLV();
        } catch (DLVInvocationException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
        
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    
}
