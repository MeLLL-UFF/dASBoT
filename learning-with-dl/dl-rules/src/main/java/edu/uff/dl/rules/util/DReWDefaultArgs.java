/*
 * UFF Project Semantic Learning
 */

package edu.uff.dl.rules.util;

/**
 *
 * @author Victor
 */
public class DReWDefaultArgs {
    public static final String[] ARGS = {
        "-rl",
        "-ontology",
        "",
        "-dlp",
        "",
        "-dlv",
        "/usr/lib/dlv.i386-apple-darwin.bin"
    };
    
    public static final String[] ARGS_NON_ONTOLOGY = {
        "-rl",
        "-dlp",
        "",
        "-dlv",
        "/usr/lib/dlv.i386-apple-darwin-iodbc.bin"
    };
}
