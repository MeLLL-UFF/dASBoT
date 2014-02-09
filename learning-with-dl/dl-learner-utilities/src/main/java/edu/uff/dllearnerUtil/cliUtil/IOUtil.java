/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uff.dllearnerUtil.cliUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Victor
 */
public class IOUtil {

    public static final String SUFFIX = ".tmp";

    public static File stringToFile(String fileContext, String virtualFileName) throws IOException {
        InputStream in = IOUtils.toInputStream(fileContext);
        final File tempFile = File.createTempFile(virtualFileName, SUFFIX);
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        IOUtils.copy(in, out);
        in.close();
        return tempFile;
    }

    public static String readFile(String filePath) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }
    
    public static String readFile(String[] filePath) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        for (String fp : filePath) {
            sb.append(readFile(fp));
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
