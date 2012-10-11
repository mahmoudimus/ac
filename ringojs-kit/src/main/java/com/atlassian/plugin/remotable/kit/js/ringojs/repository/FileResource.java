/*
 * Helma License Notice
 *
 * The contents of this file are subject to the Helma License
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://adele.helma.org/download/helma/license.txt
 *
 * Copyright 1998-2003 Helma Software. All Rights Reserved.
 *
 * $RCSfile: FileResource.java,v $
 * $Author: hannes $
 * $Revision: 1.8 $
 * $Date: 2006/04/07 14:37:11 $
 *
 */

package com.atlassian.plugin.remotable.kit.js.ringojs.repository;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * File resource modified to support coffeescript
 */
public class FileResource extends AbstractResource {

    volatile File file;
    private static final Logger log = LoggerFactory.getLogger(FileResource.class);
    private static final CoffeeScriptCompiler compiler = new CoffeeScriptCompiler("1.3.3", true);

    public FileResource(String path) throws IOException {
        this(new File(path), null);
    }

    public FileResource(File file) throws IOException {
        this(file, null);
    }

    protected FileResource(File file, FileRepository repository) throws IOException {
        // make sure our directory has an absolute path,
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4117557
        file = file.getAbsoluteFile();

        repository = repository == null ?
                new FileRepository(file.getParentFile()) : repository;
        // Make sure path is canonical for all directories, while acutal file may be a symlink
        // TODO what we probably want to do here is to just normalize the path
        file = new File(repository.getPath(), file.getName());
        path = file.getPath();
        name = file.getName();
        this.file = file;
        this.repository = repository;
        // base name is short name with extension cut off
        int lastDot = name.lastIndexOf(".");
        baseName = (lastDot == -1) ? name : name.substring(0, lastDot);
    }

    public InputStream getInputStream() throws IOException {
        return stripShebang(getFileStream(getFile()));
    }

    private InputStream getFileStream(File file) throws IOException
    {
        if (file.getName().endsWith(".js"))
        {
            return new FileInputStream(file);
        }
        else
        {
            String source = FileUtils.readFileToString(file);
            String jsSource = compiler.compile(source);
            if (log.isDebugEnabled())
            {
                log.debug("Converted " + file.getName() + " to JavaScript:\n{}", jsSource);
            }
            return new ByteArrayInputStream(jsSource.getBytes(
                    Charset.defaultCharset()));
        }
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("file:" + getFile().getAbsolutePath());
    }

    public long lastModified() {
        return getFile().lastModified();
    }

    public long getLength() {
        return getFile().length();
    }

    public boolean exists() {
        // not a resource if it's a directory
        return getFile().isFile();
    }

    @Override
    public int hashCode() {
        return 17 + path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileResource && path.equals(((FileResource)obj).path);
    }

    @Override
    public String toString() {
        return getPath();
    }

    protected File getFile()
    {
        if (!file.isFile())
        {
            file = new File(file.getParentFile(), baseName + ".coffee");
        }
        return file;
    }
}
