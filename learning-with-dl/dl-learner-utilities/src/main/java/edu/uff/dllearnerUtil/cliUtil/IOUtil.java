/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uff.dllearnerUtil.cliUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

        return tempFile;
    }
}
