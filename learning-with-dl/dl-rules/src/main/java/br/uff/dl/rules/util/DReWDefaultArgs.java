/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import java.io.File;
import java.util.Arrays;

/**
 * Class to get the deafult arguments to run the DReW.
 *
 * @author Victor Guimarães.
 */
public class DReWDefaultArgs {

    /**
     * The default argument.
     */
    public static final String[] DEFAULT_ARGS = {
            "-rl",
            "-ontology",
            "",
            "-dlp",
            "",
            "-dlv",
            System.getenv("DLV_PATH"),
    };

    /**
     * The default argument.
     */
    public static final String[] DEFAULT_ARGS_NO_ONTOLOGY = {
            "-rl",
            "-dlp",
            "",
            "-dlv",
            System.getenv("DLV_PATH"),
    };

    /**
     * The default argument without ontology.
     *
     * @deprecated does not works very well.
     */
    public static final String[] ARGS_NON_ONTOLOGY = {
            "-rl",
            "-dlp",
            "",
            "-dlv",
            "/usr/lib/dlv.i386-apple-darwin-iodbc.bin"
    };

    public static String[] getDefaultArgs() {
        return Arrays.copyOf(DEFAULT_ARGS, DEFAULT_ARGS.length);
    }

<<<<<<< HEAD
=======
    public static String[] getDefaultArgsNoOntology() {
        return Arrays.copyOf(DEFAULT_ARGS_NO_ONTOLOGY, DEFAULT_ARGS_NO_ONTOLOGY.length);
    }
    
>>>>>>> master
    public static int getOWLFilepath(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-ontology")) {
                if ((new File(args[i + 1])).exists()) {
                    return i + 1;
                }
            }
        }

        return -1;
    }

    public static int getOntologyPathIndex() {
        return 2;
    }

    public static int getDLVPathIndex() {
        return DEFAULT_ARGS.length - 1;
    }
}
