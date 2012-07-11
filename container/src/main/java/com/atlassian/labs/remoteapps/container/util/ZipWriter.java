package com.atlassian.labs.remoteapps.container.util;

import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.Sets.newHashSet;


/**
 *
 */
public class ZipWriter
{
    private static final String REMOTEAPPS_KEY_SEPARATOR = "----remoteapps-";
    private static final Logger log = LoggerFactory.getLogger(ZipWriter.class);

    public static File zipAppIntoPluginJar(File dir, String... pathsToExclude) throws IOException
    {
        RemoteAppDescriptorAccessor remoteAppDescriptorAccessor = new RemoteAppDescriptorAccessor(dir);

        File zipFile = createExtractableTempFile(dir.getName(), ".jar");
        Set<String> excludes = newHashSet(pathsToExclude);
        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zip(dir, dir, zos, excludes);

            if (!(new File(dir, "META-INF/MANIFEST.MF").exists()))
            {
                Document doc = remoteAppDescriptorAccessor.getDescriptor();
                String appKey = doc.getRootElement().attributeValue("key");
                String appVersion = doc.getRootElement().attributeValue("version");
                ZipEntry entry = new ZipEntry("META-INF/MANIFEST.MF");
                zos.putNextEntry(entry);
                IOUtils.write("Manifest-Version: 1.0\n" +
                        "Bundle-Version: " + appVersion + "\n" +
                        "Bundle-SymbolicName: " + appKey + "\n" +
                        "Atlassian-Plugin-Key: " + appKey + "\n" +
                        "Spring-Context: *;timeout:=60\n" +
                        "DynamicImport-Package: *\n" +
                        "Import-Package: org.springframework.beans.factory, com.atlassian.plugin.osgi.bridge.external," +
                        "org.jruby.ext.posix\n" +
                        "Bundle-ManifestVersion: 2\n", zos);

                addJarContents("ringojs.jar", zos);
                addJarContents("remoteapps-apputils.jar", zos);
                addJarContents("remoteapps-ringojs-kit.jar", zos);
            }
        }
        finally
        {
            IOUtils.closeQuietly(zos);
        }
        if (zipFile.length() == 0)
        {
            return null;
        }
        return zipFile;
    }

    private static void addJarContents(String jar, ZipOutputStream zos)
    {
        InputStream apputilsIn = null;
        try
        {
            apputilsIn = ZipWriter.class.getResourceAsStream("/" + jar);
            ZipInputStream zin = new ZipInputStream(apputilsIn);

            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null)
            {
                ZipEntry targetEntry = new ZipEntry(entry);
                try
                {
                    zos.putNextEntry(targetEntry);
                }
                catch (ZipException ex)
                {
                    log.debug("Unable to add file {}", entry.getName());
                    continue;
                }
                IOUtils.copy(zin, zos);
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to add apputils to jar", e);
        }
        finally
        {
            IOUtils.closeQuietly(apputilsIn);
        }
    }

    private static File createExtractableTempFile(String key, String suffix) throws IOException
    {
        return File.createTempFile(key + REMOTEAPPS_KEY_SEPARATOR, suffix);
    }

    private static final void zip(File directory, File base,
                                  ZipOutputStream zos, Set<String> excludes) throws IOException
    {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (int i = 0, n = files.length; i < n; i++)
        {
            String relativePath = files[i].getPath().substring(base.getPath().length() + 1);
            if (files[i].isDirectory() && !excludes.contains(files[i].getName()))
            {
                zos.putNextEntry(new ZipEntry(relativePath + "/"));
                zip(files[i], base, zos, excludes);
            }
            else
            {
                if (excludes.contains(files[i].getName()))
                {
                    continue;
                }
                FileInputStream in = null;
                try
                {
                    in = new FileInputStream(files[i]);
                    ZipEntry entry = new ZipEntry(relativePath);
                    zos.putNextEntry(entry);
                    while (-1 != (read = in.read(buffer)))
                    {
                        zos.write(buffer, 0, read);
                    }
                }
                finally
                {
                    IOUtils.closeQuietly(in);
                }
            }
        }
    }

    public static void addDirectoryToZip(ZipOutputStream zout, String path) throws IOException
    {
        ZipEntry entry = new ZipEntry(path);
        zout.putNextEntry(entry);
    }

    public static void addFileToZip(ZipOutputStream zout, String path, String archetypeName) throws IOException
    {
        InputStream in = null;
        try
        {
            in = ZipWriter.class.getResourceAsStream("/archetype/" + archetypeName);
            ZipEntry entry = new ZipEntry(path);
            zout.putNextEntry(entry);
            IOUtils.copy(in, zout);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

}
