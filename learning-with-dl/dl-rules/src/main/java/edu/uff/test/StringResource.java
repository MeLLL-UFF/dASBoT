/*
 * UFF Project Semantic Learning
 */

package edu.uff.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StringUtils;

/**
 *
 * @author Victor
 */
public class StringResource extends AbstractResource implements WritableResource {
    /*
     * Copyright 2002-2012 the original author or authors.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    private final File file;

    private final String path;

    private final String encoding;
    
    private String content;

    /**
     * Create a new FileSystemResource from a file path.
     * <p>
     * Note: When building relative resources via {@link #createRelative}, it
     * makes a difference whether the specified resource base path here ends
     * with a slash or not. In the case of "C:/dir1/", relative paths will be
     * built underneath that root: e.g. relative path "dir2" -> "C:/dir1/dir2".
     * In the case of "C:/dir1", relative paths will apply at the same directory
     * level: relative path "dir2" -> "C:/dir2".
     *
     * @param baseDir a file path
     * @param content content to read
     * @param encoding String encoding
     */
    public StringResource(String baseDir, String content, String encoding) {
        //Assert.notNull(path, "Path must not be null");
        File auxFile;
        auxFile = new File(baseDir);
        //this.file = new File(baseDir);
        if (auxFile.isDirectory()) {
            this.file = new File(baseDir + "temp.tmp");
        } else {
            this.file = auxFile;
        }
        this.encoding = encoding;
        this.path = StringUtils.cleanPath(baseDir);
        this.content = content;
    }

    /**
     * Return the file path for this resource.
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * This implementation returns whether the underlying file exists.
     *
     * @see java.io.File#exists()
     */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * This implementation checks whether the underlying file is marked as
     * readable (and corresponds to an actual file with content, not to a
     * directory).
     *
     * @see java.io.File#canRead()
     * @see java.io.File#isDirectory()
     */
    @Override
    public boolean isReadable() {
        return true;
    }

    /**
     * This implementation opens a FileInputStream for the underlying file.
     *
     * @see java.io.FileInputStream
     */
    public InputStream getInputStream() throws IOException {
        return new ReaderInputStream(new StringReader(content), encoding);
        //ByteArrayInputStream(content.getBytes());
    }

    /**
     * This implementation returns a URL for the underlying file.
     *
     * @see java.io.File#toURI()
     */
    @Override
    public URL getURL() throws IOException {
        return this.file.toURI().toURL();
    }

    /**
     * This implementation returns a URI for the underlying file.
     *
     * @see java.io.File#toURI()
     */
    @Override
    public URI getURI() throws IOException {
        return this.file.toURI();
    }

    /**
     * This implementation returns the underlying File reference.
     */
    @Override
    public File getFile() {
        return this.file;
    }

    /**
     * This implementation returns the underlying File's length.
     */
    @Override
    public long contentLength() throws IOException {
        return this.content.length();
    }

    /**
     * This implementation creates a FileSystemResource, applying the given path
     * relative to the path of the underlying file of this resource descriptor.
     *
     * @see org.springframework.util.StringUtils#applyRelativePath(String,
     * String)
     */
    @Override
    public Resource createRelative(String relativePath) {
        //String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return new StringResource(relativePath, content, encoding);
    }

    /**
     * This implementation returns the name of the file.
     *
     * @see java.io.File#getName()
     */
    @Override
    public String getFilename() {
        return this.file.getName();
    }

    /**
     * This implementation returns a description that includes the absolute path
     * of the file.
     *
     * @see java.io.File#getAbsolutePath()
     */
    public String getDescription() {
        return "file [" + this.file.getAbsolutePath() + "]";
    }

    //implementation of WritableResource
    /**
     * This implementation checks whether the underlying file is marked as
     * writable (and corresponds to an actual file with content, not to a
     * directory).
     *
     * @see java.io.File#canWrite()
     * @see java.io.File#isDirectory()
     */
    public boolean isWritable() {
        return true;
    }

    /**
     * This implementation opens a FileOutputStream for the underlying file.
     *
     * @see java.io.FileOutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(this.file);
    }

    /**
     * This implementation compares the underlying File references.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StringResource other = (StringResource) obj;
        if (!Objects.equals(this.path, other.path))
            return false;
        return Objects.equals(this.content, other.content);
    }

    /**
     * This implementation returns the hash code of the underlying File
     * reference.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.path);
        hash = 53 * hash + Objects.hashCode(this.content);
        return hash;
    }

}
