package edu.uff.dllearnerensemble;

import edu.uff.dllearnerensemble.clicv.CLICV;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DL-Learner Ensemble Tests!
 *
 */
public class App 
{
    public static void main(String[] args)
    {
        System.out.println("DL-Learner Ensemble Tests!");
        
        String path = "C:\\Users\\Bruno\\Projetos\\Machine Learning\\Datasets\\dl\\trainTest\\facultynear\\";
        File file = new File(path + "facultynear.conf");
        
        for(int i = 0; i < 1; i++) {
            CLICV cv = new CLICV(file, path + "facultynear", 10, "Bagging" + i + ".txt", "BaggingCV");
            try {
                cv.init();
                cv.run();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
