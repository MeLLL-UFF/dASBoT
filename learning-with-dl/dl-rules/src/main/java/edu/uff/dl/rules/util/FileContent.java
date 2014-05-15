/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Scanner;

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
    
}
