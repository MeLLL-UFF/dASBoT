/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

/**
 * Class to get the deafult arguments to run the DReW.
 *
 * @author Victor Guimarães.
 */
public class DReWDefaultArgs {

    /**
     * The default argument.
     */
    public static final String[] ARGS = {
        "-rl",
        "-ontology",
        "",
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
}
