/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author Victor
 */
public interface Serializable<T> {

    public void serialize(File file) throws FileNotFoundException;

    public T deserialize(File file) throws FileNotFoundException;

}
