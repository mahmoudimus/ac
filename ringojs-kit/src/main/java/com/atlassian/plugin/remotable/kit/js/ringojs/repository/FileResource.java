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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * File resource modified to support coffeescript
 */
public class FileResource extends AbstractResource
{
    volatile File file;

    public FileResource(String path, File homeDir) throws IOException
    {
        this(new File(path), homeDir, null);
    }

    public FileResource(File file, File homeDir) throws IOException
    {
        this(file, homeDir, null);
    }

    protected FileResource(File file, File homeDir, FileRepository repository) throws IOException
    {
        // make sure our directory has an absolute path,
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4117557
        file = file.getAbsoluteFile();

        repository = repository == null ? new FileRepository(file.getParentFile(), homeDir) : repository;
        // Make sure path is canonical for all directories, while acutal file may be a symlink
        // TODO what we probably want to do here is to just normalize the path
        file = new File(repository.getPath(), file.getName());
        path = file.getPath();
        name = file.getName();
        this.file = file;
        this.repository = repository;
        this.homeDir = homeDir;
        // base name is short name with extension cut off
        int lastDot = name.lastIndexOf(".");
        baseName = (lastDot == -1) ? name : name.substring(0, lastDot);
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
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
            return compileCoffeeScript(FileUtils.readFileToString(file));
        }
    }

    @Override
    public URL getUrl() throws MalformedURLException
    {
        return new URL("file:" + getFile().getAbsolutePath());
    }

    @Override
    public long lastModified()
    {
        return getFile().lastModified();
    }

    @Override
    public long getLength()
    {
        return getFile().length();
    }

    @Override
    public boolean exists()
    {
        // not a resource if it's a directory
        return getFile().isFile();
    }

    @Override
    public int hashCode()
    {
        return 17 + path.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof FileResource && path.equals(((FileResource)obj).path);
    }

    @Override
    public String toString()
    {
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
