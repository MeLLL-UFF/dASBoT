/*
 * UFF Project Semantic Learning
 */
package br.uff.dl.rules.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Victor Guimarães
 */
public class FoldFactory {

    private String inputFilePrefix;
    private String outputFilePrefix;
    private int numberOfFolds;

    private FoldFactory(String inputFilePrefix, String outputFilePrefix, int numberOfFolds) {
        this.inputFilePrefix = inputFilePrefix;
        this.outputFilePrefix = outputFilePrefix;
        this.numberOfFolds = numberOfFolds;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("You must pass three arguments: input prefix \t output prefix \t number of folds");
        }

        FoldFactory ff = new FoldFactory(args[0], args[1], Integer.parseInt(args[2]));
        ff.run();
    }

    private void run() {
        try {
            createFolds(inputFilePrefix, outputFilePrefix, ".f");
            createFolds(inputFilePrefix, outputFilePrefix, ".n");
        } catch (IOException ex) {
            Logger.getLogger(FoldFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createFolds(String inputFilepath, String outputPrefix, String extension) throws FileNotFoundException, IOException {
        StringBuilder[] stringBuilders = new StringBuilder[numberOfFolds];

        for (int i = 0; i < stringBuilders.length; i++) {
            stringBuilders[i] = new StringBuilder();
        }

        List<String> lines = new LinkedList<>();
        Collections.addAll(lines, FileContent.getStringFromFile(inputFilepath + extension).trim().split("\n"));
        Random random = new Random();

        int count = 0, code;
        while (!lines.isEmpty()) {
            code = random.nextInt(lines.size());
            stringBuilders[count % stringBuilders.length].append(lines.get(code)).append("\n");
            lines.remove(code);
            count++;
        }

        count = 0;
        for (StringBuilder stringBuilder : stringBuilders) {
            FileContent.saveToFile(outputPrefix + (count + 1) + extension, stringBuilder.toString().trim());
            count++;
        }
    }

}
