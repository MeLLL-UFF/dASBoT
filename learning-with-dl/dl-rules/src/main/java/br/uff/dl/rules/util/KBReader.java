/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGConverter;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.net.URI;

/**
 * Class to generate a KB from a String as content.
 *
 * @author Victor Guimarães
 */
@ComponentAnn(name = "KB Reader", shortName = "kbreader", version = 0.1)
public class KBReader extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource {

    private static Logger logger = Logger.getLogger(KBFile.class);

    private KB kb;

    private BKRules content;

    /**
     * Default constructor (needed for reflection in ComponentManager).
     */
    public KBReader() {
    }

    /**
     * Constructor allowing you to treat an already existing KB object as a
     * KBFile knowledge source. Use it sparingly, because the standard way to
     * create components is via {@link org.dllearner.core.ComponentManager}.
     *
     * @param kb A KB object.
     */
    public KBReader(KB kb) {
        this.kb = kb;
    }

    public static String getName() {
        return "KB Reader";
    }

    /**
     * Constructor allowing you to treat an KB object from a String as a KBFile
     * knowledge source.
     *
     * @param content A KB content.
     */
    public KBReader(BKRules content) {
        this.content = content;
    }

    @Override
    public void init() throws ComponentInitException {
        try {
            if (toKB() == null) {
                if (getContent() == null || content.getKBContent().length() == 0) {
                    throw new ComponentInitException("No content or kb object given. Cannot initialise KBReader component.");
                }
                //content.init();
                kb = KBParser.parseKBFile(content.getKBContent());

                logger.trace("KB Reader parsed successfully.");
            }
        } catch (ParseException e) {
            throw new ComponentInitException("KB content could not be parsed correctly.", e);
        }
    }

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {

        IRI ontologyURI = IRI.create("http://example.com");
        OWLOntology ontology;
        try {
            ontology = manager.createOntology(ontologyURI);
            OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);

        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        return ontology;
    }

    /**
     *
     * @return KB's content
     */
    public BKRules getContent() {
        return content;
    }

    /**
     *
     * @param content KB's content
     */
    public void setContent(BKRules content) {
        this.content = content;
    }

    @Override
    public String toDIG(URI kbURI) {
        return DIGConverter.getDIGString(kb, kbURI).toString();
    }

    @Override
    public String toString() {
        if (kb == null)
            return "KB (not initialised)";
        else
            return kb.toString();
    }

    @Override
    public void export(File file, org.dllearner.core.OntologyFormat format) {
        kb.export(file, format);
    }

    @Override
    public KB toKB() {
        return kb;
    }
}
