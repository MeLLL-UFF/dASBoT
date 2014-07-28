/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import java.io.File;
import java.util.Comparator;

/**
 *
 * @author Victor
 */
public class RuleFileNameComparator implements Comparator<File> {

    private String prefix;

    public RuleFileNameComparator(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public int compare(File o1, File o2) {
        try {
            if (o1.isHidden()) 
                return -1;
            if (o2.isHidden())
                return 1;
            String name1, name2;
            name1 = o1.getName();
            name2 = o2.getName();

            int n1, n2;

            name1 = name1.substring(name1.lastIndexOf(prefix) + prefix.length(), name1.lastIndexOf("."));
            name2 = name2.substring(name2.lastIndexOf(prefix) + prefix.length(), name2.lastIndexOf("."));
            n1 = Integer.parseInt(name1);
            n2 = Integer.parseInt(name2);

            if (n1 < n2)
                return -1;
            else if (n1 == n2)
                return 0;
            else
                return 1;
        } catch (Exception e) {
            return -1;
        }

    }

}
