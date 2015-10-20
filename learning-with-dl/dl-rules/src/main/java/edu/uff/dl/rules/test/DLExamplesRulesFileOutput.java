/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.test;

import edu.uff.dl.rules.cli.DLRulesCLI;
import edu.uff.dl.rules.drew.DReWRLCLILiteral;
import edu.uff.dl.rules.drew.DReWReasoner;
import edu.uff.dl.rules.rules.AnswerSetRule;
import edu.uff.dl.rules.util.Box;
import edu.uff.dl.rules.util.FileContent;
import it.unical.mat.wrapper.DLVInvocationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.parser.ParseException;

import static edu.uff.dl.rules.util.Time.getTime;

/**
 * This class was used to generate rules and safe all the process results into
 * text files. Its functions was builded into the {@link DLRulesCLI}, use this
 * instead.
 *
 * @deprecated
 * @author Victor Guimarães
 */
public class DLExamplesRulesFileOutput extends Thread {

    //In parametres
    private Set<String> dlpFilepaths;
    private String owlFilepath;
    private String templateFilepath;

    private String dlpPositivesSamplesFilepath;
    private String dlpNegativesSamplesFilepath;

    private Set<String> compareFilepaths;

    //Out parametres
    private DReWReasoner reasoner;
    private double duration;
    AnswerSetRule answerSetRule;

    //Optional parametres
    private int offset;
    private boolean compareRule;

    //Private
    private DReWRLCLILiteral drew;

    /**
     * The constructor of the class with the needed parameters.
     *
     * @param dlpFilepaths a set of paths for the bk files.
     * @param owlFilepath a path for an owl file (future use).
     * @param dlpPositivesSamplesFilepath a filepath for a set of positive
     * examples.
     * @param dlpNegativesSamplesFilepath a filepath for a set of negative
     * examples.
     * @param compareFilepaths a set of filepath to compare results.
     */
    public DLExamplesRulesFileOutput(Set<String> dlpFilepaths, String owlFilepath, String templateFilepath, String dlpPositivesSamplesFilepath, String dlpNegativesSamplesFilepath, Set<String> compareFilepaths) {
        this.dlpFilepaths = dlpFilepaths;
        this.owlFilepath = owlFilepath;
        this.templateFilepath = templateFilepath;
        this.dlpPositivesSamplesFilepath = dlpPositivesSamplesFilepath;
        this.dlpNegativesSamplesFilepath = dlpNegativesSamplesFilepath;
        this.compareFilepaths = compareFilepaths;

        this.compareRule = true;
        this.offset = 0;
    }

    @Override
    public void run() {
        String begin, end;
        Box<Long> b = new Box<>(null), e = new Box(null);
        begin = getTime(b);
        getTime();
        try {
            runDLRulesReasoner(offset);
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

    /**
     * Method which does all the process. Runs the DReW obtains the output,
     * generates the rule and copare it.
     *
     * @param offset the offset to specifi the example that will be used to base
     * the rule on.
     * @throws ComponentInitException in case something goes wrong for DReW.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     * @throws IOException in case something during the file creation goes
     * wrong.
     * @throws org.semanticweb.drew.dlprogram.parser.ParseException
     */
    public void runDLRulesReasoner(int offset) throws ComponentInitException, FileNotFoundException, IOException, org.semanticweb.drew.dlprogram.parser.ParseException {
        String dlpContent = FileContent.getStringFromFile(dlpFilepaths);
        String samplesContent = FileContent.getStringFromFile(dlpPositivesSamplesFilepath);
        String templateContent = null;
        if (templateFilepath != null && !templateFilepath.isEmpty()) {
            templateContent = FileContent.getStringFromFile(templateFilepath);
        }

        reasoner = new DReWReasoner(owlFilepath, dlpContent, samplesContent, templateContent, System.out);
        reasoner.setOffset(offset);
        //Set<Constant> individuals = new HashSet<>();
        //Set<DataLogPredicate> predicates = new HashSet<>();

        //reasoner.loadIndividualsAndPredicates(individuals, predicates);
        reasoner.init();
        reasoner.run();

        if (reasoner.getAnswerSetRules() == null || reasoner.getAnswerSetRules().size() < 1)
            return;
        answerSetRule = reasoner.getAnswerSetRules().get(0);

        String in = dlpContent + "\n" + answerSetRule.getRulesAsString();

        System.out.println(answerSetRule.getRulesAsString());
        System.out.println("");
        System.out.println("Rule's body: " + answerSetRule.getAnswerRule().getRules().iterator().next().getBody().size() + " literals.");
        System.out.println("");
        System.out.println("Comparar a Regra: " + getTime());
        System.out.println("");

        if (compareRule)
            compareRuleWithSamples(in);
    }

    /**
     * Compare the rule against a both the positive and negative examples.
     *
     * @param in the DLP's content.
     * @throws ParseException in case some file does not accord with the laguage
     * rules.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     */
    private void compareRuleWithSamples(String in) throws ParseException, FileNotFoundException {
        drew = DReWRLCLILiteral.get(reasoner.getArg());
        drew.setDLPContent(in);
        drew.go();
        Set<Literal> lits = drew.getLiteralModelHandler().getAnswerSets().get(0);
        String head = "Verificando exemplos positivos";
        String bottom = "Exemplos Positivos Cobertos (train1.f) / Total";

        Set<Literal> listSamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpPositivesSamplesFilepath));
        compareRuleWithSample(lits, listSamples, head, bottom);

        head = "Verificando exemplos negativos";

        bottom = "Exemplos Negativos Cobertos (train1.f) / Total";
        listSamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpNegativesSamplesFilepath));
        compareRuleWithSample(lits, listSamples, head, bottom);

        for (String filepaths : compareFilepaths) {
            head = "Verificando exemplos cobertos (" + filepaths + ")";
            bottom = "Exemplos Cobertos / Total";
            listSamples = FileContent.getExamplesLiterals(FileContent.getStringFromFile(dlpPositivesSamplesFilepath));
            compareRuleWithSample(lits, listSamples, head, bottom);
        }
    }

    /**
     * Compare the rule against a set of examples.
     * <br> This method have a human-like print output.
     *
     * @param literals the DReW's output (Answer Set).
     * @param listExamples the set of examples.
     * @param head a head label.
     * @param bottom a bottom label.
     * @return the number of examples proved by the rule.
     * @throws ParseException in case some file does not accord with the laguage
     * rules.
     * @throws FileNotFoundException in case something on the file system goes
     * wrong.
     */
    private int compareRuleWithSample(Set<Literal> literals, Set<Literal> listSamples, String head, String bottom) throws ParseException, FileNotFoundException {
        System.out.println(head + ": " + getTime());
        //Set<Literal> listSamples = getSamplesLiterals(FileContent.getStringFromFile(filepath));
        int positive = 0;
        for (Literal s : listSamples) {
            if (literals.contains(s)) {
                System.out.println(s);
                positive++;
            }
        }

        System.out.println(bottom + ": " + positive + " / " + listSamples.size());
        System.out.println("");

        return positive;
    }

    /**
     * Getter for the offset.
     *
     * @return the offset.
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
     * Getter for the compare rule. If compara rule is true, the rule will be
     * compared against the compare files. This variable is true by default.
     *
     * @return the compare rule.
     */
    public boolean isCompareRule() {
        return compareRule;
    }

    /**
     * Setter for the compare rule. If compara rule is true, the rule will be
     * compared against the compare files. This variable is true by default.
     *
     * @param compareRule the compare rule.
     */
    public void setCompareRule(boolean compareRule) {
        this.compareRule = compareRule;
    }

    /**
     * Getter for the {@link DReWReasoner}.
     *
     * @return the {@link DReWReasoner}.
     */
    public DReWReasoner getReasoner() {
        return reasoner;
    }

    /**
     * Getter for the duration. The process's duration.
     *
     * @return the duration.
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Getter for the {@link AnswerSetRule}.
     *
     * @return the {@link AnswerSetRule}.
     */
    public AnswerSetRule getAnwserSetRule() {
        return answerSetRule;
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
