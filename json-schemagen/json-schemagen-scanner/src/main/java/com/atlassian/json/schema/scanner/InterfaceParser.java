package com.atlassian.json.schema.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.atlassian.json.schema.scanner.model.InterfaceImplementors;
import com.atlassian.json.schema.scanner.model.InterfaceList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import static com.atlassian.json.schema.ClassloaderUtil.getClassloader;

public class InterfaceParser
{
    public InterfaceList parse(String basePackage, String classpath) throws Exception
    {
        ClassLoader classLoader = getClassloader(classpath);

        String path = basePackage.replace('.', '/');
        List<InterfaceImplementors> implementors = new ArrayList<InterfaceImplementors>();

        Map<String, Set<String>> ifaceToImpls = new HashMap<String, Set<String>>();
        InterfaceClassVisitor classVisitor = new InterfaceClassVisitor(ifaceToImpls);

        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements())
        {
            URL resource = resources.nextElement();
            processResource(resource, basePackage, classVisitor);
        }

        for (Map.Entry<String, Set<String>> entry : ifaceToImpls.entrySet())
        {
            implementors.add(new InterfaceImplementors(entry.getKey(), entry.getValue()));
        }
        return new InterfaceList(implementors);
    }

    protected void processResource(URL resource, String packageName, ClassVisitor classVisitor) throws IOException
    {
        if (resource.getProtocol()
                    .equals("file"))
        {
            processFileDirectory(new File(resource.getFile()), packageName, classVisitor);
        }
        else if (resource.getProtocol()
                         .equals("jar"))
        {
            JarURLConnection conn = (JarURLConnection) resource.openConnection();
            processJarDirectory(conn.getJarFile(), packageName, classVisitor);
        }
    }

    protected void processJarDirectory(JarFile jarFile, String packageName, ClassVisitor classVisitor) throws IOException
    {
        Enumeration<JarEntry> entries = jarFile.entries();
        String basePath = packageName.replace('.', '/');
        while (entries.hasMoreElements())
        {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(basePath) && !entry.isDirectory() && name.endsWith(".class"))
            {
                InputStream is = jarFile.getInputStream(entry);
                try
                {
                    processClassFile(is, classVisitor);
                }
                finally
                {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    protected void processFileDirectory(File directory, String packageName, ClassVisitor classVisitor) throws IOException
    {
        if (!directory.exists())
        {
            return;
        }
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                assert !file.getName().contains(".");
                processFileDirectory(file, packageName + "." + file.getName(), classVisitor);
            }
            else if (file.getName().endsWith(".class"))
            {
                InputStream is = FileUtils.openInputStream(file);

                try
                {
                    processClassFile(is, classVisitor);
                }
                finally
                {
                    IOUtils.closeQuietly(is);
                }

            }
        }
    }

    protected void processClassFile(InputStream is, ClassVisitor classVisitor) throws IOException
    {
        ClassReader classReader = new ClassReader(is);
        classReader.accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
}
